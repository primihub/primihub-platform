package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.ModelTypeEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dto.ModelOutputPathDto;
import com.primihub.biz.entity.data.po.DataModel;
import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectOrgan;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.entity.data.req.DataFResourceReq;
import com.primihub.biz.entity.data.vo.ModelProjectResourceVo;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.service.data.FusionResourceService;
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
import java.util.*;
import java.util.stream.Collectors;

@Service("modelComponentTaskServiceImpl")
@Slf4j
public class ModelComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private DataProjectRepository dataProjectRepository;

    @Override
    public BaseResultEntity check(DataComponentReq req,  ComponentTaskReq taskReq) {
        BaseResultEntity baseResultEntity = componentTypeVerification(req, baseConfiguration.getModelComponents(), taskReq);
        if (baseResultEntity.getCode()!=0)
            return baseResultEntity;
        ModelTypeEnum modelType = ModelTypeEnum.MODEL_TYPE_MAP.get(Integer.valueOf(taskReq.getValueMap().get("modelType")));
        taskReq.getDataModel().setTrainType(modelType.getTrainType());
        taskReq.getDataModel().setModelType(modelType.getType());
        if (modelType.getType().equals(ModelTypeEnum.TRANSVERSE_LR.getType())){
            String arbiterOrgan = taskReq.getValueMap().get("arbiterOrgan");
            if (arbiterOrgan==null)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"横向LR模型 可信第三方选择不可以为空");
            DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(taskReq.getDataModel().getProjectId(), null);
            List<DataProjectOrgan> dataProjectOrgans = dataProjectRepository.selectDataProjcetOrganByProjectId(dataProject.getProjectId());
            if (dataProjectOrgans.size()<=2)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"项目参与方少于3方");
            List<ModelProjectResourceVo> resourceLists = JSONObject.parseArray(taskReq.getValueMap().get("selectData"), ModelProjectResourceVo.class);
            Set<String> organIdSet = resourceLists.stream().map(ModelProjectResourceVo::getOrganId).collect(Collectors.toSet());
            if (organIdSet.contains(arbiterOrgan))
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"可信第三方不可以和数据集机构重复");
            DataFResourceReq fresourceReq = new DataFResourceReq();
            fresourceReq.setOrganId(arbiterOrgan);
            fresourceReq.setServerAddress(dataProject.getServerAddress());
            BaseResultEntity resourceList = fusionResourceService.getResourceList(fresourceReq);
            if (resourceList.getCode()!=0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"可信第三方检索失败-代码1");
            LinkedHashMap<String,Object> data = (LinkedHashMap<String,Object>)resourceList.getResult();
            List<LinkedHashMap<String,Object>> resourceDataList = (List<LinkedHashMap<String,Object>>)data.get("data");
            if (resourceDataList==null || resourceDataList.size()==0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"可信第三方检索失败-代码2");
            taskReq.getFreemarkerMap().put(DataConstant.PYTHON_ARBITER_DATASET,resourceDataList.get(0).get("resourceId").toString());
        }
        return baseResultEntity;
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        if (Integer.valueOf(taskReq.getValueMap().get("modelType")).equals(ModelTypeEnum.V_XGBOOST.getType())){
            return xgb(req,taskReq);
        }else if (Integer.valueOf(taskReq.getValueMap().get("modelType")).equals(ModelTypeEnum.TRANSVERSE_LR.getType())){
            return lr(req,taskReq);
        }
        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
        taskReq.getDataTask().setTaskErrorMsg("运行失败:无法进行任务执行");
        return BaseResultEntity.success();
    }

    private BaseResultEntity lr(DataComponentReq req, ComponentTaskReq taskReq) {
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

    public BaseResultEntity xgb(DataComponentReq req, ComponentTaskReq taskReq){
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
