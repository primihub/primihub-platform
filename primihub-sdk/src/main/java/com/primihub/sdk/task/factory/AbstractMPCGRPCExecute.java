package com.primihub.sdk.task.factory;

import com.google.protobuf.ByteString;
import com.primihub.sdk.task.cache.CacheService;
import com.primihub.sdk.task.param.TaskMPCParam;
import com.primihub.sdk.task.param.TaskParam;
import io.grpc.Channel;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AbstractMPCGRPCExecute extends AbstractGRPCExecuteFactory {

    private final static Logger log = LoggerFactory.getLogger(AbstractMPCGRPCExecute.class);

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
        runMPC(channel,taskParam);
    }

    private void runMPC(Channel channel, TaskParam<TaskMPCParam> taskParam){
        try {
            TaskMPCParam mpcParam = taskParam.getTaskContentParam();
            Common.Params.Builder paramsBuilder = Common.Params.newBuilder();
            Iterator<Map.Entry<String, Object>> mapIterator = mpcParam.getParamMap().entrySet().iterator();
            while (mapIterator.hasNext()){
                Map.Entry<String, Object> next = mapIterator.next();
                if (next.getValue() == null){
                    continue;
                }
                if (next.getValue() instanceof Integer){
                    paramsBuilder.putParamMap(next.getKey(),Common.ParamValue.newBuilder().setValueInt32((int)next.getValue()).build());
                }else if(next.getValue() instanceof String){
                    paramsBuilder.putParamMap(next.getKey(),Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(next.getValue().toString().getBytes(StandardCharsets.UTF_8))).build());
                }else if(next.getValue() instanceof List){
                    if (((List<?>) next.getValue()).size()==0){
                        paramsBuilder.putParamMap(next.getKey(),Common.ParamValue.newBuilder().build());
                    }else {
                        if (((List<?>) next.getValue()).get(0) instanceof String){
                            Common.string_array.Builder stringBuilder = Common.string_array.newBuilder();
                            for (Object o : ((List<?>) next.getValue())) {
                                stringBuilder.addValueStringArray(ByteString.copyFrom(o.toString().getBytes(StandardCharsets.UTF_8)));
                            }
                            paramsBuilder.putParamMap(next.getKey(),Common.ParamValue.newBuilder().setValueStringArray(stringBuilder).build());
                        }
                    }
                } else {
                    paramsBuilder.putParamMap(next.getKey(),Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(next.getValue().toString().getBytes(StandardCharsets.UTF_8))).build());
                }
            }
            Common.TaskContext taskBuild = assembleTaskContext(taskParam);
            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.ACTOR_TASK)
                    .setParams(paramsBuilder.build())
                    .setName(mpcParam.getTaskName())
                    .setCode(ByteString.copyFrom(mpcParam.getTaskCode().getBytes(StandardCharsets.UTF_8)))
                    .setLanguage(Common.Language.PROTO)
                    .setTaskInfo(taskBuild)
                    .putAllPartyDatasets(assembleModelMpcDatasets(mpcParam.getResourceIds()))
                    .build();
            PushTaskRequest request = PushTaskRequest.newBuilder()
                    .setIntendedWorkerId(ByteString.copyFrom("".getBytes(StandardCharsets.UTF_8)))
                    .setTask(task)
                    .setSequenceNumber(11)
                    .setClientProcessedUpTo(22)
                    .build();
            log.info("grpc PushTaskRequest :\n{}", request.toString());
            PushTaskReply reply = runVMNodeGrpc(o -> o.submitTask(request),channel);
            log.info("grpc :{}", reply.toString());
            if (reply.getRetCode()==0){
                taskParam.setPartyCount(reply.getPartyCount());
                if (taskParam.getOpenGetStatus()){
                    continuouslyObtainTaskStatus(channel,taskBuild,taskParam,reply.getPartyCount());
                }
            }else {
                taskParam.setSuccess(false);
                taskParam.setError(reply.getMsgInfo().toStringUtf8());
            }
        }catch (Exception e){
            taskParam.setSuccess(false);
            taskParam.setError(e.getMessage());
            log.info("grpc Exception:{}", e.getMessage());
            e.printStackTrace();
        }
        taskParam.setEnd(true);
    }
}
