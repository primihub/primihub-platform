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

    public void submitTask(PushTaskRequest request, StreamObserver<PushTaskReply> responseObserver) {
        PushTaskReply reply=PushTaskReply.newBuilder().setJobId(ByteString.copyFrom("666".getBytes(StandardCharsets.UTF_8))).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

}
