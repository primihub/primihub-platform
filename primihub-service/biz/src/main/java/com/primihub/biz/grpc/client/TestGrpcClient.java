package com.primihub.biz.grpc.client;

import io.grpc.Channel;
import java_test.TestServiceGrpc;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class TestGrpcClient {

    @Resource(name="grpcClientChannel")
    private Channel channel;

    public <Result> Result run(Functional<TestServiceGrpc.TestServiceBlockingStub,Result> functional){
        TestServiceGrpc.TestServiceBlockingStub testServiceBlockingStub = TestServiceGrpc.newBlockingStub(channel);
        return functional.run(testServiceBlockingStub);
    }

    public <Result> Result run(Functional<TestServiceGrpc.TestServiceBlockingStub,Result> functional,Channel channel){
        TestServiceGrpc.TestServiceBlockingStub testServiceBlockingStub = TestServiceGrpc.newBlockingStub(channel);
        return functional.run(testServiceBlockingStub);
    }

}
