package com.primihub.sdk.task.factory;

import com.google.protobuf.ByteString;
import com.primihub.sdk.task.cache.CacheService;
import com.primihub.sdk.task.param.TaskPIRParam;
import com.primihub.sdk.task.param.TaskParam;
import io.grpc.Channel;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;

public class AbstractPirGRPCExecute extends AbstractGRPCExecuteFactory {

    private final static String QUERY_CONFIG_JSON = "{ \"SERVER\": {\"key_columns\": <key_columns>} }";

    private final static Logger log = LoggerFactory.getLogger(AbstractPirGRPCExecute.class);

    private CacheService cacheService;

    @Override
    public CacheService getCacheService() {
        return cacheService;
    }

    @Override
    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void execute(Channel channel, TaskParam taskParam) {
        runPir(channel,taskParam);
    }

    private void runPir(Channel channel, TaskParam<TaskPIRParam> param){
        try {
            log.info("grpc run {} - time:{}", param.toString(), System.currentTimeMillis());
            Common.string_array.Builder builder = Common.string_array.newBuilder();
            for (String str : param.getTaskContentParam().getQueryParam()) {
                builder.addValueStringArray(ByteString.copyFrom(str.getBytes(StandardCharsets.UTF_8)));
            }
            Common.ParamValue clientDataParamValue = Common.ParamValue.newBuilder().setIsArray(true).setValueStringArray(builder).build();
            Common.ParamValue serverDataParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(param.getTaskContentParam().getServerData().getBytes(StandardCharsets.UTF_8))).build();
            Common.ParamValue pirTagParamValue = Common.ParamValue.newBuilder().setValueInt32(1).build();
            Common.ParamValue outputFullFilenameParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(param.getTaskContentParam().getOutputFullFilename().getBytes(StandardCharsets.UTF_8))).build();
            String queryConfig = "";
            if (param.getTaskContentParam().getKeyColumns() == null || param.getTaskContentParam().getKeyColumns().length==0){
                param.setError("KeyColumns 不可以为空");
                param.setSuccess(false);
                param.setEnd(true);
                log.info("grpc end {} - time:{}", param.toString(), System.currentTimeMillis());
                return;
            }
            queryConfig = QUERY_CONFIG_JSON.replace("<key_columns>",param.getTaskContentParam().getKeyColumnsString());
            Common.ParamValue aueryConfigParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(queryConfig.getBytes(StandardCharsets.UTF_8))).build();
            Common.Params params = Common.Params.newBuilder()
                    .putParamMap("clientData", clientDataParamValue)
                    .putParamMap("serverData", serverDataParamValue)
                    .putParamMap("pirType", pirTagParamValue)
                    .putParamMap("outputFullFilename", outputFullFilenameParamValue)
                    .putParamMap("QueryConfig", aueryConfigParamValue)
                    .build();
            Common.TaskContext taskBuild = assembleTaskContext(param);
            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.PIR_TASK)
                    .setParams(params)
                    .setName("pirTask")
                    .setTaskInfo(taskBuild)
                    .setLanguage(Common.Language.PROTO)
                    .setCode(ByteString.copyFrom("".getBytes(StandardCharsets.UTF_8)))
                    .putPartyDatasets("SERVER", Common.Dataset.newBuilder().putData("SERVER", param.getTaskContentParam().getServerData()).build())
                    .build();
            log.info("grpc Common.Task :\n{}",task.toString());
            PushTaskRequest request = PushTaskRequest.newBuilder()
                    .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                    .setTask(task)
                    .setSequenceNumber(11)
                    .setClientProcessedUpTo(22)
                    .build();
            PushTaskReply reply = runVMNodeGrpc(o -> o.submitTask(request),channel);
            log.info("grpc结果:"+reply);
            if (reply.getRetCode()==0){
                param.setPartyCount(reply.getPartyCount());
                if (param.getOpenGetStatus()){
                    continuouslyObtainTaskStatus(channel,taskBuild,param,reply.getPartyCount());
                }
            }else {
                param.setError(reply.getMsgInfo().toStringUtf8());
                param.setSuccess(false);
            }
            log.info("grpc end {} - time:{}", param.toString(), System.currentTimeMillis());
        } catch (Exception e) {
            param.setSuccess(false);
            param.setError(e.getMessage());
            log.info("grpc pir Exception:{}",e.getMessage());
            e.printStackTrace();
        }
        param.setEnd(true);
    }
}
