package com.yyds.biz.grpc.server;

import io.grpc.stub.StreamObserver;
import java_test.Request;
import java_test.Result;
import java_test.TestServiceGrpc;
import org.springframework.stereotype.Component;

@Component
public class TestGrpcService extends TestServiceGrpc.TestServiceImplBase {

    public void method(Request request, StreamObserver<Result> responseObserver) {
        Result result = Result.newBuilder().setResult1(request.getRequest1()+"result1").setResult2(request.getRequest2()+"result2").build();
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }
}
