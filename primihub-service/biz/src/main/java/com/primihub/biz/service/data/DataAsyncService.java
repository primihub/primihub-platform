package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
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
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentRelationReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.entity.data.req.DataModelAndComponentReq;
import com.primihub.biz.entity.data.vo.ModelProjectResourceVo;
import com.primihub.biz.entity.data.vo.ShareModelVo;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.repository.primarydb.data.DataPsiPrRepository;
import com.primihub.biz.repository.primarydb.data.DataReasoningPrRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.secondarydb.data.*;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.service.data.component.impl.StartComponentTaskServiceImpl;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.FreemarkerUtil;
import com.primihub.biz.util.ZipUtils;
import com.primihub.biz.util.crypt.DateUtil;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * psi 异步调用实现
 */
@Service
@Slf4j
public class DataAsyncService implements ApplicationContextAware {

    private static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context=applicationContext;
    }
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
    private FusionResourceService fusionResourceService;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;
    @Autowired
    private DataTaskRepository dataTaskRepository;
    @Autowired
    private DataProjectRepository dataProjectRepository;
    @Autowired
    private DataModelPrRepository dataModelPrRepository;
    @Autowired
    private DataReasoningPrRepository dataReasoningPrRepository;
    @Autowired
    private SingleTaskChannel singleTaskChannel;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    public BaseResultEntity executeBeanMethod(boolean isCheck,DataComponentReq req, ComponentTaskReq taskReq){
        String baenName = req.getComponentCode()+ DataConstant.COMPONENT_BEAN_NAME_SUFFIX;
        log.info("execute : {}",baenName);
        ComponentTaskService taskService = (ComponentTaskService)context.getBean(baenName);
        if (taskService==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,req.getComponentName()+"组件无实现方法");
        try {
            return isCheck?taskService.check(req,taskReq):taskService.runTask(req,taskReq);
        }catch (Exception e){
            log.info("ComponentCode:{} -- e:{}",req.getComponentCode(),e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,req.getComponentName()+"组件执行异常");
        }
    }



    @Async
    public void runModelTask(ComponentTaskReq req){
        log.info("start model task grpc modelId:{} modelName:{} end time:{}",req.getDataModel().getModelId(),req.getDataModel().getModelName(),System.currentTimeMillis());
        for (DataComponent dataComponent : req.getDataComponents()) {
            dataComponent.setModelId(req.getDataModelTask().getModelId());
            dataComponent.setTaskId(req.getDataModelTask().getTaskId());
            dataModelPrRepository.saveDataComponent(dataComponent);
        }
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
        dataTaskPrRepository.updateDataTask(req.getDataTask());
        Map<String, DataComponentReq> dataComponentReqMap = req.getModelComponentReq().getModelComponents().stream().collect(Collectors.toMap(DataComponentReq::getComponentCode, Function.identity()));
        req.getDataModelTask().setComponentJson(JSONObject.toJSONString(req.getDataComponents()));
        dataModelPrRepository.updateDataModelTask(req.getDataModelTask());
        for (DataComponent dataComponent : req.getDataComponents()) {
            if (req.getDataTask().getTaskState().equals(TaskStateEnum.FAIL.getStateType()))
                break;
            dataComponent.setStartTime(System.currentTimeMillis());
            dataComponent.setComponentState(2);
            req.getDataModelTask().setComponentJson(JSONObject.toJSONString(req.getDataComponents()));
            dataModelPrRepository.updateDataModelTask(req.getDataModelTask());
            dataComponent.setComponentState(1);
            executeBeanMethod(false, dataComponentReqMap.get(dataComponent.getComponentCode()), req);
            if(req.getDataTask().getTaskState().equals(TaskStateEnum.FAIL.getStateType()))
                dataComponent.setComponentState(3);
            dataComponent.setEndTime(System.currentTimeMillis());
            req.getDataModelTask().setComponentJson(JSONObject.toJSONString(req.getDataComponents()));
            dataModelPrRepository.updateDataModelTask(req.getDataModelTask());
        }
        req.getDataTask().setTaskEndTime(System.currentTimeMillis());
        dataTaskPrRepository.updateDataTask(req.getDataTask());
        log.info("end model task grpc modelId:{} modelName:{} end time:{}",req.getDataModel().getModelId(),req.getDataModel().getModelName(),System.currentTimeMillis());
        if (req.getDataTask().getTaskState() == TaskStateEnum.SUCCESS.getStateType()){
            log.info("Share model task modelId:{} modelName:{}",req.getDataModel().getModelId(),req.getDataModel().getModelName());
            ShareModelVo vo = new ShareModelVo();
            vo.setDataModel(req.getDataModel());
            vo.setDataTask(req.getDataTask());
            vo.setDataModelTask(req.getDataModelTask());
            vo.setDmrList(req.getDmrList());
            vo.setShareOrganId(req.getResourceList().stream().map(ModelProjectResourceVo::getOrganId).collect(Collectors.toList()));
            sendShareModelTask(vo);
        }
    }

    private String formatModelComponentJson(DataModelAndComponentReq params, Map<String, DataComponent> dataComponentMap){
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
    public void psiGrpcRun(DataPsiTask psiTask, DataPsi dataPsi){
        DataResource ownDataResource = dataResourceRepository.queryDataResourceById(dataPsi.getOwnResourceId());
        String resourceId,resourceColumnNameList;
        if (dataPsi.getOtherOrganId().equals(organConfiguration.getSysLocalOrganId())){
            DataResource otherDataResource = dataResourceRepository.queryDataResourceById(Long.parseLong(dataPsi.getOtherResourceId()));
            resourceId = StringUtils.isNotBlank(otherDataResource.getResourceFusionId())?otherDataResource.getResourceFusionId():otherDataResource.getResourceId().toString();
            resourceColumnNameList = otherDataResource.getFileHandleField();
        }else {
            BaseResultEntity dataResource = fusionResourceService.getDataResource(dataPsi.getServerAddress(), dataPsi.getOtherResourceId());
            if (dataResource.getCode()!=0)
                return;
            Map<String, Object> otherDataResource = (LinkedHashMap)dataResource.getResult();
            resourceId = otherDataResource.getOrDefault("resourceId","").toString();
            resourceColumnNameList = otherDataResource.getOrDefault("resourceColumnNameList","").toString();
        }

        Date date=new Date();
        StringBuilder sb=new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(DateUtil.formatDate(date,DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat())).append("/").append(psiTask.getTaskId()).append(".csv");
        psiTask.setFilePath(sb.toString());
        PushTaskReply reply = null;
        try {
            log.info("grpc run dataPsiId:{} - psiTaskId:{} - outputFilePath{} - time:{}",dataPsi.getId(),psiTask.getId(),psiTask.getFilePath(),System.currentTimeMillis());
            Common.ParamValue clientDataParamValue=Common.ParamValue.newBuilder().setValueString(ownDataResource.getResourceFusionId()).build();
            Common.ParamValue serverDataParamValue=Common.ParamValue.newBuilder().setValueString(resourceId).build();
            Common.ParamValue psiTypeParamValue=Common.ParamValue.newBuilder().setValueInt32(dataPsi.getOutputContent()).build();
            Common.ParamValue psiTagParamValue=Common.ParamValue.newBuilder().setValueInt32(dataPsi.getTag()).build();
            int clientIndex = Arrays.asList(ownDataResource.getFileHandleField().split(",")).indexOf(dataPsi.getOwnKeyword());
            Common.ParamValue clientIndexParamValue=Common.ParamValue.newBuilder().setValueInt32(clientIndex).build();
            int serverIndex = Arrays.asList(resourceColumnNameList.split(",")).indexOf(dataPsi.getOtherKeyword());
            Common.ParamValue serverIndexParamValue=Common.ParamValue.newBuilder().setValueInt32(serverIndex).build();
            Common.ParamValue outputFullFilenameParamValue=Common.ParamValue.newBuilder().setValueString(psiTask.getFilePath()).build();
            Common.Params params=Common.Params.newBuilder()
                    .putParamMap("clientData",clientDataParamValue)
                    .putParamMap("serverData",serverDataParamValue)
                    .putParamMap("psiType",psiTypeParamValue)
                    .putParamMap("psiTag",psiTagParamValue)
                    .putParamMap("clientIndex",clientIndexParamValue)
                    .putParamMap("serverIndex",serverIndexParamValue)
                    .putParamMap("outputFullFilename",outputFullFilenameParamValue)
                    .build();
            Common.Task task= Common.Task.newBuilder()
                    .setType(Common.TaskType.PSI_TASK)
                    .setParams(params)
                    .setName("testTask")
                    .setLanguage(Common.Language.PROTO)
                    .setCode("import sys;")
                    .setJobId(ByteString.copyFrom(dataPsi.getId().toString().getBytes(StandardCharsets.UTF_8)))
                    .setTaskId(ByteString.copyFrom(psiTask.getId().toString().getBytes(StandardCharsets.UTF_8)))
                    .addInputDatasets("clientData")
                    .addInputDatasets("serverData")
                    .build();
            log.info("grpc Common.Task : \n{}",task.toString());
            PushTaskRequest request=PushTaskRequest.newBuilder()
                    .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                    .setTask(task)
                    .setSequenceNumber(11)
                    .setClientProcessedUpTo(22)
                    .build();
            psiTask.setTaskState(2);
            dataPsiPrRepository.updateDataPsiTask(psiTask);
            reply = workGrpcClient.run(o -> o.submitTask(request));
            log.info("grpc结果:"+reply);
            DataPsiTask task1 = dataPsiRepository.selectPsiTaskById(psiTask.getId());
            if (task1.getTaskState()!=4){
                psiTask.setTaskState(1);
                dataPsiPrRepository.updateDataPsiTask(psiTask);
//                psiTaskOutputFileHandle(psiTask);
            }
        } catch (Exception e) {
            psiTask.setTaskState(3);
            dataPsiPrRepository.updateDataPsiTask(psiTask);
            log.info("grpc Exception:{}",e.getMessage());
        }
        log.info("grpc end dataPsiId:{} - psiTaskId:{} - outputFilePath{} - time:{}",dataPsi.getId(),psiTask.getId(),psiTask.getFilePath(),System.currentTimeMillis());
    }
    public void psiTaskOutputFileHandle(DataPsiTask task){
        if (task.getTaskState()!=1)
            return;
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
    public void pirGrpcTask(DataTask dataTask, String resourceId, String pirParam, Integer resourceRowsCount) {
        Date date = new Date();
        try {
            String formatDate = DateUtil.formatDate(date, DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat());
            StringBuilder sb = new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(formatDate).append("/").append(dataTask.getTaskIdName()).append(".csv");
            dataTask.setTaskResultPath(sb.toString());
            PushTaskReply reply = null;
            log.info("grpc run pirSubmitTask:{} - resourceId_fileId:{} - queryIndeies:{} - time:{}", sb.toString(), resourceId, pirParam, System.currentTimeMillis());
            Common.ParamValue queryIndeiesParamValue = Common.ParamValue.newBuilder().setValueString(pirParam).build();
            Common.ParamValue serverDataParamValue = Common.ParamValue.newBuilder().setValueString(resourceId).build();
            Common.ParamValue databaseSizeParamValue = Common.ParamValue.newBuilder().setValueString(resourceRowsCount.toString()).build();
            Common.ParamValue outputFullFilenameParamValue = Common.ParamValue.newBuilder().setValueString(sb.toString()).build();
            Common.Params params = Common.Params.newBuilder()
                    .putParamMap("queryIndeies", queryIndeiesParamValue)
                    .putParamMap("serverData", serverDataParamValue)
                    .putParamMap("databaseSize", databaseSizeParamValue)
                    .putParamMap("outputFullFilename", outputFullFilenameParamValue)
                    .build();
            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.PIR_TASK)
                    .setParams(params)
                    .setName("testTask")
                    .setLanguage(Common.Language.PROTO)
                    .setCode("import sys;")
                    .setJobId(ByteString.copyFrom(resourceId.toString().getBytes(StandardCharsets.UTF_8)))
                    .setTaskId(ByteString.copyFrom(resourceId.toString().getBytes(StandardCharsets.UTF_8)))
                    .addInputDatasets("serverData")
                    .build();
            log.info("grpc Common.Task :\n{}",task.toString());
            PushTaskRequest request = PushTaskRequest.newBuilder()
                    .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                    .setTask(task)
                    .setSequenceNumber(11)
                    .setClientProcessedUpTo(22)
                    .build();
            reply = workGrpcClient.run(o -> o.submitTask(request));
            if (reply.getRetCode()==0){
                dataTask.setTaskState(TaskStateEnum.SUCCESS.getStateType());
                dataTask.setTaskResultContent(FileUtil.getFileContent(dataTask.getTaskResultPath()));
            }else {
                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataTask.setTaskErrorMsg("运行失败:"+reply.getRetCode());
            }
            log.info("grpc end pirSubmitTask:{} - resourceId_fileId:{} - queryIndeies:{} - time:{} - reply:{}", sb.toString(), resourceId, pirParam, System.currentTimeMillis(),reply.toString());
        } catch (Exception e) {
            dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
            dataTask.setTaskErrorMsg(e.getMessage());
            log.info("grpc pirSubmitTask Exception:{}",e.getMessage());
        }
        dataTaskPrRepository.updateDataTask(dataTask);
    }
    public void sendShareModelTask(ShareModelVo shareModelVo){
        singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SPREAD_MODEL_DATA_TASK.getHandleType(),shareModelVo))).build());
    }

    public void deleteModel(ShareModelVo vo) {
        Long projectId = vo.getDataModel().getProjectId();
        DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(projectId, null);
        vo.setProjectId(dataProject.getProjectId());
        vo.setServerAddress(dataProject.getServerAddress());
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
                    if (org.apache.commons.lang.StringUtils.isNotBlank(taskReq.getDataModelTask().getPredictContent())){
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

    @Async
    public void runReasoning(DataReasoning dataReasoning,List<DataReasoningResource> dataReasoningResourceList){
        String resourceId = "";
        for (DataReasoningResource dataReasoningResource : dataReasoningResourceList) {
            if (dataReasoningResource.getParticipationIdentity() == 1){
                resourceId = dataReasoningResource.getResourceId();
            }
        }
        log.info(resourceId);
        DataTask dataTask = new DataTask();
        dataTask.setTaskIdName(UUID.randomUUID().toString());
        dataTask.setTaskName(dataReasoning.getReasoningName());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTask.setTaskType(TaskTypeEnum.REASONING.getTaskType());
        dataTask.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        dataTask.setTaskUserId(dataReasoning.getUserId());
        dataTaskPrRepository.saveDataTask(dataTask);
        dataReasoning.setRunTaskId(dataTask.getTaskId());
        dataReasoningPrRepository.updateDataReasoning(dataReasoning);
        Map<String,String> map = new HashMap<>();
        map.put(DataConstant.PYTHON_LABEL_DATASET,resourceId);
        String freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_HOMO_LR_INFER_PAHT, freeMarkerConfigurer, map);
        if (freemarkerContent != null) {
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
                Common.ParamValue modelFileNameParamValue = Common.ParamValue.newBuilder().setValueString(modelOutputPathDto.getModelFileName()).build();
                Common.ParamValue predictFileNameeParamValue = Common.ParamValue.newBuilder().setValueString(dataTask.getTaskResultPath()).build();
                Common.Params params = Common.Params.newBuilder()
                        .putParamMap("modelFileName", modelFileNameParamValue)
                        .putParamMap("predictFileName", predictFileNameeParamValue)
                        .build();
                Common.Task task = Common.Task.newBuilder()
                        .setType(Common.TaskType.ACTOR_TASK)
                        .setParams(params)
                        .setName("modelTask")
                        .setLanguage(Common.Language.PYTHON)
                        .setCodeBytes(ByteString.copyFrom(freemarkerContent.getBytes(StandardCharsets.UTF_8)))
                        .setJobId(ByteString.copyFrom(dataTask.getTaskIdName().getBytes(StandardCharsets.UTF_8)))
                        .setTaskId(ByteString.copyFrom(dataTask.getTaskIdName().getBytes(StandardCharsets.UTF_8)))
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
                    dataTask.setTaskState(TaskStateEnum.SUCCESS.getStateType());
                }else {
                    dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                    dataTask.setTaskErrorMsg("运行失败:"+reply.getRetCode());
                }
            } catch (Exception e) {
                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataTask.setTaskErrorMsg(e.getMessage());
                log.info("grpc Exception:{}", e.getMessage());
                e.printStackTrace();
            }
            dataReasoning.setReasoningState(dataTask.getTaskState());
        }
        dataTask.setTaskEndTime(System.currentTimeMillis());
        dataTaskPrRepository.updateDataTask(dataTask);
        dataReasoningPrRepository.updateDataReasoning(dataReasoning);
    }

}
