package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseFunctionHandleEntity;
import com.primihub.biz.entity.base.BaseFunctionHandleEnum;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.ModelStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.dto.ModelOutputPathDto;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.entity.data.vo.ModelProjectResourceVo;
import com.primihub.biz.entity.data.vo.ShareModelVo;
import com.primihub.biz.entity.sys.po.SysUser;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.repository.primarydb.data.*;
import com.primihub.biz.repository.secondarydb.data.*;
import com.primihub.biz.repository.secondarydb.sys.SysUserSecondarydbRepository;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.service.sys.SysEmailService;
import com.primihub.biz.util.DataUtil;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.FreemarkerUtil;
import com.primihub.biz.util.crypt.DateUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.primihub.biz.constant.DataConstant.PYTHON_GUEST_DATASET;
import static com.primihub.biz.constant.DataConstant.PYTHON_LABEL_DATASET;

/**
 * psi 异步调用实现
 */
@Service
@Slf4j
public class DataAsyncService implements ApplicationContextAware {

    private static ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private DataModelRepository dataModelRepository;
    @Autowired
    private DataPsiPrRepository dataPsiPrRepository;
    @Autowired
    private DataPsiRepository dataPsiRepository;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;
    @Autowired
    private DataTaskRepository dataTaskRepository;
    @Autowired
    private DataProjectRepository dataProjectRepository;
    @Autowired
    private DataProjectPrRepository dataProjectPrRepository;
    @Autowired
    private DataModelPrRepository dataModelPrRepository;
    @Autowired
    private DataReasoningPrRepository dataReasoningPrRepository;
    @Autowired
    private SingleTaskChannel singleTaskChannel;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private SysUserSecondarydbRepository sysUserSecondarydbRepository;
    @Autowired
    private SysEmailService sysEmailService;
    @Autowired
    private DataTaskMonitorService dataTaskMonitorService;


    public BaseResultEntity executeBeanMethod(boolean isCheck, DataComponentReq req, ComponentTaskReq taskReq) {
        String baenName = req.getComponentCode() + DataConstant.COMPONENT_BEAN_NAME_SUFFIX;
        log.info("execute : {}", baenName);
        try {
            ComponentTaskService taskService = (ComponentTaskService) context.getBean(baenName);
            if (taskService == null) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, req.getComponentName() + "组件无实现方法");
            }
            return isCheck ? taskService.check(req, taskReq) : taskService.runTask(req, taskReq);
        } catch (Exception e) {
            log.info("ComponentCode:{} -- e:{}", req.getComponentCode(), e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, req.getComponentName() + "组件执行异常");
        }
    }


    @Async
    public void runModelTask(ComponentTaskReq req) {
        log.info("start model task grpc modelId:{} modelName:{} end time:{}", req.getDataModel().getModelId(), req.getDataModel().getModelName(), System.currentTimeMillis());
        for (DataComponent dataComponent : req.getDataComponents()) {
            dataComponent.setModelId(req.getDataModelTask().getModelId());
            dataComponent.setTaskId(req.getDataModelTask().getTaskId());
            dataModelPrRepository.saveDataComponent(dataComponent);
        }
        try {
            Map<String, DataComponent> dataComponentMap = req.getDataComponents().stream().collect(Collectors.toMap(DataComponent::getComponentCode, Function.identity()));
            for (DataModelComponent dataModelComponent : req.getDataModelComponents()) {
                dataModelComponent.setModelId(req.getDataModelTask().getModelId());
                dataModelComponent.setTaskId(req.getDataModelTask().getTaskId());
                dataModelComponent.setInputComponentId(dataModelComponent.getInputComponentCode() == null ? null : dataComponentMap.get(dataModelComponent.getInputComponentCode()) == null ? null : dataComponentMap.get(dataModelComponent.getInputComponentCode()).getComponentId());
                dataModelComponent.setOutputComponentId(dataModelComponent.getInputComponentCode() == null ? null : dataComponentMap.get(dataModelComponent.getOutputComponentCode()) == null ? null : dataComponentMap.get(dataModelComponent.getOutputComponentCode()).getComponentId());
                dataModelPrRepository.saveDataModelComponent(dataModelComponent);
            }
            // 重新组装json
            req.getDataModel().setComponentJson(formatModelComponentJson(req.getModelComponentReq(), dataComponentMap));
            req.getDataModel().setIsDraft(ModelStateEnum.SAVE.getStateType());
            req.getDataTask().setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
            Map<String, DataComponentReq> dataComponentReqMap = req.getModelComponentReq().getModelComponents().stream().collect(Collectors.toMap(DataComponentReq::getComponentCode, Function.identity()));
            if (dataComponentReqMap.containsKey("jointStatistical")) {
                req.getDataTask().setTaskType(TaskTypeEnum.JOINT_STATISTICAL.getTaskType());
            }
            dataTaskPrRepository.updateDataTask(req.getDataTask());
            req.getDataModelTask().setComponentJson(JSONObject.toJSONString(req.getDataComponents()));
            dataModelPrRepository.updateDataModelTask(req.getDataModelTask());
            for (DataComponent dataComponent : req.getDataComponents()) {
                if (req.getDataTask().getTaskState().equals(TaskStateEnum.FAIL.getStateType())
                        || req.getDataTask().getTaskState().equals(TaskStateEnum.CANCEL.getStateType())) {
                    break;
                }
                dataComponent.setStartTime(System.currentTimeMillis());
                dataComponent.setComponentState(2);
                req.getDataModelTask().setComponentJson(JSONObject.toJSONString(req.getDataComponents()));
                dataModelPrRepository.updateDataModelTask(req.getDataModelTask());
                dataComponent.setComponentState(1);
                executeBeanMethod(false, dataComponentReqMap.get(dataComponent.getComponentCode()), req);
                if (req.getDataTask().getTaskState().equals(TaskStateEnum.FAIL.getStateType())) {
                    dataComponent.setComponentState(3);
                }
                dataComponent.setEndTime(System.currentTimeMillis());
                req.getDataModelTask().setComponentJson(JSONObject.toJSONString(req.getDataComponents()));
                dataModelPrRepository.updateDataModelTask(req.getDataModelTask());
            }
        } catch (Exception e) {
            req.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            log.info(e.getMessage());
            e.printStackTrace();
        }
        req.getDataTask().setTaskEndTime(System.currentTimeMillis());
        updateTaskState(req.getDataTask());
//        dataTaskPrRepository.updateDataTask(req.getDataTask());
        log.info("end model task grpc modelId:{} modelName:{} end time:{}", req.getDataModel().getModelId(), req.getDataModel().getModelName(), System.currentTimeMillis());
//        if (req.getDataTask().getTaskState().equals(TaskStateEnum.SUCCESS.getStateType())){
        log.info("Share model task modelId:{} modelName:{}", req.getDataModel().getModelId(), req.getDataModel().getModelName());
        ShareModelVo vo = new ShareModelVo();
        vo.setDataModel(req.getDataModel());
        vo.setDataTask(req.getDataTask());
        vo.setDataModelTask(req.getDataModelTask());
        vo.setDmrList(req.getDmrList());
        vo.setShareOrganId(req.getResourceList().stream().map(ModelProjectResourceVo::getOrganId).collect(Collectors.toList()));
        vo.setDerivationList(req.getDerivationList());
        sendShareModelTask(vo);
//        }
        sendModelTaskMail(req.getDataTask(), req.getDataModel().getProjectId());
        dataProjectPrRepository.updateDataProject(dataProjectRepository.selectDataProjectByProjectId(req.getDataModel().getProjectId(), null));
    }

    private String formatModelComponentJson(DataModelAndComponentReq params, Map<String, DataComponent> dataComponentMap) {
        for (DataComponentReq modelComponent : params.getModelComponents()) {
            modelComponent.setComponentId(dataComponentMap.get(modelComponent.getComponentCode()).getComponentId().toString());
            for (DataComponentRelationReq req : modelComponent.getInput()) {
                req.setComponentId(dataComponentMap.get(req.getComponentCode()).getComponentId().toString());
            }
            for (DataComponentRelationReq req : modelComponent.getOutput()) {
                req.setComponentId(dataComponentMap.get(req.getComponentCode()).getComponentId().toString());
            }
        }
        return JSONObject.toJSONString(params);
    }


    @Async
    public void psiGrpcRun(DataPsiTask psiTask, DataPsi dataPsi) {
        DataResource ownDataResource = dataResourceRepository.queryDataResourceById(dataPsi.getOwnResourceId());
        String resourceId, resourceColumnNameList;
        int available;
        if (dataPsi.getOtherOrganId().equals(organConfiguration.getSysLocalOrganId())) {
            DataResource otherDataResource = dataResourceRepository.queryDataResourceById(Long.parseLong(dataPsi.getOtherResourceId()));
            resourceId = StringUtils.isNotBlank(otherDataResource.getResourceFusionId()) ? otherDataResource.getResourceFusionId() : otherDataResource.getResourceId().toString();
            resourceColumnNameList = otherDataResource.getFileHandleField();
            available = otherDataResource.getResourceState();
        } else {
            BaseResultEntity dataResource = otherBusinessesService.getDataResource(dataPsi.getOtherResourceId());
            if (dataResource.getCode() != 0) {
                return;
            }
            Map<String, Object> otherDataResource = (LinkedHashMap) dataResource.getResult();
            resourceId = otherDataResource.getOrDefault("resourceId", "1").toString();
            resourceColumnNameList = otherDataResource.getOrDefault("resourceColumnNameList", "").toString();
            available = Integer.parseInt(otherDataResource.getOrDefault("available", "1").toString());
        }
        DataTask dataTask = new DataTask();
        dataTask.setTaskIdName(psiTask.getTaskId());
        dataTask.setTaskName(dataPsi.getResultName());
        dataTask.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        dataTask.setTaskType(TaskTypeEnum.PSI.getTaskType());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTaskPrRepository.saveDataTask(dataTask);
        psiTask.setTaskState(2);
        dataPsiPrRepository.updateDataPsiTask(psiTask);
        log.info("psi available:{}", available);
        if (available == 0) {
            Date date = new Date();
            StringBuilder sb = new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(DateUtil.formatDate(date, DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat())).append("/").append(psiTask.getTaskId()).append(".csv");
            psiTask.setFilePath(sb.toString());
            PushTaskReply reply = null;
            try {
                log.info("grpc run dataPsiId:{} - psiTaskId:{} - outputFilePath{} - time:{}", dataPsi.getId(), psiTask.getId(), psiTask.getFilePath(), System.currentTimeMillis());
                Common.ParamValue psiTypeParamValue = Common.ParamValue.newBuilder().setValueInt32(dataPsi.getOutputContent()).build();
                Common.ParamValue psiTagParamValue = Common.ParamValue.newBuilder().setValueInt32(dataPsi.getTag()).build();
                List<String> clientFields = Arrays.asList(ownDataResource.getFileHandleField().split(","));
                Common.int32_array.Builder clientFieldsBuilder = Common.int32_array.newBuilder();
                Arrays.stream(dataPsi.getOwnKeyword().split(",")).forEach(str -> {
                    clientFieldsBuilder.addValueInt32Array(clientFields.indexOf(str));
                });
                Common.ParamValue clientIndexParamValue = Common.ParamValue.newBuilder().setIsArray(true).setValueInt32Array(clientFieldsBuilder.build()).build();
                List<String> serverFields = Arrays.asList(resourceColumnNameList.split(","));
                Common.int32_array.Builder serverFieldsBuilder = Common.int32_array.newBuilder();
                Arrays.stream(dataPsi.getOtherKeyword().split(",")).forEach(str -> {
                    serverFieldsBuilder.addValueInt32Array(serverFields.indexOf(str));
                });
                Common.ParamValue serverIndexParamValue = Common.ParamValue.newBuilder().setIsArray(true).setValueInt32Array(serverFieldsBuilder.build()).build();
                Common.ParamValue outputFullFilenameParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(psiTask.getFilePath().getBytes(StandardCharsets.UTF_8))).build();
                Common.Params params = Common.Params.newBuilder()
                        .putParamMap("psiType", psiTypeParamValue)
                        .putParamMap("psiTag", psiTagParamValue)
                        .putParamMap("clientIndex", clientIndexParamValue)
                        .putParamMap("serverIndex", serverIndexParamValue)
                        .putParamMap("outputFullFilename", outputFullFilenameParamValue)
                        .build();
                Common.TaskContext taskBuild = Common.TaskContext.newBuilder().setJobId("1").setRequestId(String.valueOf(SnowflakeId.getInstance().nextId())).setTaskId(dataTask.getTaskIdName()).build();
                Common.Task task = Common.Task.newBuilder()
                        .setType(Common.TaskType.PSI_TASK)
                        .setParams(params)
                        .setName("psiTask")
                        .setTaskInfo(taskBuild)
                        .setLanguage(Common.Language.PROTO)
                        .putPartyDatasets("SERVER", Common.Dataset.newBuilder().putData("SERVER", resourceId).build())
                        .putPartyDatasets("CLIENT", Common.Dataset.newBuilder().putData("CLIENT", ownDataResource.getResourceFusionId()).build())
                        .build();
                log.info("grpc Common.Task : \n{}", task.toString());
                PushTaskRequest request = PushTaskRequest.newBuilder()
                        .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                        .setTask(task)
                        .setSequenceNumber(11)
                        .setClientProcessedUpTo(22)
                        .build();
                reply = workGrpcClient.run(o -> o.submitTask(request));
                log.info("grpc结果:" + reply);
                dataTaskMonitorService.continuouslyObtainTaskStatus(dataTask, taskBuild, reply.getPartyCount(), psiTask.getFilePath());
                DataPsiTask task1 = dataPsiRepository.selectPsiTaskById(psiTask.getId());
                psiTask.setTaskState(task1.getTaskState());
                if (task1.getTaskState() != 4) {
                    if (FileUtil.isFileExists(psiTask.getFilePath())) {
                        psiTask.setTaskState(1);
                    } else {
                        psiTask.setTaskState(3);
                    }
                }
            } catch (Exception e) {
                psiTask.setTaskState(3);
                log.info("grpc Exception:{}", e.getMessage());
                e.printStackTrace();
            }
            log.info("grpc end dataPsiId:{} - psiTaskId:{} - outputFilePath{} - time:{}", dataPsi.getId(), psiTask.getId(), psiTask.getFilePath(), System.currentTimeMillis());
        } else {
            psiTask.setTaskState(3);
        }
        dataPsiPrRepository.updateDataPsiTask(psiTask);
        dataTask.setTaskState(psiTask.getTaskState());
        dataTask.setTaskEndTime(System.currentTimeMillis());
        updateTaskState(dataTask);
    }

    public void psiTaskOutputFileHandle(DataPsiTask task) {
        if (task.getTaskState() != 1) {
            return;
        }
        List<String> fileContent = FileUtil.getFileContent(task.getFilePath(), null);
        StringBuilder sb = new StringBuilder();
        for (String line : fileContent) {
            sb.append(line).append("\r\n");
        }
        task.setFileContent(sb.toString());
        task.setFileRows(fileContent.size());
        dataPsiPrRepository.updateDataPsiTask(task);
    }

    @Async
    public void pirGrpcTask(DataTask dataTask, String resourceId, String pirParam) {
        Date date = new Date();
        try {
            String formatDate = DateUtil.formatDate(date, DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat());
            StringBuilder sb = new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(formatDate).append("/").append(dataTask.getTaskIdName()).append(".csv");
            dataTask.setTaskResultPath(sb.toString());
            PushTaskReply reply = null;
            log.info("grpc run pirSubmitTask:{} - resourceId_fileId:{} - queryIndeies:{} - time:{}", sb.toString(), resourceId, pirParam, System.currentTimeMillis());
            String[] paramSplit = pirParam.split(",");
            Common.string_array.Builder builder = Common.string_array.newBuilder();
            for (String str : paramSplit) {
                builder.addValueStringArray(ByteString.copyFrom(str.getBytes(StandardCharsets.UTF_8)));
            }
            Common.ParamValue clientDataParamValue = Common.ParamValue.newBuilder().setIsArray(true).setValueStringArray(builder).build();
            Common.ParamValue serverDataParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(resourceId.getBytes(StandardCharsets.UTF_8))).build();
            Common.ParamValue pirTagParamValue = Common.ParamValue.newBuilder().setValueInt32(1).build();
            Common.ParamValue outputFullFilenameParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(sb.toString().getBytes(StandardCharsets.UTF_8))).build();
            Common.Params params = Common.Params.newBuilder()
                    .putParamMap("clientData", clientDataParamValue)
                    .putParamMap("serverData", serverDataParamValue)
                    .putParamMap("pirType", pirTagParamValue)
                    .putParamMap("outputFullFilename", outputFullFilenameParamValue)
                    .build();
            Common.TaskContext taskBuild = Common.TaskContext.newBuilder().setJobId("1").setRequestId(String.valueOf(SnowflakeId.getInstance().nextId())).setTaskId(dataTask.getTaskIdName()).build();
            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.PIR_TASK)
                    .setParams(params)
                    .setName("testTask")
                    .setTaskInfo(taskBuild)
                    .setLanguage(Common.Language.PROTO)
//                    .setCode(ByteString.copyFrom("import sys;".getBytes(StandardCharsets.UTF_8)))
//                    .addInputDatasets("serverData")
                    .putPartyDatasets("SERVER", Common.Dataset.newBuilder().putData("SERVER", resourceId).build())
                    .build();
            log.info("grpc Common.Task :\n{}", task.toString());
            PushTaskRequest request = PushTaskRequest.newBuilder()
                    .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                    .setTask(task)
                    .setSequenceNumber(11)
                    .setClientProcessedUpTo(22)
                    .setSubmitClientId(ByteString.copyFrom(baseConfiguration.getGrpcClient().getGrpcClientPort().toString().getBytes(StandardCharsets.UTF_8)))
                    .build();
            reply = workGrpcClient.run(o -> o.submitTask(request));
            log.info(reply.toString());
            if (reply.getRetCode() == 0) {
                dataTaskMonitorService.continuouslyObtainTaskStatus(dataTask, taskBuild, reply.getPartyCount(), dataTask.getTaskResultPath());
            } else {
                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataTask.setTaskErrorMsg("运行失败:" + reply.getRetCode());
            }
            log.info("grpc end pirSubmitTask:{} - resourceId_fileId:{} - queryIndeies:{} - time:{} - reply:{}", sb.toString(), resourceId, pirParam, System.currentTimeMillis(), reply.toString());
        } catch (Exception e) {
            dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
            dataTask.setTaskErrorMsg(e.getMessage());
            log.info("grpc pirSubmitTask Exception:{}", e.getMessage());
            e.printStackTrace();
        }
        dataTask.setTaskEndTime(System.currentTimeMillis());
        updateTaskState(dataTask);
//        dataTaskPrRepository.updateDataTask(dataTask);
    }

    public void sendShareModelTask(ShareModelVo shareModelVo) {
        singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SPREAD_MODEL_DATA_TASK.getHandleType(), shareModelVo))).build());
    }

    public void deleteModel(ShareModelVo vo) {
        Long projectId = vo.getDataModel().getProjectId();
        DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(projectId, null);
        vo.setProjectId(dataProject.getProjectId());
        List<DataProjectOrgan> dataProjectOrgans = dataProjectRepository.selectDataProjcetOrganByProjectId(dataProject.getProjectId());
        vo.setShareOrganId(dataProjectOrgans.stream().map(DataProjectOrgan::getOrganId).collect(Collectors.toList()));
        sendShareModelTask(vo);
    }

    @Async
    public void deleteModelTask(DataTask dataTask) {
        DataModelTask modelTask = dataModelRepository.queryModelTaskById(dataTask.getTaskId());
        DataModel dataModel = dataModelRepository.queryDataModelById(modelTask.getModelId());
        DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(dataModel.getProjectId(), null);
        ShareModelVo vo = new ShareModelVo(dataProject);
        vo.setDataTask(dataTask);
        vo.setDataModelTask(modelTask);
        dataModel.setIsDel(1);
        vo.setDataModel(dataModel);
        List<DataProjectOrgan> dataProjectOrgans = dataProjectRepository.selectDataProjcetOrganByProjectId(dataProject.getProjectId());
        vo.setShareOrganId(dataProjectOrgans.stream().map(DataProjectOrgan::getOrganId).collect(Collectors.toList()));
        sendShareModelTask(vo);
    }


    /**
     * 运行推理
     *
     * @param dataReasoning             推理
     * @param dataReasoningResourceList 推理资源
     * @param modelTask                 推理所得
     */
    @Async
    public void runReasoning(DataReasoning dataReasoning, List<DataReasoningResource> dataReasoningResourceList, DataModelTask modelTask) {
        String labelDataset = "";    // 发起者资源
        String guestDataset = "";    // 协助方资源
        for (DataReasoningResource dataReasoningResource : dataReasoningResourceList) {
            if (dataReasoningResource.getParticipationIdentity() == 1) {
                labelDataset = dataReasoningResource.getResourceId();
            } else {
                guestDataset = dataReasoningResource.getResourceId();
            }
        }
        log.info("{}-{}", labelDataset, guestDataset);
        DataTask dataTask = new DataTask();
//        dataTask.setTaskIdName(UUID.randomUUID().toString());
        dataTask.setTaskIdName(Long.toString(SnowflakeId.getInstance().nextId()));
        dataTask.setTaskName(dataReasoning.getReasoningName());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTask.setTaskType(TaskTypeEnum.REASONING.getTaskType());
        dataTask.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        dataTask.setTaskUserId(dataReasoning.getUserId());
        dataTaskPrRepository.saveDataTask(dataTask);
        dataReasoning.setRunTaskId(dataTask.getTaskId());
        dataReasoning.setReasoningState(dataTask.getTaskState());
        dataReasoningPrRepository.updateDataReasoning(dataReasoning);
        Map<String, Object> map = new HashMap<>();
        map.put(PYTHON_LABEL_DATASET, labelDataset);  // 放入发起方资源
        List<DataComponent> dataComponents = JSONArray.parseArray(modelTask.getComponentJson(), DataComponent.class);
        DataComponent model = dataComponents.stream().filter(dataComponent -> "model".equals(dataComponent.getComponentCode())).findFirst().orElse(null);
        if (model == null) {
            dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
            dataTask.setTaskErrorMsg("未能获取到模型信息");
        } else {
            List<DataComponentValue> dataComponentValue = JSONArray.parseArray(model.getDataJson(), DataComponentValue.class);
            DataComponentValue modelType = dataComponentValue.stream().filter(d -> "modelType".equals(d.getKey())).findFirst().orElse(null);
            if (modelType == null || StringUtils.isBlank(modelType.getVal())) {
                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataTask.setTaskErrorMsg("未能获取到模型类型信息");
            } else {
                map.put(PYTHON_GUEST_DATASET, guestDataset);  // 放入合作方资源
                grpc(dataReasoning, dataTask, modelType.getVal(), map);
                //String freemarkerContent = "";
                /*if ("2".equals(modelType.getVal())){
                 *//*freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_HOMO_XGB_INFER_PATH, freeMarkerConfigurer, map);
                    grpc(dataReasoning,dataTask,freemarkerContent,modelType.getVal());*//*
                }else if(modelType.getVal().equals("5")){
                    *//*freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_HETERO_LR_INFER_PATH, freeMarkerConfigurer, map);
                    grpc(dataReasoning,dataTask,freemarkerContent,modelType.getVal());*//*
                }else if(modelType.getVal().equals("6")||modelType.getVal().equals("7")){
                    *//*freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_HOMO_NN_BINARY_INFER_PATH, freeMarkerConfigurer, map);
                    grpc(dataReasoning,dataTask,freemarkerContent,modelType.getVal());*//*
                }else{
                    Common.ParamValue hostModelFileNameParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(modelOutputPathDto.getHostModelFileName().getBytes(StandardCharsets.UTF_8))).build();
                    map.put("hostModelFileName", dataTask.getTaskResultPath());
                    freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_HOMO_LR_INFER_PATH, freeMarkerConfigurer, map);
                    grpc(dataReasoning,dataTask,freemarkerContent,modelType.getVal());
                }*/
            }
        }

        dataTask.setTaskEndTime(System.currentTimeMillis());
        dataTaskPrRepository.updateDataTask(dataTask);
        dataReasoningPrRepository.updateDataReasoning(dataReasoning);
    }

    public void sendModelTaskMail(DataTask dataTask, Long projectId) {
        if (!dataTask.getTaskState().equals(TaskStateEnum.FAIL.getStateType())) {
            return;
        }
        if (StringUtils.isBlank(baseConfiguration.getTaskEmailSubject())) {
            return;
        }
        SysUser sysUser = sysUserSecondarydbRepository.selectSysUserByUserId(dataTask.getTaskUserId());
        if (sysUser == null) {
            log.info("task_id:{} The task email was not sent. Reason for not sending : No user information", dataTask.getTaskIdName());
            return;
        }
        if (!DataUtil.isEmail(sysUser.getUserAccount())) {
            log.info("task_id:{} The task email was not sent. Reason for not sending : The user account is not an email address", dataTask.getTaskIdName());
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("尊敬的【");
        sb.append(sysUser.getUserName());
        sb.append("】您在【");
        sb.append(DateUtil.formatDate(dataTask.getCreateDate(), DateUtil.DateStyle.TIME_FORMAT_NORMAL.getFormat()));
        sb.append("】使用【");
        sb.append(sysUser.getUserAccount());
        sb.append("】创建的任务已失败\n");
        if (StringUtils.isNotBlank(dataTask.getTaskName())) {
            sb.append("任务名称：【").append(dataTask.getTaskName()).append("】\n");
        }
        sb.append("任务ID：【").append(dataTask.getTaskIdName()).append("】\n");
        if (StringUtils.isNotBlank(baseConfiguration.getSystemDomainName())) {
            sb.append("<a href=\"").append(baseConfiguration.getSystemDomainName()).append("/#/project/detail/").append(projectId).append("/task/").append(dataTask.getTaskId());
            sb.append("\">").append("点击查询任务详情").append("</a>");
        }
        sysEmailService.send(sysUser.getUserAccount(), baseConfiguration.getTaskEmailSubject(), sb.toString());
    }

    public void updateTaskState(DataTask dataTask) {
        DataTask rawDataTask = dataTaskRepository.selectDataTaskByTaskId(dataTask.getTaskId());
        if (rawDataTask.getTaskState().equals(TaskStateEnum.CANCEL.getStateType())) {
            dataTask.setTaskState(TaskStateEnum.CANCEL.getStateType());
        } else {
            dataTaskPrRepository.updateDataTask(dataTask);
        }
    }



    /**
     * @param dataReasoning     推理实体
     * @param dataTask          推理任务
     * @param freemarkerContent 推理模板
     * @param modelType         推理类型
     */
    public void grpc(DataReasoning dataReasoning, DataTask dataTask, String freemarkerContent,String modelType,int size){
        try {
            log.info(freemarkerContent);
            DataTask modelTask = dataTaskRepository.selectDataTaskByTaskId(dataReasoning.getTaskId());
            log.info(modelTask.toString());
            log.info(modelTask.getTaskResultContent());
            ModelOutputPathDto modelOutputPathDto = JSONObject.parseObject(modelTask.getTaskResultContent(), ModelOutputPathDto.class);
            log.info(modelOutputPathDto.toString());
            StringBuilder filePath = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix()).append(dataTask.getTaskIdName()).append("/outfile.csv");
            dataTask.setTaskResultPath(filePath.toString());
            log.info(dataTask.getTaskResultPath());
            Common.ParamValue modelFileNameParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(modelOutputPathDto.getHostModelFileName().getBytes(StandardCharsets.UTF_8))).build();
            Common.ParamValue predictFileNameeParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(dataTask.getTaskResultPath().getBytes(StandardCharsets.UTF_8))).build();
            Common.Params params = null;
            if ("2".equals(modelType)){
                Common.ParamValue hostLookupTableParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(modelOutputPathDto.getHostLookupTable().getBytes(StandardCharsets.UTF_8))).build();
                Common.ParamValue guestLookupTableParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(modelOutputPathDto.getGuestLookupTable().getBytes(StandardCharsets.UTF_8))).build();
                params = Common.Params.newBuilder()
                        .putParamMap("modelFileName", modelFileNameParamValue)
                        .putParamMap("predictFileName", predictFileNameeParamValue)
                        .putParamMap("hostLookupTable", hostLookupTableParamValue)
                        .putParamMap("guestLookupTable", guestLookupTableParamValue)
                        .build();
            }else {
                params = Common.Params.newBuilder()
                        .putParamMap("modelFileName", modelFileNameParamValue)
                        .putParamMap("predictFileName", predictFileNameeParamValue)
                        .build();
            }

            Common.TaskContext taskBuild = Common.TaskContext.newBuilder().setJobId("1").setRequestId(String.valueOf(SnowflakeId.getInstance().nextId())).setTaskId(dataTask.getTaskIdName()).build();
            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.ACTOR_TASK)
                    .setParams(params)
                    .setName("modelTask")
                    .setTaskInfo(taskBuild)
                    .setLanguage(Common.Language.PYTHON)
//                    .setCode(ByteString.copyFrom(freemarkerContent.getBytes(StandardCharsets.UTF_8)))
                    .build();
            log.info("grpc Common.Task :\n{}", task.toString());
            PushTaskRequest request = PushTaskRequest.newBuilder()
                    .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                    .setTask(task)
                    .setSequenceNumber(11)
                    .setClientProcessedUpTo(22)
                    //.setSubmitClientId(ByteString.copyFrom(baseConfiguration.getGrpcClient().getGrpcClientPort().toString().getBytes(StandardCharsets.UTF_8)))
                    .build();
            PushTaskReply reply = workGrpcClient.run(o -> o.submitTask(request));
            log.info("grpc结果:{}", reply.toString());
            if (reply.getRetCode() == 0) {
                dataTaskMonitorService.continuouslyObtainTaskStatus(dataTask, taskBuild, reply.getPartyCount(), dataTask.getTaskResultPath());
                if (dataTask.getTaskState().equals(TaskStateEnum.SUCCESS.getStateType())) {
                    dataReasoning.setReleaseDate(new Date());
                }
            } else {
                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataTask.setTaskErrorMsg("运行失败:" + reply.getRetCode());
            }
        } catch (Exception e) {
            dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
            dataTask.setTaskErrorMsg(e.getMessage());
            log.info("grpc Exception:{}", e.getMessage());
            e.printStackTrace();
        }
        dataReasoning.setReasoningState(dataTask.getTaskState());
    }

    private void grpc(DataReasoning dataReasoning, DataTask dataTask, String modelType, Map<String, Object> map) {
        // 推理任务
        DataTask modelTask = dataTaskRepository.selectDataTaskByTaskId(dataReasoning.getTaskId());
        log.info(modelTask.toString());
        log.info(modelTask.getTaskResultContent());
        // 推理模板参数
        ModelOutputPathDto modelOutputPathDto = JSONObject.parseObject(modelTask.getTaskResultContent(), ModelOutputPathDto.class);
        log.info("模板参数-------"+modelOutputPathDto.toString());
        String freemarkerContent = "";
        Map<String, Common.Dataset> values = new HashMap<>();
        values.put("Bob",Common.Dataset.newBuilder().putData("data_set",map.get(PYTHON_LABEL_DATASET).toString()).build());
        if ("2".equals(modelType)) {
            map.put("indicatorFileName", modelOutputPathDto.getIndicatorFileName());
            map.put("predictFileName", modelOutputPathDto.getPredictFileName());
            map.put("hostModelFileName", modelOutputPathDto.getHostModelFileName());
            map.put("guestModelFileName", modelOutputPathDto.getGuestModelFileName());
            map.put("hostLookupTable", modelOutputPathDto.getHostLookupTable());
            map.put("guestLookupTable", modelOutputPathDto.getGuestLookupTable());
            values.put("Charlie",Common.Dataset.newBuilder().putData("data_set",map.get(PYTHON_GUEST_DATASET).toString()).build());
            freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_HOMO_XGB_INFER_PATH, freeMarkerConfigurer, map);
        } else if ("5".equals(modelType)) {
            map.put("indicatorFileName", modelOutputPathDto.getIndicatorFileName());
            map.put("predictFileName", modelOutputPathDto.getPredictFileName());
            map.put("hostModelFileName", modelOutputPathDto.getHostModelFileName());
            map.put("guestModelFileName", modelOutputPathDto.getGuestModelFileName());
            values.put("Charlie",Common.Dataset.newBuilder().putData("data_set",map.get(PYTHON_GUEST_DATASET).toString()).build());
            freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_HETERO_LR_INFER_PATH, freeMarkerConfigurer, map);
        } else if ("6".equals(modelType) || "7".equals(modelType)) {
            map.put("hostModelFileName", modelOutputPathDto.getHostModelFileName());
            map.put("predictFileName", modelOutputPathDto.getPredictFileName());
            freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_HOMO_NN_BINARY_INFER_PATH, freeMarkerConfigurer, map);
        } else {
            map.put("hostModelFileName", modelOutputPathDto.getHostModelFileName());
            map.put("predictFileName", modelOutputPathDto.getPredictFileName());
            freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_HOMO_LR_INFER_PATH, freeMarkerConfigurer, map);
        }
        try {
            Common.ParamValue componentParamsParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(JSONObject.toJSONString(JSONObject.parseObject(freemarkerContent), SerializerFeature.WriteMapNullValue).getBytes(StandardCharsets.UTF_8))).build();
            Common.Params params = Common.Params.newBuilder()
                    .putParamMap("component_params", componentParamsParamValue)
                    .build();
            Common.TaskContext taskBuild = Common.TaskContext.newBuilder().setJobId("1").setRequestId(String.valueOf(SnowflakeId.getInstance().nextId())).setTaskId(dataTask.getTaskIdName()).build();
            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.ACTOR_TASK)
                    .setParams(params)
                    .setName("modelTask")
                    .setTaskInfo(taskBuild)
                    .setLanguage(Common.Language.PYTHON)
                    .putAllPartyDatasets(values)
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
                dataTaskMonitorService.continuouslyObtainTaskStatus(dataTask,taskBuild,reply.getPartyCount(),dataTask.getTaskResultPath());
                if (dataTask.getTaskState().equals(TaskStateEnum.SUCCESS.getStateType())){
                    dataTask.setTaskResultPath(modelOutputPathDto.getPredictFileName());
                    dataReasoning.setReleaseDate(new Date());
                    dataTaskPrRepository.updateDataTask(dataTask);
                }
            }else {
                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataTask.setTaskErrorMsg("运行失败:"+reply.getRetCode());
                dataTaskPrRepository.updateDataTask(dataTask);
            }
        } catch (Exception e) {
            dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
            dataTask.setTaskErrorMsg(e.getMessage());
            dataTaskPrRepository.updateDataTask(dataTask);
            log.info("grpc Exception:{}", e.getMessage());
            e.printStackTrace();
        }
        dataReasoning.setReasoningState(dataTask.getTaskState());
    }
}
