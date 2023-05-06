package com.primihub.biz.grpc.server;

import io.grpc.stub.StreamObserver;
import java_data_service.DataSetServiceGrpc;
import java_data_service.NewDatasetRequest;
import java_data_service.NewDatasetResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataGrpcService extends DataSetServiceGrpc.DataSetServiceImplBase {

    @Override
    public void newDataset(NewDatasetRequest request, StreamObserver<NewDatasetResponse> responseObserver) {
        NewDatasetResponse result = NewDatasetResponse.newBuilder().setRetCode(NewDatasetResponse.ResultCode.SUCCESS).setDatasetUrl("success:"+request.getMetaInfo().getAccessInfo()).build();
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }

}
