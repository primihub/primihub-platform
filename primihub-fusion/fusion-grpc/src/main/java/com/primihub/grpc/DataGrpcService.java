package com.primihub.grpc;

import com.google.protobuf.ByteString;
import com.primihub.entity.DataSet;
import com.primihub.service.StorageService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@GrpcService
public class DataGrpcService extends DataSetServiceGrpc.DataSetServiceImplBase {

    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    private StorageService storageService;

    @Override
    public void newDataset(NewDatasetRequest request, StreamObserver<NewDatasetResponse> responseObserver) {
        MetaInfo metaInfo = request.getMetaInfo();
        String id = metaInfo.getId();
        String accessInfo = metaInfo.getAccessInfo();
        String driver = metaInfo.getDriver();
        String address = metaInfo.getAddress();
        String vibility = metaInfo.getVibility().getValueDescriptor().getName();
        String operator = request.getOperator().getValueDescriptor().getName();
        if (id == null || "".equals(id)){
            newError(responseObserver,ResultCode.FAIL,"param id Cannot be empty");
            return;
        }
        DataSet dataSet = new DataSet(id, accessInfo, driver, address, vibility);
        if ("REGISTER".equals(operator)){
            if (accessInfo == null || "".equals(accessInfo)){
                newError(responseObserver,ResultCode.FAIL,"param accessInfo Cannot be empty");
                return;
            }
            if (driver == null || "".equals(driver)){
                newError(responseObserver,ResultCode.FAIL,"param driver Cannot be empty");
                return;
            }
            if (address == null || "".equals(address)){
                newError(responseObserver,ResultCode.FAIL,"param address Cannot be empty");
                return;
            }

            storageService.saveDataSet(dataSet);
            log.info("REGISTER ---- toString:{}", dataSet.toString());
        }else if ("UPDATE".equals(operator)){
            if (address == null || "".equals(address)){
                newError(responseObserver,ResultCode.FAIL,"param address Cannot be empty");
                return;
            }
            storageService.updateDataSet(dataSet);
            log.info("REGISTER ---- toString:{}", dataSet.toString());
        }else if ("UNREGISTER".equals(operator)){
            storageService.deleteDataSet(dataSet);
        }else {
            newError(responseObserver,ResultCode.FAIL,operator+":Operation type has no implementation");
        }
        NewDatasetResponse result = NewDatasetResponse.newBuilder().setRetCode(ResultCode.SUCCESS).setDatasetUrl("success:"+id).build();
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }


    public void getDataset(GetDatasetRequest request, StreamObserver<GetDatasetResponse> responseObserver){
        List<ByteString> ids = request.getIdList().asByteStringList();
        GetDatasetResponse.Builder builder = GetDatasetResponse.newBuilder();
        List<DataSet> list;
        if (ids==null || ids.isEmpty()){
            list = storageService.getAll();
        }else {
            list = storageService.getByIds(ids.stream().map(ByteString::toString).collect(Collectors.toList()));
        }
        for (DataSet dataSet : list) {
            builder.addDataSet(dataModelConvertVo(dataSet));
        }
        responseObserver.onNext(builder.setRetCode(ResultCode.SUCCESS).build());
        responseObserver.onCompleted();
    }

    private void newError(StreamObserver<NewDatasetResponse> responseObserver,ResultCode retcode,String msg){
        responseObserver.onNext(NewDatasetResponse.newBuilder().setRetCode(retcode).setRetMsg(msg).build());
        responseObserver.onCompleted();
    }
    private void getError(StreamObserver<GetDatasetResponse> responseObserver,ResultCode retcode,String msg){
        responseObserver.onNext(GetDatasetResponse.newBuilder().setRetCode(retcode).setRetMsg(msg).build());
        responseObserver.onCompleted();
    }

    public static DatasetData dataModelConvertVo(DataSet dataSet){
        return DatasetData.newBuilder()
                .setAvailable(DatasetData.Status.valueOf(dataSet.getAvailable()))
                .setMetaInfo(MetaInfo.newBuilder()
                        .setId(dataSet.getId())
                        .setAccessInfo(dataSet.getAccessInfo())
                        .setDriver(dataSet.getDriver())
                        .setAddress(dataSet.getAddress())
                        .setVibility(MetaInfo.Visibility.valueOf(dataSet.getVibility())).build()).build();
    }
}
