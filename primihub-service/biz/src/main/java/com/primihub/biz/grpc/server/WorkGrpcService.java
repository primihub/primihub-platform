package com.primihub.biz.grpc.server;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import java_worker.VMNodeGrpc;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class WorkGrpcService extends VMNodeGrpc.VMNodeImplBase {

    @Override
    public void submitTask(PushTaskRequest request, StreamObserver<PushTaskReply> responseObserver) {
        try {
//            Thread.sleep(10000L);
        }catch (Exception e){

        }
        PushTaskReply reply=PushTaskReply.newBuilder().build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
