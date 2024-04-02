package com.primihub.sdk.task.factory;

import com.primihub.sdk.task.cache.CacheService;
import com.primihub.sdk.task.param.TaskDataSetParam;
import com.primihub.sdk.task.param.TaskParam;
import io.grpc.Channel;
import java_data_service.DataTypeInfo;
import java_data_service.MetaInfo;
import java_data_service.NewDatasetRequest;
import java_data_service.NewDatasetResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractDataSetGRPCExecute extends AbstractGRPCExecuteFactory {

    private final static Logger log = LoggerFactory.getLogger(AbstractDataSetGRPCExecute.class);

    @Override
    public CacheService getCacheService() {
        return null;
    }
    @Override
    public void setCacheService(CacheService cacheService) {
    }

    @Override
    public void execute(Channel channel, TaskParam taskParam) {
        runDataSet(channel, taskParam);
    }

    private void runDataSet(Channel channel,TaskParam<TaskDataSetParam> param){
        log.info("run dataServiceGrpc param:{} - time:{}",param.toString(),System.currentTimeMillis());
        MetaInfo.Builder metaInfoBuilder = MetaInfo.newBuilder()
                .setId(param.getTaskContentParam().getId())
                .setAccessInfo(param.getTaskContentParam().getAccessInfo())
                .setDriver(param.getTaskContentParam().getDriver())
                .setVisibility(MetaInfo.Visibility.PUBLIC);
        for (TaskDataSetParam.FieldType fieldType : param.getTaskContentParam().getFieldTypes()) {
            metaInfoBuilder.addDataType(fieldType.getPlainDataType()!=null?DataTypeInfo.newBuilder().setType(fieldType.getPlainDataType()):DataTypeInfo.newBuilder().setType(DataTypeInfo.PlainDataType.valueOf(fieldType.getFieldTypeEnum().getNodeTypeName())).setName(fieldType.getName()));
        }
        NewDatasetRequest request = NewDatasetRequest.newBuilder().setMetaInfo(metaInfoBuilder).setOpTypeValue(NewDatasetRequest.Operator.REGISTER.getNumber()).build();
        log.info("NewDatasetRequest:{}",request.toString());
        try {
            NewDatasetResponse response = runDataServiceGrpc(o -> o.newDataset(request),channel);
            log.info("dataServiceGrpc Response:{}",response.toString());
            NewDatasetResponse.ResultCode retCode = response.getRetCode();
            if (retCode.getValueDescriptor().getName().equals(NewDatasetResponse.ResultCode.SUCCESS.getValueDescriptor().getName())){
                log.info("dataServiceGrpc success");
            }else {
                param.setError(response.getRetMsg());
                param.setSuccess(false);
            }
        }catch (Exception e){
            param.setSuccess(false);
            param.setError(e.getMessage());
            log.info("dataServiceGrpcException:{}",e.getMessage());
            e.printStackTrace();
        }
        param.setEnd(true);
        log.info("end dataServiceGrpc fparam:{} - time:{}",param.toString(),System.currentTimeMillis());
    }
}
