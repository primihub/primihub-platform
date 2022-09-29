package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.dataenum.ModelTypeEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dto.ModelOutputPathDto;
import com.primihub.biz.entity.data.po.DataModel;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.FreemarkerUtil;
import com.primihub.biz.util.ZipUtils;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;

@Service("modelComponentTaskServiceImpl")
@Slf4j
public class ModelComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Override
    public BaseResultEntity check(DataComponentReq req,  ComponentTaskReq taskReq) {
        BaseResultEntity baseResultEntity = componentTypeVerification(req, baseConfiguration.getModelComponents(), taskReq);
        if (baseResultEntity.getCode()!=0)
            return baseResultEntity;
        ModelTypeEnum modelType = ModelTypeEnum.MODEL_TYPE_MAP.get(Integer.valueOf(taskReq.getValueMap().get("modelType")));
        taskReq.getDataModel().setTrainType(modelType.getTrainType());
        return baseResultEntity;
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        if (taskReq.getValueMap().get("modelType").equals("2")){
            return portraitXGB(req,taskReq);
        }else if (taskReq.getValueMap().get("modelType").equals("3")){
            return transverseXGB(req,taskReq);
        }
        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
        taskReq.getDataTask().setTaskErrorMsg("运行失败:无法进行任务执行");
        return BaseResultEntity.success();
    }

    private BaseResultEntity transverseXGB(DataComponentReq req, ComponentTaskReq taskReq) {
        String freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_HOMO_LR_PAHT, freeMarkerConfigurer, taskReq.getFreemarkerMap());
        if (freemarkerContent != null) {
            try {
                StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix()).append(taskReq.getDataTask().getTaskIdName());
                ModelOutputPathDto outputPathDto = new ModelOutputPathDto(baseSb.toString());
                taskReq.getDataTask().setTaskResultContent(JSONObject.toJSONString(outputPathDto));
                taskReq.getDataModelTask().setPredictFile(outputPathDto.getIndicatorFileName());
                Common.ParamValue predictFileNameParamValue = Common.ParamValue.newBuilder().setValueString(outputPathDto.getPredictFileName()).build();
                Common.ParamValue modelFileNameParamValue = Common.ParamValue.newBuilder().setValueString(outputPathDto.getModelFileName()).build();
                Common.Params params = Common.Params.newBuilder()
                        .putParamMap("predictFileName", predictFileNameParamValue)
                        .putParamMap("modelFileName", modelFileNameParamValue)
                        .build();
                Common.Task task = Common.Task.newBuilder()
                        .setType(Common.TaskType.ACTOR_TASK)
                        .setParams(params)
                        .setName("modelTask")
                        .setLanguage(Common.Language.PYTHON)
                        .setCodeBytes(ByteString.copyFrom(freemarkerContent.getBytes(StandardCharsets.UTF_8)))
                        .setJobId(ByteString.copyFrom(taskReq.getDataTask().getTaskIdName().getBytes(StandardCharsets.UTF_8)))
                        .setTaskId(ByteString.copyFrom(taskReq.getDataTask().getTaskIdName().getBytes(StandardCharsets.UTF_8)))
                        .build();
                log.info("grpc Common.Task :\n{}", task.toString());
                PushTaskRequest request = PushTaskRequest.newBuilder()
                        .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                        .setTask(task)
                        .setSequenceNumber(11)
                        .setClientProcessedUpTo(22)
                        .build();
                PushTaskReply reply = workGrpcClient.run(o -> o.submitTask(request));
                log.info("grpc结果:{}", reply.toString());
                if (reply.getRetCode()==0){
                    taskReq.getDataModelTask().setPredictContent(FileUtil.getFileContent(taskReq.getDataModelTask().getPredictFile()));
                    if (StringUtils.isNotBlank(taskReq.getDataModelTask().getPredictContent())){
                        taskReq.getDataTask().setTaskState(TaskStateEnum.SUCCESS.getStateType());
                        log.info("zip -- modelId:{} -- taskId:{} -- start",taskReq.getDataModel().getModelId(),taskReq.getDataTask().getTaskIdName());
                        ZipUtils.pathFileTOZipFile(outputPathDto.getTaskPath(),outputPathDto.getModelRunZipFilePath(),new HashSet<String>(){{add("guestLookupTable");add("indicatorFileName.json");}});
                        log.info("zip -- modelId:{} -- taskId:{} -- end",taskReq.getDataModel().getModelId(),taskReq.getDataTask().getTaskIdName());
                    }else {
                        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                        taskReq.getDataTask().setTaskErrorMsg("运行失败:无文件信息");
                    }
                }else {
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg("运行失败:"+reply.getRetCode());
                }
            } catch (Exception e) {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(e.getMessage());
                log.info("grpc Exception:{}", e.getMessage());
            }
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity portraitXGB(DataComponentReq req, ComponentTaskReq taskReq){
        String freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_EN_PAHT, freeMarkerConfigurer, taskReq.getFreemarkerMap());
        if (freemarkerContent != null) {
            try {
                StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix()).append(taskReq.getDataTask().getTaskIdName());
                ModelOutputPathDto outputPathDto = new ModelOutputPathDto(baseSb.toString());
                taskReq.getDataTask().setTaskResultContent(JSONObject.toJSONString(outputPathDto));
                taskReq.getDataModelTask().setPredictFile(outputPathDto.getIndicatorFileName());
                Common.ParamValue predictFileNameParamValue = Common.ParamValue.newBuilder().setValueString(outputPathDto.getPredictFileName()).build();
                Common.ParamValue indicatorFileNameParamValue = Common.ParamValue.newBuilder().setValueString(outputPathDto.getIndicatorFileName()).build();
                Common.ParamValue hostLookupTableParamValue = Common.ParamValue.newBuilder().setValueString(outputPathDto.getHostLookupTable()).build();
                Common.ParamValue guestLookupTableParamValue = Common.ParamValue.newBuilder().setValueString(outputPathDto.getGuestLookupTable()).build();
                Common.ParamValue modelFileNameParamValue = Common.ParamValue.newBuilder().setValueString(outputPathDto.getModelFileName()).build();
                Common.Params params = Common.Params.newBuilder()
                        .putParamMap("predictFileName", predictFileNameParamValue)
                        .putParamMap("indicatorFileName", indicatorFileNameParamValue)
                        .putParamMap("hostLookupTable", hostLookupTableParamValue)
                        .putParamMap("guestLookupTable", guestLookupTableParamValue)
                        .putParamMap("modelFileName", modelFileNameParamValue)
                        .build();
                Common.Task task = Common.Task.newBuilder()
                        .setType(Common.TaskType.ACTOR_TASK)
                        .setParams(params)
                        .setName("modelTask")
                        .setLanguage(Common.Language.PYTHON)
                        .setCodeBytes(ByteString.copyFrom(freemarkerContent.getBytes(StandardCharsets.UTF_8)))
                        .setJobId(ByteString.copyFrom(taskReq.getDataTask().getTaskIdName().getBytes(StandardCharsets.UTF_8)))
                        .setTaskId(ByteString.copyFrom(taskReq.getDataTask().getTaskIdName().getBytes(StandardCharsets.UTF_8)))
                        .build();
                log.info("grpc Common.Task :\n{}", task.toString());
                PushTaskRequest request = PushTaskRequest.newBuilder()
                        .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                        .setTask(task)
                        .setSequenceNumber(11)
                        .setClientProcessedUpTo(22)
                        .build();
                PushTaskReply reply = workGrpcClient.run(o -> o.submitTask(request));
                log.info("grpc结果:{}", reply.toString());
                if (reply.getRetCode()==0){
                    taskReq.getDataModelTask().setPredictContent(FileUtil.getFileContent(taskReq.getDataModelTask().getPredictFile()));
                    if (StringUtils.isNotBlank(taskReq.getDataModelTask().getPredictContent())){
                        taskReq.getDataTask().setTaskState(TaskStateEnum.SUCCESS.getStateType());
                        log.info("zip -- modelId:{} -- taskId:{} -- start",taskReq.getDataModel().getModelId(),taskReq.getDataTask().getTaskIdName());
                        ZipUtils.pathFileTOZipFile(outputPathDto.getTaskPath(),outputPathDto.getModelRunZipFilePath(),new HashSet<String>(){{add("guestLookupTable");add("indicatorFileName.json");}});
                        log.info("zip -- modelId:{} -- taskId:{} -- end",taskReq.getDataModel().getModelId(),taskReq.getDataTask().getTaskIdName());
                    }else {
                        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                        taskReq.getDataTask().setTaskErrorMsg("运行失败:无文件信息");
                    }
                }else {
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg("运行失败:"+reply.getRetCode());
                }
            } catch (Exception e) {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(e.getMessage());
                log.info("grpc Exception:{}", e.getMessage());
            }
        }
        return BaseResultEntity.success();
    }
}
