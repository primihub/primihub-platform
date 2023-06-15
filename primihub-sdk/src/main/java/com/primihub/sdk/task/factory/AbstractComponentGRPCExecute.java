package com.primihub.sdk.task.factory;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.protobuf.ByteString;
import com.primihub.sdk.task.cache.CacheService;
import com.primihub.sdk.task.dataenum.ModelTypeEnum;
import com.primihub.sdk.task.param.TaskComponentParam;
import com.primihub.sdk.task.param.TaskParam;
import com.primihub.sdk.util.FreemarkerTemplate;
import io.grpc.Channel;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.Map;

public class AbstractComponentGRPCExecute extends AbstractGRPCExecuteFactory {

    private final static Logger log = LoggerFactory.getLogger(AbstractComponentGRPCExecute.class);

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
        runComponentTask(channel,taskParam);
    }

    private void runComponentTask(Channel channel, TaskParam<TaskComponentParam> taskParam){
        try {
            TaskComponentParam taskContentParam = taskParam.getTaskContentParam();
            if (taskContentParam.getModelType() == ModelTypeEnum.CLASSIFICATION_BINARY){
                taskContentParam.getFreemarkerMap().put("taskNNType","classification");
            }
            if (taskContentParam.getModelType() == ModelTypeEnum.REGRESSION_BINARY){
                taskContentParam.getFreemarkerMap().put("taskNNType","regression");
            }
            String freemarkerContent;
            if (StringUtils.isEmpty(taskContentParam.getTemplatesContent())){
                freemarkerContent = FreemarkerTemplate.getInstance().generateTemplateStr(taskContentParam.getFreemarkerMap(),taskContentParam.isInfer()?taskContentParam.getModelType().getInferFtlPath():taskContentParam.getModelType().getModelFtlPath());
            }else {
                freemarkerContent = taskContentParam.isUntreated()?FreemarkerTemplate.getInstance().generateTemplateStrFreemarkerContent(taskContentParam.isInfer()?taskContentParam.getModelType().getInferFtlPath():taskContentParam.getModelType().getModelFtlPath(),taskContentParam.getTemplatesContent(),taskContentParam.getFreemarkerMap()):taskContentParam.getTemplatesContent();
            }
            Common.ParamValue componentParamsParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(JSONObject.toJSONString(JSONObject.parseObject(freemarkerContent), SerializerFeature.WriteMapNullValue).getBytes(StandardCharsets.UTF_8))).build();
            Common.Params params = Common.Params.newBuilder()
                    .putParamMap("component_params", componentParamsParamValue)
                    .build();
            Map<String, Common.Dataset> values = assembleModelDatasets(taskContentParam.getFreemarkerMap());
            Common.TaskContext taskBuild = assembleTaskContext(taskParam);
            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.ACTOR_TASK)
                    .setParams(params)
                    .setName(taskParam.getTaskContentParam().getModelType().getTypeName())
                    .setLanguage(Common.Language.PYTHON)
                    .setTaskInfo(taskBuild)
                    .putAllPartyDatasets(values)
                    .build();
            PushTaskRequest request = PushTaskRequest.newBuilder()
                    .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                    .setTask(task)
                    .setSequenceNumber(11)
                    .setClientProcessedUpTo(22)
                    .build();
            log.info("grpc PushTaskRequest :\n{}", request.toString());
            PushTaskReply reply = runVMNodeGrpc(o -> o.submitTask(request),channel);
            log.info("grpc结果:{}", reply.toString());
            if (reply.getRetCode()==0){
                taskParam.setPartyCount(reply.getPartyCount());
                if (taskParam.getOpenGetStatus()){
                    continuouslyObtainTaskStatus(channel,taskBuild,taskParam,reply.getPartyCount());
                }
            }else {
                taskParam.setSuccess(false);
                taskParam.setError(reply.getMsgInfo().toStringUtf8());
            }
        } catch (Exception e) {
            taskParam.setSuccess(false);
            taskParam.setError(e.getMessage());
            log.info("grpc Exception:{}", e.getMessage());
            e.printStackTrace();
        }
    }

}
