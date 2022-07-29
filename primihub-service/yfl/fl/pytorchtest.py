import torch
import numpy as np

if __name__ == '__main__':
    print(torch.IntTensor(2,3))
    print(torch.FloatTensor(2,3))
    print(torch.empty(2,3))

    print(torch.rand(2,3))
    print(torch.randn(2,3))
    # print(torch.normal(2.0,3.0))

    print(torch.full((2,3),7))
    print(torch.ones(2,3))
    print(torch.zeros(2,3))

    my_arr= np.array([1,2,3,4,5])
    print(torch.tensor(my_arr))
    print(torch.as_tensor(my_arr))

    my_arr[0]=10

    print(torch.tensor(my_arr))
    print(torch.as_tensor(my_arr))

    x=torch.tensor([1,2,3])
    y=torch.tensor([4,5,6])
    print(x+y)
    print(torch.add(x,y))

    a=torch.randn(5,3)
    b=a.view(3,5)
    print(a)
    print(b)

    x=torch.tensor([1,2,3])
    print(x.device)
    if torch.cuda.is_available():
        x=x.cuda()
    print(x.device)

    x = torch.ones(2,2,requires_grad=True)
    y = x+2
    z = y*y*3
    out = z.mean()
    out.backward()
    print(x.grad)



