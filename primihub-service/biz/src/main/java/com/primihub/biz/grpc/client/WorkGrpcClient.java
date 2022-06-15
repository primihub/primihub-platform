package com.primihub.biz.grpc.client;

import io.grpc.Channel;
import java_worker.VMNodeGrpc;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class WorkGrpcClient {

    @Resource(name="grpcClientChannel")
    private Channel channel;

    public <Result> Result run(Functional<VMNodeGrpc.VMNodeBlockingStub,Result> functional){
        VMNodeGrpc.VMNodeBlockingStub vMNodeBlockingStub =VMNodeGrpc.newBlockingStub(channel);
        return functional.run(vMNodeBlockingStub);
    }

    public <Result> Result run(Functional<VMNodeGrpc.VMNodeBlockingStub,Result> functional, Channel channel){
        VMNodeGrpc.VMNodeBlockingStub vMNodeBlockingStub =VMNodeGrpc.newBlockingStub(channel);
        return functional.run(vMNodeBlockingStub);
    }
}
