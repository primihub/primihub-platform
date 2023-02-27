package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.constant.RedisKeyConstant;
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
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.repository.primarydb.data.DataPsiPrRepository;
import com.primihub.biz.repository.primarydb.data.DataReasoningPrRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
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
    @Autowired
    private SysUserSecondarydbRepository sysUserSecondarydbRepository;
    @Autowired
    private SysEmailService sysEmailService;
    @Autowired
    private DataTaskMonitorService dataTaskMonitorService;



    public BaseResultEntity executeBeanMethod(boolean isCheck,DataComponentReq req, ComponentTaskReq taskReq){
        String baenName = req.getComponentCode()+ DataConstant.COMPONENT_BEAN_NAME_SUFFIX;
        log.info("execute : {}",baenName);
        try {
            ComponentTaskService taskService = (ComponentTaskService)context.getBean(baenName);
            if (taskService==null)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,req.getComponentName()+"组件无实现方法");
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
            dataTaskPrRepository.updateDataTask(req.getDataTask());
            Map<String, DataComponentReq> dataComponentReqMap = req.getModelComponentReq().getModelComponents().stream().collect(Collectors.toMap(DataComponentReq::getComponentCode, Function.identity()));
            req.getDataModelTask().setComponentJson(JSONObject.toJSONString(req.getDataComponents()));
            dataModelPrRepository.updateDataModelTask(req.getDataModelTask());
            for (DataComponent dataComponent : req.getDataComponents()) {
                if (req.getDataTask().getTaskState().equals(TaskStateEnum.FAIL.getStateType())
                        || req.getDataTask().getTaskState().equals(TaskStateEnum.CANCEL.getStateType()))
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
                ShareModelVo vo = new ShareModelVo();
                vo.setDataModel(req.getDataModel());
                vo.setDataTask(req.getDataTask());
                vo.setDataModelTask(req.getDataModelTask());
                vo.setDmrList(req.getDmrList());
                vo.setShareOrganId(req.getResourceList().stream().map(ModelProjectResourceVo::getOrganId).collect(Collectors.toList()));
                vo.setDerivationList(req.getDerivationList());
                sendShareModelTask(vo);
            }
        }catch (Exception e){
            req.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            log.info(e.getMessage());
            e.printStackTrace();
        }
        req.getDataTask().setTaskEndTime(System.currentTimeMillis());
        updateTaskState(req.getDataTask());
//        dataTaskPrRepository.updateDataTask(req.getDataTask());
        log.info("end model task grpc modelId:{} modelName:{} end time:{}",req.getDataModel().getModelId(),req.getDataModel().getModelName(),System.currentTimeMillis());
        sendModelTaskMail(req.getDataTask(),req.getDataModel().getProjectId());
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
        int available;
        if (dataPsi.getOtherOrganId().equals(organConfiguration.getSysLocalOrganId())){
            DataResource otherDataResource = dataResourceRepository.queryDataResourceById(Long.parseLong(dataPsi.getOtherResourceId()));
            resourceId = StringUtils.isNotBlank(otherDataResource.getResourceFusionId())?otherDataResource.getResourceFusionId():otherDataResource.getResourceId().toString();
            resourceColumnNameList = otherDataResource.getFileHandleField();
            available = otherDataResource.getResourceState();
        }else {
            BaseResultEntity dataResource = fusionResourceService.getDataResource(dataPsi.getServerAddress(), dataPsi.getOtherResourceId());
            if (dataResource.getCode()!=0)
                return;
            Map<String, Object> otherDataResource = (LinkedHashMap)dataResource.getResult();
            resourceId = otherDataResource.getOrDefault("resourceId","1").toString();
            resourceColumnNameList = otherDataResource.getOrDefault("resourceColumnNameList","").toString();
            available = Integer.parseInt(otherDataResource.getOrDefault("available","1").toString());
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
        log.info("psi available:{}",available);
        if (available==0){
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
                List<String> clientFields = Arrays.asList(ownDataResource.getFileHandleField().split(","));
                Common.int32_array.Builder clientFieldsBuilder = Common.int32_array.newBuilder();
                Arrays.stream(dataPsi.getOwnKeyword().split(",")).forEach(str -> {
                    clientFieldsBuilder.addValueInt32Array(clientFields.indexOf(str));
                });
                Common.ParamValue clientIndexParamValue=Common.ParamValue.newBuilder().setIsArray(true).setValueInt32Array(clientFieldsBuilder.build()).build();
                List<String> serverFields = Arrays.asList(resourceColumnNameList.split(","));
                Common.int32_array.Builder serverFieldsBuilder = Common.int32_array.newBuilder();
                Arrays.stream(dataPsi.getOtherKeyword().split(",")).forEach(str -> {
                    serverFieldsBuilder.addValueInt32Array(serverFields.indexOf(str));
                });
                Common.ParamValue serverIndexParamValue=Common.ParamValue.newBuilder().setIsArray(true).setValueInt32Array(serverFieldsBuilder.build()).build();
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
                        .setCode(ByteString.copyFrom("import sys;".getBytes(StandardCharsets.UTF_8)))
                        .setJobId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                        .setTaskId(ByteString.copyFrom(dataTask.getTaskIdName().getBytes(StandardCharsets.UTF_8)))
                        .addInputDatasets("clientData")
                        .addInputDatasets("serverData")
                        .build();
                log.info("grpc Common.Task : \n{}",task.toString());
                PushTaskRequest request=PushTaskRequest.newBuilder()
                        .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                        .setTask(task)
                        .setSequenceNumber(11)
                        .setClientProcessedUpTo(22)
                        .setSubmitClientId(ByteString.copyFrom(baseConfiguration.getGrpcClient().getGrpcClientPort().toString().getBytes(StandardCharsets.UTF_8)))
                        .build();
                reply = workGrpcClient.run(o -> o.submitTask(request));
                log.info("grpc结果:"+reply);
                dataTaskMonitorService.verifyWhetherTheTaskIsSuccessfulAgain(dataTask, "1",2,psiTask.getFilePath());
                DataPsiTask task1 = dataPsiRepository.selectPsiTaskById(psiTask.getId());
                psiTask.setTaskState(task1.getTaskState());
                if (task1.getTaskState()!=4){
                    if (FileUtil.isFileExists(psiTask.getFilePath())){
                        psiTask.setTaskState(1);
                    }else {
                        psiTask.setTaskState(3);
                    }
                }
            } catch (Exception e) {
                psiTask.setTaskState(3);
                log.info("grpc Exception:{}",e.getMessage());
            }
            log.info("grpc end dataPsiId:{} - psiTaskId:{} - outputFilePath{} - time:{}",dataPsi.getId(),psiTask.getId(),psiTask.getFilePath(),System.currentTimeMillis());
        }else {
            psiTask.setTaskState(3);
        }
        dataPsiPrRepository.updateDataPsiTask(psiTask);
        dataTask.setTaskState(psiTask.getTaskState());
        updateTaskState(dataTask);
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
    public void pirGrpcTask(DataTask dataTask, String resourceId, String pirParam) {
        Date date = new Date();
        try {
            String formatDate = DateUtil.formatDate(date, DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat());
            StringBuilder sb = new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(formatDate).append("/").append(dataTask.getTaskIdName()).append(".csv");
            dataTask.setTaskResultPath(sb.toString());
            PushTaskReply reply = null;
            log.info("grpc run pirSubmitTask:{} - resourceId_fileId:{} - queryIndeies:{} - time:{}", sb.toString(), resourceId, pirParam, System.currentTimeMillis());
            Common.ParamValue clientDataParamValue = Common.ParamValue.newBuilder().setIsArray(true).setValueString(pirParam).build();
            Common.ParamValue serverDataParamValue = Common.ParamValue.newBuilder().setValueString(resourceId).build();
            Common.ParamValue pirTagParamValue = Common.ParamValue.newBuilder().setValueInt32(1).build();
            Common.ParamValue outputFullFilenameParamValue = Common.ParamValue.newBuilder().setValueString(sb.toString()).build();
            Common.Params params = Common.Params.newBuilder()
                    .putParamMap("clientData", clientDataParamValue)
                    .putParamMap("serverData", serverDataParamValue)
                    .putParamMap("pirType", pirTagParamValue)
                    .putParamMap("outputFullFilename", outputFullFilenameParamValue)
                    .build();
            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.PIR_TASK)
                    .setParams(params)
                    .setName("testTask")
                    .setLanguage(Common.Language.PROTO)
                    .setCode(ByteString.copyFrom("import sys;".getBytes(StandardCharsets.UTF_8)))
                    .setJobId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                    .setTaskId(ByteString.copyFrom(dataTask.getTaskIdName().getBytes(StandardCharsets.UTF_8)))
                    .addInputDatasets("serverData")
                    .build();
            log.info("grpc Common.Task :\n{}",task.toString());
            PushTaskRequest request = PushTaskRequest.newBuilder()
                    .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                    .setTask(task)
                    .setSequenceNumber(11)
                    .setClientProcessedUpTo(22)
                    .setSubmitClientId(ByteString.copyFrom(baseConfiguration.getGrpcClient().getGrpcClientPort().toString().getBytes(StandardCharsets.UTF_8)))
                    .build();
            reply = workGrpcClient.run(o -> o.submitTask(request));
            log.info(reply.toString());
            if (reply.getRetCode()==0){
                dataTaskMonitorService.verifyWhetherTheTaskIsSuccessfulAgain(dataTask, "1",2,dataTask.getTaskResultPath());
//                dataTask.setTaskState(TaskStateEnum.SUCCESS.getStateType());
//                dataTask.setTaskResultContent(FileUtil.getFileContent(dataTask.getTaskResultPath()));
//                if (!FileUtil.isFileExists(dataTask.getTaskResultPath())){
//                    dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
//                    dataTask.setTaskErrorMsg("运行失败:无文件信息");
//                }
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
        dataTask.setTaskEndTime(System.currentTimeMillis());
        updateTaskState(dataTask);
//        dataTaskPrRepository.updateDataTask(dataTask);
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


    @Async
    public void runReasoning(DataReasoning dataReasoning,List<DataReasoningResource> dataReasoningResourceList, DataModelTask modelTask){
        String labelDataset = "";
        String guestDataset = "";
        for (DataReasoningResource dataReasoningResource : dataReasoningResourceList) {
            if (dataReasoningResource.getParticipationIdentity() == 1){
                labelDataset = dataReasoningResource.getResourceId();
            }else {
                guestDataset = dataReasoningResource.getResourceId();
            }
        }
        log.info("{}-{}",labelDataset,guestDataset);
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
        Map<String,String> map = new HashMap<>();
        map.put(DataConstant.PYTHON_LABEL_DATASET,labelDataset);
        List<DataComponent> dataComponents = JSONArray.parseArray(modelTask.getComponentJson(), DataComponent.class);
        DataComponent model = dataComponents.stream().filter(dataComponent -> dataComponent.getComponentCode().equals("model")).findFirst().orElse(null);
        if (model==null){
            dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
            dataTask.setTaskErrorMsg("未能获取到模型信息");
        }else {
            List<DataComponentValue> dataComponentValue = JSONArray.parseArray(model.getDataJson(), DataComponentValue.class);
            DataComponentValue modelType = dataComponentValue.stream().filter(d -> d.getKey().equals("modelType")).findFirst().orElse(null);
            if (modelType==null || StringUtils.isBlank(modelType.getVal())){
                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataTask.setTaskErrorMsg("未能获取到模型类型信息");
            }else {
                Long[] portNumber = getPortNumber();
                map.put(DataConstant.PYTHON_LABEL_PORT,portNumber[0].toString());
                map.put(DataConstant.PYTHON_GUEST_PORT,portNumber[1].toString());
                String freemarkerContent = "";
                if (modelType.getVal().equals("2")){
                    map.put(DataConstant.PYTHON_GUEST_DATASET,guestDataset);
                    freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_HOMO_XGB_INFER_PATH, freeMarkerConfigurer, map);
                    grpc(dataReasoning,dataTask,freemarkerContent,modelType.getVal(),2);
                }else{
                    freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_HOMO_LR_INFER_PATH, freeMarkerConfigurer, map);
                    grpc(dataReasoning,dataTask,freemarkerContent,modelType.getVal(),1);
                }
            }
        }

        dataTask.setTaskEndTime(System.currentTimeMillis());
        dataTaskPrRepository.updateDataTask(dataTask);
        dataReasoningPrRepository.updateDataReasoning(dataReasoning);
    }

    public void sendModelTaskMail(DataTask dataTask,Long projectId){
        if (!dataTask.getTaskState().equals(TaskStateEnum.FAIL.getStateType()))
            return;
        if (StringUtils.isBlank(baseConfiguration.getTaskEmailSubject()))
            return;
        SysUser sysUser = sysUserSecondarydbRepository.selectSysUserByUserId(dataTask.getTaskUserId());
        if (sysUser == null){
            log.info("task_id:{} The task email was not sent. Reason for not sending : No user information",dataTask.getTaskIdName());
            return;
        }
        if (!DataUtil.isEmail(sysUser.getUserAccount())){
            log.info("task_id:{} The task email was not sent. Reason for not sending : The user account is not an email address",dataTask.getTaskIdName());
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("尊敬的【");
        sb.append(sysUser.getUserName());
        sb.append("】您在【");
        sb.append(DateUtil.formatDate(dataTask.getCreateDate(),DateUtil.DateStyle.TIME_FORMAT_NORMAL.getFormat()));
        sb.append("】使用【");
        sb.append(sysUser.getUserAccount());
        sb.append("】创建的任务已失败\n");
        if (StringUtils.isNotBlank(dataTask.getTaskName())){
            sb.append("任务名称：【").append(dataTask.getTaskName()).append("】\n");
        }
        sb.append("任务ID：【").append(dataTask.getTaskIdName()).append("】\n");
        if (StringUtils.isNotBlank(baseConfiguration.getSystemDomainName())){
            sb.append("<a href=\"").append(baseConfiguration.getSystemDomainName()).append("/#/project/detail/").append(projectId).append("/task/").append(dataTask.getTaskId());
            sb.append("\">").append("点击查询任务详情").append("</a>");
        }
        sysEmailService.send(sysUser.getUserAccount(),baseConfiguration.getTaskEmailSubject(),sb.toString());
    }

    public void updateTaskState(DataTask dataTask){
        DataTask rawDataTask = dataTaskRepository.selectDataTaskByTaskId(dataTask.getTaskId());
        if (rawDataTask.getTaskState().equals(TaskStateEnum.CANCEL.getStateType())){
            dataTask.setTaskState(TaskStateEnum.CANCEL.getStateType());
        }else {
            dataTaskPrRepository.updateDataTask(dataTask);
        }
    }


    public Long[] getPortNumber(){
        Long[] port = new Long[2];
        String hostKey = RedisKeyConstant.REQUEST_PORT_NUMBER.replace("<square>", "h");
        String guestKey = RedisKeyConstant.REQUEST_PORT_NUMBER.replace("<square>", "g");
        // 递增还是递减
        String squareKey = RedisKeyConstant.REQUEST_PORT_NUMBER.replace("<square>", "s");
        String squareVal = stringRedisTemplate.opsForValue().get(squareKey);
        String hostVal = stringRedisTemplate.opsForValue().get(hostKey);
        if (org.apache.commons.lang.StringUtils.isBlank(hostVal)){
            stringRedisTemplate.opsForValue().set(squareKey,"0");
            stringRedisTemplate.opsForValue().set(hostKey,DataConstant.HOST_PORT_RANGE[0].toString());
            stringRedisTemplate.opsForValue().set(guestKey,DataConstant.GUEST_PORT_RANGE[0].toString());
        }
        if (org.apache.commons.lang.StringUtils.isBlank(squareVal) || "0".equals(squareVal)){
            port[0] = stringRedisTemplate.opsForValue().increment(hostKey);
            port[1] = stringRedisTemplate.opsForValue().increment(guestKey);
            if (DataConstant.HOST_PORT_RANGE[1].equals(port[0]))
                stringRedisTemplate.opsForValue().set(squareKey,"1");
        }else {
            port[0] = stringRedisTemplate.opsForValue().decrement(hostKey);
            port[1] = stringRedisTemplate.opsForValue().decrement(guestKey);
            if (DataConstant.HOST_PORT_RANGE[0].equals(port[0]))
                stringRedisTemplate.opsForValue().set(squareKey,"0");
        }
        return port;
    }



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
            Common.ParamValue modelFileNameParamValue = Common.ParamValue.newBuilder().setValueString(modelOutputPathDto.getModelFileName()).build();
            Common.ParamValue predictFileNameeParamValue = Common.ParamValue.newBuilder().setValueString(dataTask.getTaskResultPath()).build();
            Common.Params params = null;
            if (modelType.equals("2")){
                Common.ParamValue hostLookupTableParamValue = Common.ParamValue.newBuilder().setValueString(modelOutputPathDto.getHostLookupTable()).build();
                Common.ParamValue guestLookupTableParamValue = Common.ParamValue.newBuilder().setValueString(modelOutputPathDto.getGuestLookupTable()).build();
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

            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.ACTOR_TASK)
                    .setParams(params)
                    .setName("modelTask")
                    .setLanguage(Common.Language.PYTHON)
                    .setCode(ByteString.copyFrom(freemarkerContent.getBytes(StandardCharsets.UTF_8)))
                    .setJobId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
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
//            if (reply.getRetCode()==0){
                dataTaskMonitorService.verifyWhetherTheTaskIsSuccessfulAgain(dataTask, "1",size,dataTask.getTaskResultPath());
            if (dataTask.getTaskState().equals(TaskStateEnum.SUCCESS.getStateType())){
                dataReasoning.setReleaseDate(new Date());
            }
//                dataTask.setTaskState(TaskStateEnum.SUCCESS.getStateType());
//            }else {
//                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
//                dataTask.setTaskErrorMsg("运行失败:"+reply.getRetCode());
//            }
        } catch (Exception e) {
            dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
            dataTask.setTaskErrorMsg(e.getMessage());
            log.info("grpc Exception:{}", e.getMessage());
            e.printStackTrace();
        }
        dataReasoning.setReasoningState(dataTask.getTaskState());
    }


}
