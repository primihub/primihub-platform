package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.ComponentsConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.ModelTypeEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dto.ModelDerivationDto;
import com.primihub.biz.entity.data.dto.ModelOutputPathDto;
import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectOrgan;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.entity.data.req.DataFResourceReq;
import com.primihub.biz.entity.data.vo.ModelProjectResourceVo;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.service.data.DataTaskMonitorService;
import com.primihub.biz.service.data.OtherBusinessesService;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.FreemarkerUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import primihub.rpc.Common;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("modelComponentTaskServiceImpl")
@Slf4j
public class ModelComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
    @Autowired
    private ComponentsConfiguration componentsConfiguration;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private DataProjectRepository dataProjectRepository;
    @Autowired
    private DataTaskMonitorService dataTaskMonitorService;

    @Override
    public BaseResultEntity check(DataComponentReq req,  ComponentTaskReq taskReq) {
        BaseResultEntity baseResultEntity = componentTypeVerification(req, componentsConfiguration.getModelComponents(), taskReq);
        if (baseResultEntity.getCode()!=0) {
            return baseResultEntity;
        }
        ModelTypeEnum modelType = ModelTypeEnum.MODEL_TYPE_MAP.get(Integer.valueOf(taskReq.getValueMap().get("modelType")));
        taskReq.getDataModel().setTrainType(modelType.getTrainType());
        taskReq.getDataModel().setModelType(modelType.getType());
        if (modelType.getType().equals(ModelTypeEnum.TRANSVERSE_LR.getType())|| modelType.getType().equals(ModelTypeEnum.REGRESSION_BINARY.getType()) || modelType.getType().equals(ModelTypeEnum.CLASSIFICATION_BINARY.getType()) ){
            String arbiterOrgan = taskReq.getValueMap().get("arbiterOrgan");
            log.info(arbiterOrgan);
            if (StringUtils.isBlank(arbiterOrgan)) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"可信第三方选择不可以为空");
            }
            DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(taskReq.getDataModel().getProjectId(), null);
            List<DataProjectOrgan> dataProjectOrgans = dataProjectRepository.selectDataProjcetOrganByProjectId(dataProject.getProjectId());
            if (dataProjectOrgans.size()<=2) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"项目参与方少于3方");
            }
            List<ModelProjectResourceVo> resourceLists = JSONObject.parseArray(taskReq.getValueMap().get("selectData"), ModelProjectResourceVo.class);
            Set<String> organIdSet = resourceLists.stream().map(ModelProjectResourceVo::getOrganId).collect(Collectors.toSet());
            log.info(organIdSet.toString());
            if (organIdSet.contains(arbiterOrgan)) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"可信第三方不可以和数据集机构重复");
            }
            DataFResourceReq fresourceReq = new DataFResourceReq();
            fresourceReq.setOrganId(arbiterOrgan);
            BaseResultEntity resourceList = otherBusinessesService.getResourceList(fresourceReq);
            if (resourceList.getCode()!=0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"可信第三方检索失败-代码1");
            }
            LinkedHashMap<String,Object> data = (LinkedHashMap<String,Object>)resourceList.getResult();
            List<LinkedHashMap<String,Object>> resourceDataList = (List<LinkedHashMap<String,Object>>)data.get("data");
            if (resourceDataList==null || resourceDataList.size()==0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"可信第三方检索失败-代码2");
            }
            taskReq.getFreemarkerMap().put(DataConstant.PYTHON_ARBITER_DATASET,resourceDataList.get(0).get("resourceId").toString());
            taskReq.getFusionResourceList().add(resourceDataList.get(0));
        }
        return baseResultEntity;
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        if (Integer.valueOf(taskReq.getValueMap().get("modelType")).equals(ModelTypeEnum.MPC_LR.getType())){
            return mpclr(req,taskReq);
        }
        taskReq.getFreemarkerMap().putAll(getComponentVals(req.getComponentValues()));
        if (taskReq.getNewest()!=null && taskReq.getNewest().size()!=0){
            log.info("newest:{}",JSONObject.toJSONString(taskReq.getNewest()));
            log.info("freemarkerMap1:{}",JSONObject.toJSONString(taskReq.getFreemarkerMap()));
            Map<String, ModelDerivationDto> derivationMap = taskReq.getNewest().stream().collect(Collectors.toMap(ModelDerivationDto::getOriginalResourceId, Function.identity()));
            Iterator<Map.Entry<String, String>> iterator = taskReq.getFreemarkerMap().entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, String> next = iterator.next();
                if (derivationMap.containsKey(next.getValue())){
                    String newDataSetId = derivationMap.get(next.getValue()).getNewResourceId();
                    taskReq.getFreemarkerMap().put(next.getKey(),newDataSetId);
                }
            }
            log.info("freemarkerMap2:{}",JSONObject.toJSONString(taskReq.getFreemarkerMap()));
        }
        taskReq.getFreemarkerMap().put("feature_names","None");
        if (StringUtils.isNotBlank(taskReq.getFreemarkerMap().get(DataConstant.PYTHON_CALCULATION_FIELD))){
            String field = taskReq.getFreemarkerMap().get(DataConstant.PYTHON_CALCULATION_FIELD);
            log.info("field:{}",field);
            taskReq.getFreemarkerMap().put("feature_names",field);
        }
        Integer modelType = Integer.valueOf(taskReq.getValueMap().get("modelType"));
        log.info("modelType:{}",modelType);
        if (modelType.equals(ModelTypeEnum.V_XGBOOST.getType())){
            return xgb(req,taskReq);
        }else if (modelType.equals(ModelTypeEnum.TRANSVERSE_LR.getType())
                ||modelType.equals(ModelTypeEnum.HETERO_LR.getType())
                ||modelType.equals(ModelTypeEnum.REGRESSION_BINARY.getType())
                ||modelType.equals(ModelTypeEnum.CLASSIFICATION_BINARY.getType())){
            return lr(req,taskReq,ModelTypeEnum.MODEL_TYPE_MAP.get(modelType));
        }
        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
        taskReq.getDataTask().setTaskErrorMsg("运行失败:无法进行任务执行");
        return BaseResultEntity.success();
    }

    private BaseResultEntity mpclr(DataComponentReq req, ComponentTaskReq taskReq){
        try {
            List<String> resourceIds = new ArrayList<>();
            if (taskReq.getNewest()!=null && taskReq.getNewest().size()!=0){
                resourceIds.addAll(taskReq.getNewest().stream().map(ModelDerivationDto::getNewResourceId).collect(Collectors.toSet()));
            }else {
                resourceIds.addAll(taskReq.getResourceList().stream().map(ModelProjectResourceVo::getResourceId).collect(Collectors.toSet()));
            }
            String jobId = String.valueOf(taskReq.getJob());
            StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix()).append(taskReq.getDataTask().getTaskIdName());
            ModelOutputPathDto outputPathDto = new ModelOutputPathDto(baseSb.toString());
            taskReq.getDataTask().setTaskResultContent(JSONObject.toJSONString(outputPathDto));
            int batchSize = 128;
            int numlters = 5;
            try {
                if (taskReq.getValueMap().containsKey("batchSize")){
                    batchSize = Integer.parseInt(taskReq.getValueMap().get("batchSize"));
                }
                if (taskReq.getValueMap().containsKey("numlters")){
                    numlters = Integer.parseInt(taskReq.getValueMap().get("numlters"));
                }
            }catch (Exception e){
                log.info(e.getMessage());
            }
            Common.ParamValue batchSizeParamValue = Common.ParamValue.newBuilder().setValueInt32(batchSize).build();
            Common.ParamValue numItersParamValue = Common.ParamValue.newBuilder().setValueInt32(numlters).build();
            Common.ParamValue modelNameeParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(outputPathDto.getHostModelFileName().getBytes(StandardCharsets.UTF_8))).build();
            Common.Params params = Common.Params.newBuilder()
                    .putParamMap("BatchSize", batchSizeParamValue)
                    .putParamMap("NumIters", numItersParamValue)
                    .putParamMap("modelName", modelNameeParamValue)
                    .build();
            Map<String, Common.Dataset> values = new HashMap<>();
            for (int i = 0; i < resourceIds.size(); i++) {
                values.put("PARTY"+i,Common.Dataset.newBuilder().putData("Data_File",resourceIds.get(i)).build());
            }
            Common.TaskContext taskBuild = Common.TaskContext.newBuilder().setJobId(jobId).setRequestId(String.valueOf(SnowflakeId.getInstance().nextId())).setTaskId(taskReq.getDataTask().getTaskIdName()).build();
            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.ACTOR_TASK)
                    .setParams(params)
                    .setName("logistic_regression")
                    .setCode(ByteString.copyFrom("logistic_regression".getBytes(StandardCharsets.UTF_8)))
                    .setLanguage(Common.Language.PROTO)
                    .setTaskInfo(taskBuild)
                    .putAllPartyDatasets(values)
                    .build();
            PushTaskRequest request = PushTaskRequest.newBuilder()
                    .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                    .setTask(task)
                    .setSequenceNumber(11)
                    .setClientProcessedUpTo(22)
                    .setSubmitClientId(ByteString.copyFrom(baseConfiguration.getGrpcClient().getGrpcClientPort().toString().getBytes(StandardCharsets.UTF_8)))
                    .build();
            log.info("grpc PushTaskRequest :\n{}", request.toString());
            PushTaskReply reply = workGrpcClient.run(o -> o.submitTask(request));
            log.info("grpc结果:{}", reply.toString());
            if (reply.getRetCode()==0){
                dataTaskMonitorService.continuouslyObtainTaskStatus(taskReq.getDataTask(),taskBuild,reply.getPartyCount(),null);
                if (!TaskStateEnum.SUCCESS.getStateType().equals(taskReq.getDataTask().getTaskState())){
                    File sourceFile = new File(baseSb.toString());
                    if (sourceFile.isDirectory()){
                        File[] files = sourceFile.listFiles();
                        if (files!=null){
                            for (File file : files) {
                                if (file.getName().contains("modelFileName")){
                                    taskReq.getDataTask().setTaskState(TaskStateEnum.SUCCESS.getStateType());
                                    taskReq.getDataTask().setTaskErrorMsg("");
                                    break;
                                }
                            }
                        }else {
                            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:目录文件夹中无文件");
                        }
                    }else {
                        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                        taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:目录文件夹中无文件");
                    }
                }
            }else {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:"+reply.getMsgInfo().toStringUtf8());
            }
        } catch (Exception e) {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:"+e.getMessage());
            log.info("grpc Exception:{}", e.getMessage());
            e.printStackTrace();
        }
        return BaseResultEntity.success();
    }
    private BaseResultEntity lr(DataComponentReq req, ComponentTaskReq taskReq,ModelTypeEnum modelType) {
        log.info("------- 执行任务, modelType:{}", modelType);
        StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix()).append(taskReq.getDataTask().getTaskIdName());
        ModelOutputPathDto outputPathDto = new ModelOutputPathDto(baseSb.toString());
        putPath(outputPathDto,taskReq);
        if (6 == modelType.getType().intValue()){
            // 横向 -NN (分类)
            taskReq.getFreemarkerMap().put("taskNNType","classification");
        } else if (7 == modelType.getType().intValue()){
            // 横向 -NN (回归)
            taskReq.getFreemarkerMap().put("taskNNType","regression");
        }
        String freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(modelType.getFtlName(), freeMarkerConfigurer,new HashMap<String, Object>(taskReq.getFreemarkerMap()));
        if (freemarkerContent != null) {
            try {
                String jobId = String.valueOf(taskReq.getJob());
                taskReq.getDataTask().setTaskResultContent(JSONObject.toJSONString(outputPathDto));
                taskReq.getDataModelTask().setPredictFile(outputPathDto.getIndicatorFileName());
                Common.ParamValue componentParamsParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(JSONObject.toJSONString(JSONObject.parseObject(freemarkerContent),SerializerFeature.WriteMapNullValue).getBytes(StandardCharsets.UTF_8))).build();
                Common.Params params = Common.Params.newBuilder()
                        .putParamMap("component_params", componentParamsParamValue)
                        .build();
                Map<String, Common.Dataset> values = new HashMap<>();
                if (StringUtils.isNotBlank(taskReq.getFreemarkerMap().get("label_dataset"))){
                    values.put("Bob",Common.Dataset.newBuilder().putData("data_set",taskReq.getFreemarkerMap().get("label_dataset")).build());
                }
                if (StringUtils.isNotBlank(taskReq.getFreemarkerMap().get("guest_dataset"))){
                    values.put("Charlie",Common.Dataset.newBuilder().putData("data_set",taskReq.getFreemarkerMap().get("guest_dataset")).build());
                }
                if (StringUtils.isNotBlank(taskReq.getFreemarkerMap().get("arbiter_dataset"))){
                    values.put("Alice",Common.Dataset.newBuilder().putData("data_set",taskReq.getFreemarkerMap().get("arbiter_dataset")).build());
                }
                Common.TaskContext taskBuild = Common.TaskContext.newBuilder().setJobId(jobId).setRequestId(String.valueOf(SnowflakeId.getInstance().nextId())).setTaskId(taskReq.getDataTask().getTaskIdName()).build();
                Common.Task task = Common.Task.newBuilder()
                        .setType(Common.TaskType.ACTOR_TASK)
                        .setParams(params)
                        .setName("modelTask")
                        .setLanguage(Common.Language.PYTHON)
                        .setTaskInfo(taskBuild)
                        .putAllPartyDatasets(values)
                        .build();
                PushTaskRequest request = PushTaskRequest.newBuilder()
                        .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                        .setTask(task)
                        .setSequenceNumber(11)
                        .setClientProcessedUpTo(22)
                        .setSubmitClientId(ByteString.copyFrom(baseConfiguration.getGrpcClient().getGrpcClientPort().toString().getBytes(StandardCharsets.UTF_8)))
                        .build();
                log.info("grpc PushTaskRequest :\n{}", request.toString());
                PushTaskReply reply = workGrpcClient.run(o -> o.submitTask(request));
                log.info("grpc结果:{}", reply.toString());
                if (reply.getRetCode()==0){
                    dataTaskMonitorService.continuouslyObtainTaskStatus(taskReq.getDataTask(),taskBuild,reply.getPartyCount(),null);
                    if (taskReq.getDataTask().getTaskState().equals(TaskStateEnum.SUCCESS.getStateType())){
                        File sourceFile = new File(baseSb.toString());
                        if (sourceFile.isDirectory()){
                            File[] files = sourceFile.listFiles();
                            if (files!=null){
                                for (File file : files) {
                                    if (file.getName().contains("modelFileName")){
                                        taskReq.getDataModelTask().setPredictContent(FileUtil.getFileContent(taskReq.getDataModelTask().getPredictFile()));
                                        taskReq.getDataTask().setTaskState(TaskStateEnum.SUCCESS.getStateType());
                                        taskReq.getDataTask().setTaskErrorMsg("");
                                        break;
                                    }
                                }
                            }else {
                                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:目录文件夹中无文件");
                            }
                        }else {
                            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:目录文件夹中无文件");
                        }
                    }
                }else {
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:"+reply.getMsgInfo().toStringUtf8());
                }
            } catch (Exception e) {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:"+e.getMessage());
                log.info("grpc Exception:{}", e.getMessage());
                e.printStackTrace();
            }
        }else {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:创建模板失败null");
        }
        return BaseResultEntity.success();
    }

    public void putPath(ModelOutputPathDto outputPathDto,ComponentTaskReq taskReq){
        taskReq.getDataModelTask().setPredictFile(outputPathDto.getIndicatorFileName());
        taskReq.getFreemarkerMap().put("predictFileName",outputPathDto.getPredictFileName());
        taskReq.getFreemarkerMap().put("indicatorFileName",outputPathDto.getIndicatorFileName());
        taskReq.getFreemarkerMap().put("hostLookupTable",outputPathDto.getHostLookupTable());
        taskReq.getFreemarkerMap().put("guestLookupTable",outputPathDto.getGuestLookupTable());
        taskReq.getFreemarkerMap().put("hostModelFileName",outputPathDto.getHostModelFileName());
        taskReq.getFreemarkerMap().put("guestModelFileName",outputPathDto.getGuestModelFileName());
    }

    public BaseResultEntity xgb(DataComponentReq req, ComponentTaskReq taskReq){
        StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix()).append(taskReq.getDataTask().getTaskIdName());
        ModelOutputPathDto outputPathDto = new ModelOutputPathDto(baseSb.toString());
        putPath(outputPathDto,taskReq);
        taskReq.getDataTask().setTaskResultContent(JSONObject.toJSONString(outputPathDto));
        String freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_EN_PATH, freeMarkerConfigurer, new HashMap<>(taskReq.getFreemarkerMap()));
        if (freemarkerContent != null) {
            try {
                String jobId = String.valueOf(taskReq.getJob());
                Common.ParamValue componentParamsParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(JSONObject.toJSONString(JSONObject.parseObject(freemarkerContent),SerializerFeature.WriteMapNullValue).getBytes(StandardCharsets.UTF_8))).build();
                Common.Params params = Common.Params.newBuilder()
                        .putParamMap("component_params", componentParamsParamValue)
                        .build();
                Map<String, Common.Dataset> values = new HashMap<>();
                values.put("Bob",Common.Dataset.newBuilder().putData("data_set",taskReq.getFreemarkerMap().get("label_dataset")).build());
                values.put("Charlie",Common.Dataset.newBuilder().putData("data_set",taskReq.getFreemarkerMap().get("guest_dataset")).build());
                Common.TaskContext taskBuild = Common.TaskContext.newBuilder().setJobId(jobId).setRequestId(String.valueOf(SnowflakeId.getInstance().nextId())).setTaskId(taskReq.getDataTask().getTaskIdName()).build();
                Common.Task task = Common.Task.newBuilder()
                        .setType(Common.TaskType.ACTOR_TASK)
                        .setParams(params)
                        .setName("modelTask")
                        .setLanguage(Common.Language.PYTHON)
                        .setTaskInfo(taskBuild)
                        .putAllPartyDatasets(values)
                        .build();
                PushTaskRequest request = PushTaskRequest.newBuilder()
                        .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                        .setTask(task)
                        .setSequenceNumber(11)
                        .setClientProcessedUpTo(22)
                        .setSubmitClientId(ByteString.copyFrom(baseConfiguration.getGrpcClient().getGrpcClientPort().toString().getBytes(StandardCharsets.UTF_8)))
                        .build();
                log.info("grpc PushTaskRequest :\n{}", request.toString());
                PushTaskReply reply = workGrpcClient.run(o -> o.submitTask(request));
                log.info("grpc结果:{}", reply.toString());
                if (reply.getRetCode()==0){
                    dataTaskMonitorService.continuouslyObtainTaskStatus(taskReq.getDataTask(),taskBuild,reply.getPartyCount(),null);
                    if (taskReq.getDataTask().getTaskState().equals(TaskStateEnum.SUCCESS.getStateType())){
                        File sourceFile = new File(baseSb.toString());
                        if (sourceFile.isDirectory()){
                            File[] files = sourceFile.listFiles();
                            if (files!=null){
                                for (File file : files) {
                                    if (file.getName().contains("modelFileName")){
                                        taskReq.getDataModelTask().setPredictContent(FileUtil.getFileContent(taskReq.getDataModelTask().getPredictFile()));
                                        taskReq.getDataTask().setTaskState(TaskStateEnum.SUCCESS.getStateType());
                                        taskReq.getDataTask().setTaskErrorMsg("");
                                        break;
                                    }
                                }
                            }else {
                                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:目录文件夹中无文件");
                            }
                        }else {
                            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:目录文件夹中无文件");
                        }
                    }
                }else {
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:"+reply.getMsgInfo().toStringUtf8());
                }
            } catch (Exception e) {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:"+e.getMessage());
                log.info("grpc Exception:{}", e.getMessage());
                e.printStackTrace();
            }
        }else {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:创建模板失败null");
        }
        return BaseResultEntity.success();
    }
}
