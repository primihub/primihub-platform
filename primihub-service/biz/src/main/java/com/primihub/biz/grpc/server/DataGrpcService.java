package com.primihub.biz.grpc.server;

import io.grpc.stub.StreamObserver;
import java_data_service.DataServiceGrpc;
import java_data_service.NewDatasetRequest;
import java_data_service.NewDatasetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataGrpcService extends DataServiceGrpc.DataServiceImplBase {

    @Override
    public void newDataset(NewDatasetRequest request, StreamObserver<NewDatasetResponse> responseObserver) {
        NewDatasetResponse result = NewDatasetResponse.newBuilder().setRetCode(0).setDatasetUrl("success:"+request.getPath()).build();
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

}
