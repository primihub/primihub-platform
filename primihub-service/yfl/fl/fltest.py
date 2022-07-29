import random

import torch.utils.data
import torchvision


def get_dataset(dir, name):
    if name == 'mnist':
        train_dataset = torchvision.datasets.MNIST(dir, train=True, download=True,
                                                   transform=torchvision.transforms.ToTensor())
        eval_dataset = torchvision.datasets.MNIST(dir, train=True, transform=torchvision.transforms.ToTensor())
    elif name == 'cifar':
        transform_train = torchvision.transforms.Compose([
            torchvision.transforms.RandomCrop(32, padding=4), torchvision.transforms.RandomHorizontalFlip(),
            torchvision.transforms.ToTensor(),
            torchvision.transforms.Normalize((0.4914, 0.4822, 0.4465), (0.2023, 0.1994, 0.2010)),
        ])

        transform_test = torchvision.transforms.Compose([
            torchvision.transforms.ToTensor(),
            torchvision.transforms.Normalize((0.4914, 0.4822, 0.4465), (0.2023, 0.1994, 0.2010)),
        ])

        train_dataset = torchvision.datasets.CIFAR10(dir, train=True, download=True, transform=transform_train)
        eval_dataset = torchvision.datasets.CIFAR10(dir, train=False, transform=transform_test)
    return train_dataset, eval_dataset


class Server(object):

    def __init__(self, conf, eval_dataset):
        self.conf = conf
        self.global_model = torchvision.models.resnet18()
        self.eval_loader = torch.utils.data.DataLoader(eval_dataset, batch_size=self.conf["batch_size"], shuffle=True)

    def model_aggregate(self, weight_accumulator):
        for name, data in self.global_model.state_dict().items():
            update_per_layer = weight_accumulator[name] * self.conf["lambda"]
            if data.type() != update_per_layer.type():
                data.add_(update_per_layer.to(torch.int64))
            else:
                data.add_(update_per_layer)

    def model_eval(self):
        self.global_model.eval()
        total_loss = 0.0
        correct = 0
        dataset_size = 0
        for batch_id, batch in enumerate(self.eval_loader):
            data, target = batch
        dataset_size += data.size()[0]
        if torch.cuda.is_available():
            data = data.cuda()
            target = target.cuda()
        output = self.global_model(data)
        total_loss += torch.nn.functional.cross_entropy(output, target, reduction='sum').item()
        pred = output.data.max(1)[1]
        correct += pred.eq(target.data.view_as(pred)).cpu().sum().item()
        acc = 100.0 * (float(correct) / float(dataset_size))
        total_1 = total_loss / dataset_size
        return acc, total_1


class Client(object):
    def __init__(self, conf, model, train_dataset, id=1):
        self.conf = conf
        self.local_model = model
        self.client_id = id
        self.train_dataset = train_dataset
        all_range = list(range(len(self.train_dataset)))
        data_len = int(len(self.train_dataset) / self.conf['no_models'])
        indices = all_range[id * data_len:(id + 1) * data_len]
        self.train_loader = torch.utils.data.DataLoader(self.train_dataset,
                                                        batch_size=conf["batch_size"],
                                                        sampler=torch.utils.data.sampler.SubsetRandomSampler(indices))

    def local_train(self, model):
        for name, param in model.state_dict().items():
            self.local_model.state_dict()[name].copy_(param.clone())

        optimizer = torch.optim.SGD(self.local_model.parameters(), lr=self.conf['lr'], momentum=self.conf['momentum'])
        self.local_model.train()
        for e in range(self.conf["local_epochs"]):
            for batch_id, batch in enumerate(self.train_loader):
                data, target = batch
                if torch.cuda.is_available():
                    data = data.cuda()
                    target = target.cuda()
            optimizer.zero_grad()
            output = self.local_model(data)
            loss = torch.nn.functional.cross_entropy(output, target)
            loss.backward()
            optimizer.step()
            print("Epoch%d done." % e)
        diff = dict()
        for name, data in self.local_model.state_dict().items():
            diff[name] = (data - model.state_dict()[name])
        return diff


if __name__ == '__main__':

    conf = {
        "no_models": 10,
        "type": "cifar",
        "global_epochs": 20,
        "local_epochs": 3,
        "k": 6,
        "batch_size": 32,
        "lr": 0.001,
        "momentum": 0.0001,
        "lambda": 0.1
    }

    train_datasets, eval_datasets = get_dataset("./data", conf["type"])
    server = Server(conf, eval_datasets)
    clients = []

    for c in range(conf["no_models"]):
        clients.append(Client(conf, server.global_model, train_datasets, c))

    for e in range(conf["global_epochs"]):
        candidates = random.sample(clients, conf['k'])
        weight_accumulator = {}
        for name, params in server.global_model.state_dict().items():
            weight_accumulator[name] = torch.zeros_like(params)

        for c in candidates:
            diff = c.local_train(server.global_model)
            for name, param in server.global_model.state_dict().items():
                weight_accumulator[name].add_(diff[name])

    server.model_aggregate(weight_accumulator)
    acc, loss = server.model_eval()
    print("Epoch %d, acc:%f,loss:%f\n" % (e, acc, loss))
