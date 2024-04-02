package com.primihub.sdk.task.factory;

import com.google.protobuf.ByteString;
import com.primihub.sdk.task.cache.CacheService;
import com.primihub.sdk.task.param.TaskPSIParam;
import com.primihub.sdk.task.param.TaskParam;
import io.grpc.Channel;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class AbstractPsiGRPCExecute extends AbstractGRPCExecuteFactory {

    private final static Logger log = LoggerFactory.getLogger(AbstractPsiGRPCExecute.class);

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
        runPsi(channel,taskParam);
    }

    private void runPsi(Channel channel, TaskParam<TaskPSIParam> param){
        try {
            log.info("grpc run {} - time:{}",param.toString(),System.currentTimeMillis());
            Common.ParamValue clientDataParamValue=Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(param.getTaskContentParam().getClientData().getBytes(StandardCharsets.UTF_8))).build();
            Common.ParamValue serverDataParamValue=Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(param.getTaskContentParam().getServerData().getBytes(StandardCharsets.UTF_8))).build();
            Common.ParamValue psiTypeParamValue=Common.ParamValue.newBuilder().setValueInt32(param.getTaskContentParam().getPsiType()).build();
            Common.ParamValue psiTagParamValue=Common.ParamValue.newBuilder().setValueInt32(param.getTaskContentParam().getPsiTag()).build();
            Common.int32_array.Builder clientFieldsBuilder = Common.int32_array.newBuilder();
            for (Integer clientIndex : param.getTaskContentParam().getClientIndex()) {
                clientFieldsBuilder.addValueInt32Array(clientIndex);
            }
            Common.int32_array.Builder serverFieldsBuilder = Common.int32_array.newBuilder();
            for (Integer serverIndex : param.getTaskContentParam().getServerIndex()) {
                serverFieldsBuilder.addValueInt32Array(serverIndex);
            }
            Common.ParamValue clientIndexParamValue=Common.ParamValue.newBuilder().setIsArray(true).setValueInt32Array(clientFieldsBuilder.build()).build();
            Common.ParamValue serverIndexParamValue=Common.ParamValue.newBuilder().setIsArray(true).setValueInt32Array(serverFieldsBuilder.build()).build();
            Common.ParamValue outputFullFilenameParamValue=Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(param.getTaskContentParam().getOutputFullFilename().getBytes(StandardCharsets.UTF_8))).build();
            Common.Params.Builder paramsBuilder = Common.Params.newBuilder()
                    .putParamMap("clientData", clientDataParamValue)
                    .putParamMap("serverData", serverDataParamValue)
                    .putParamMap("psiType", psiTypeParamValue)
                    .putParamMap("psiTag", psiTagParamValue)
                    .putParamMap("clientIndex", clientIndexParamValue)
                    .putParamMap("serverIndex", serverIndexParamValue)
                    .putParamMap("outputFullFilename", outputFullFilenameParamValue);

            if (param.getTaskContentParam().getSyncResultToServer()==1){
                Common.ParamValue syncResultToServerParamValue=Common.ParamValue.newBuilder().setValueInt32(1).build();
                Common.ParamValue serverOutputFullFilnameParamValue=Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(param.getTaskContentParam().getServerOutputFullFilname().getBytes(StandardCharsets.UTF_8))).build();
                paramsBuilder.putParamMap("sync_result_to_server",syncResultToServerParamValue).putParamMap("server_outputFullFilname",serverOutputFullFilnameParamValue);
            }
            Common.TaskContext taskBuild = assembleTaskContext(param);
            Map<String, Common.Dataset> datasetMap = new HashMap<>();
            datasetMap.put("SERVER",Common.Dataset.newBuilder().putData("SERVER", param.getTaskContentParam().getServerData()).build());
            datasetMap.put("CLIENT",Common.Dataset.newBuilder().putData("CLIENT", param.getTaskContentParam().getClientData()).build());
            String code = "";
            if (param.getTaskContentParam().getPsiTag().equals(2)){
                datasetMap.put("TEE_COMPUTE",Common.Dataset.newBuilder().putData("TEE_COMPUTE", param.getTaskContentParam().getTeeData()).build());
                code = "psi";
            }
            Common.Task task= Common.Task.newBuilder()
                    .setType(Common.TaskType.PSI_TASK)
                    .setParams(paramsBuilder.build())
                    .setName("psiTask")
                    .setTaskInfo(taskBuild)
                    .setLanguage(Common.Language.PROTO)
                    .setCode(ByteString.copyFrom(code.getBytes(StandardCharsets.UTF_8)))
                    .putAllPartyDatasets(datasetMap)
                    .build();
            log.info("grpc Common.Task : \n{}",task.toString());
            PushTaskRequest request=PushTaskRequest.newBuilder()
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
        } catch (Exception e) {
            param.setSuccess(false);
            param.setError(e.getMessage());
            log.info("grpc Exception:{}",e.getMessage());
            e.printStackTrace();
        }
        param.setEnd(true);
        log.info("grpc end {} - time:{}",param.toString(),System.currentTimeMillis());
    }
}
