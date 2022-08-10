#!/usr/bin/env python
# -*- coding: utf-8 -*-
# Python version: 3.6

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt
import copy
import numpy as np
from torchvision import datasets, transforms
import torch

from utils.sampling import mnist_iid, mnist_noniid, cifar_iid
from utils.options import args_parser
from models.Update import LocalUpdate
from models.Nets import MLP, CNNMnist, CNNCifar
from models.Fed import FedAvg
from models.test import test_img
import os


if __name__ == '__main__':
    # parse args
    args = args_parser()
    args.device = torch.device('cuda:{}'.format(args.gpu) if torch.cuda.is_available() and args.gpu != -1 else 'cpu')

    # load dataset and split users
    # 手写体数据集，0-9分类器
    if args.dataset == 'mnist':
        # 将图片转化成tensor类型并进行归一化操作 方便后续快速收敛和防止数据溢出 transforms.Normalize来进行归一化
        trans_mnist = transforms.Compose([transforms.ToTensor(), transforms.Normalize((0.1307,), (0.3081,))])
        dataset_train = datasets.MNIST('./data/', train=True, download=True, transform=trans_mnist)
        dataset_test = datasets.MNIST('./data/', train=False, download=True, transform=trans_mnist)
        # sample users
        # 模拟测试FedAvg在独立同分布和非独立同分布下的情况
        # 将图片进行数据分发
        if args.iid:
            # 独立同分布
            dict_users = mnist_iid(dataset_train, args.num_users)
        else:
            # 非独立同分布
            dict_users = mnist_noniid(dataset_train, args.num_users)
    # cifar-10是一个包含60000张图片的数据集
    elif args.dataset == 'cifar':
        trans_cifar = transforms.Compose([transforms.ToTensor(), transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))])
        dataset_train = datasets.CIFAR10('./data/', train=True, download=True, transform=trans_cifar)
        dataset_test = datasets.CIFAR10('./data/', train=False, download=True, transform=trans_cifar)
        # 将图片进行数据分发
        if args.iid:
            dict_users = cifar_iid(dataset_train, args.num_users)
        else:
            exit('Error: only consider IID setting in CIFAR10')
    else:
        exit('Error: unrecognized dataset')
    img_size = dataset_train[0][0].shape

    # build model
    # mlp是cnn的一个特例，mlp实际是一个1*1的卷积 当卷积核大小与输入大小相同时起计算过程等价于mlp,mlp等价于卷积核大小与每层输入大小相同的cnn
    # 比如输入图片为28*28，卷积核大小为28*28
    # 神经网络结构 输入层X:1*n  隐藏层H:m  输出层Y:O  可以推断出w1:n*m  w2 m*o
    # H=X*W1+b1 Y=H*w2+b2
    # 神经网路训练：1.输入数据 2.向前传播 3.计算输出层及损失 4.误差反向传播 5 优化参数（w,b）
    #
    # Relu：激励函数，更加效率的梯度下降，以及反向传播，避免了梯度爆炸和梯度消失问题 pytorch.nn.functional模块 relu() sigmoid() tanh() softplus()
    #
    # 计算损失 CrossEntropyLoss():交叉熵损失函数（-logQ） BCELoss():二元交叉熵损失函数 MSELoss():均方损失函数
    #
    # 反向传播及参数优化 :
    # SGD（随机梯度下降算法torch.optim.SGD()）
    # Momentum(标准动量优化算法torch.optim.SGD(momentum=0.8))
    # RMSProp算法(torch.optim.RMSprop())
    # Adam算法(torch.optim.Adam())
    if args.model == 'cnn' and args.dataset == 'cifar':
        net_glob = CNNCifar(args=args).to(args.device)
    elif args.model == 'cnn' and args.dataset == 'mnist':
        net_glob = CNNMnist(args=args).to(args.device)
    elif args.model == 'mlp':
        len_in = 1
        for x in img_size:
            len_in *= x
        net_glob = MLP(dim_in=len_in, dim_hidden=200, dim_out=args.num_classes).to(args.device)
    else:
        exit('Error: unrecognized model')
    # net_glob就是未训练前的网络模型
    print(net_glob)
    # 切换成训练模式 记录每一批数据的均值和方式，并进行误差计算，反向传播和参数更新  测试模式为model.eval() 测试模式不需要记录均值和方差
    net_glob.train()

    # copy weights
    # 赋值训练过程中需要学习的权重和偏执系数
    w_glob = net_glob.state_dict()

    # training
    loss_train = []
    cv_loss, cv_acc = [], []
    val_loss_pre, counter = 0, 0
    net_best = None
    best_loss = None
    val_acc_list, net_list = [], []

    if args.all_clients: 
        print("Aggregation over all clients")
        w_locals = [w_glob for i in range(args.num_users)]
    for iter in range(args.epochs):
        loss_locals = []
        if not args.all_clients:
            w_locals = []
        # 0.1*100
        m = max(int(args.frac * args.num_users), 1)
        # 随机选取一部分Client,全部选择会增加通信量，还有可能训练效果不好
        idxs_users = np.random.choice(range(args.num_users), m, replace=False)
        for idx in idxs_users:
            # 设置计算损失的模型
            local = LocalUpdate(args=args, dataset=dataset_train, idxs=dict_users[idx])
            # 实际计算训练过程
            w, loss = local.train(net=copy.deepcopy(net_glob).to(args.device))
            if args.all_clients:
                w_locals[idx] = copy.deepcopy(w)
            else:
                w_locals.append(copy.deepcopy(w))
            loss_locals.append(copy.deepcopy(loss))
        # update global weights
        # fedAvg减少通信量，快速收敛
        w_glob = FedAvg(w_locals)

        # copy weight to net_glob
        net_glob.load_state_dict(w_glob)

        # print loss
        # 每个epoch的总体损失
        loss_avg = sum(loss_locals) / len(loss_locals)
        print('Round {:3d}, Average loss {:.3f}'.format(iter, loss_avg))
        loss_train.append(loss_avg)

    # plot loss curve
    file_name = './save/fed_{}_{}_{}_C{}_iid{}.png'.format(args.dataset, args.model, args.epochs, args.frac, args.iid)
    if not os.path.exists('./save'):
        os.makedirs('./save')
    plt.figure()
    plt.plot(range(len(loss_train)), loss_train)
    plt.ylabel('train_loss')
    plt.savefig(file_name)

    # testing
    net_glob.eval()
    acc_train, loss_train = test_img(net_glob, dataset_train, args)
    acc_test, loss_test = test_img(net_glob, dataset_test, args)
    print("Training accuracy: {:.2f}".format(acc_train))
    print("Testing accuracy: {:.2f}".format(acc_test))

