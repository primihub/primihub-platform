package com.primihub.service.grpc;

import com.google.protobuf.ByteString;
import com.primihub.constant.SysConstant;
import com.primihub.entity.base.BaseResultEnum;
import io.grpc.stub.StreamObserver;
import java_data_service.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@GrpcService
public class DataGrpcService extends DataSetServiceGrpc.DataSetServiceImplBase {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void newDataset(NewDatasetRequest request, StreamObserver<NewDatasetResponse> responseObserver) {
        int type = request.getType();
        String id = request.getId();
        String driver = request.getDriver();
        String address = request.getAddress();
        String expand = request.getExpand();
        if (type==0){
            // register dataset
            if (StringUtils.isBlank(id)){
                newError(responseObserver,BaseResultEnum.FAILURE.getReturnCode(),"param id Cannot be empty");
                return;
            }
            if (StringUtils.isBlank(driver)){
                newError(responseObserver,BaseResultEnum.FAILURE.getReturnCode(),"param driver Cannot be empty");
                return;
            }
            if (StringUtils.isBlank(address)){
                newError(responseObserver,BaseResultEnum.FAILURE.getReturnCode(),"param address Cannot be empty");
                return;
            }
            if (StringUtils.isBlank(expand)){
                newError(responseObserver,BaseResultEnum.FAILURE.getReturnCode(),"param expand Cannot be empty");
                return;
            }
            Map<String,Object> map = new HashMap<>();
            map.put(SysConstant.GRPC_DATASET_FIELD_DRIVER,driver);
            map.put(SysConstant.GRPC_DATASET_FIELD_ADDRESS,address);
            map.put(SysConstant.GRPC_DATASET_FIELD_EXPAND,expand);
            stringRedisTemplate.opsForHash().putAll(SysConstant.GRPC_DATASET_KEY.replace("<id>",id),map);
        }else if (type==1){
            // Update address
            if (StringUtils.isBlank(address)){
                newError(responseObserver,BaseResultEnum.FAILURE.getReturnCode(),"param address Cannot be empty");
                return;
            }
            // TODO update node address
        }else {
            newError(responseObserver,BaseResultEnum.FAILURE.getReturnCode(),"type Not in scope");
            return;
        }
        log.info("type:{} - driver:{} - id:{} - address:{} - expand:{}",type,driver,id,address,expand);
        NewDatasetResponse result = NewDatasetResponse.newBuilder().setRetCode(0).setDatasetUrl("success:"+request.getAddress()).build();
        responseObserver.onNext(result);
        responseObserver.onCompleted();
    }


    public void getDataset(GetDatasetRequest request, StreamObserver<GetDatasetResponse> responseObserver){
        List<ByteString> ids = request.getIdList().asByteStringList();
        if (ids==null || ids.isEmpty()){
            getError(responseObserver,BaseResultEnum.FAILURE.getReturnCode(),"param id Cannot be empty");
            return;
        }
        GetDatasetResponse.Builder builder = GetDatasetResponse.newBuilder();
        for (ByteString id : ids) {
            String key = SysConstant.GRPC_DATASET_KEY.replace("<id>", id.toStringUtf8());
            if(stringRedisTemplate.hasKey(key)){
                Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
                DatasetData datasetData = DatasetData.newBuilder().setAddress(entries.get(SysConstant.GRPC_DATASET_FIELD_ADDRESS).toString())
                        .setDriver(entries.get(SysConstant.GRPC_DATASET_FIELD_DRIVER).toString())
                        .setExpand(entries.get(SysConstant.GRPC_DATASET_FIELD_EXPAND).toString())
                        .setAvailable(0).build();
                builder.addDataSet(datasetData);
            }
        }
        responseObserver.onNext(builder.setRetCode(0).build());
        responseObserver.onCompleted();
    }

    private void newError(StreamObserver<NewDatasetResponse> responseObserver,int code,String msg){
        responseObserver.onNext(NewDatasetResponse.newBuilder().setRetCode(code).setRetMsg(msg).build());
        responseObserver.onCompleted();
    }
    private void getError(StreamObserver<GetDatasetResponse> responseObserver,int code,String msg){
        responseObserver.onNext(GetDatasetResponse.newBuilder().setRetCode(code).setRetMsg(msg).build());
        responseObserver.onCompleted();
    }
}
