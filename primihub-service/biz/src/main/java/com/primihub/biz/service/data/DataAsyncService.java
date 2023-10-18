package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.primihub.biz.repository.primarydb.data.*;
import com.primihub.biz.repository.secondarydb.data.*;
import com.primihub.biz.repository.secondarydb.sys.SysUserSecondarydbRepository;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.service.sys.SysEmailService;
import com.primihub.biz.util.DataUtil;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.crypt.DateUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import com.primihub.sdk.task.TaskHelper;
import com.primihub.sdk.task.dataenum.ModelTypeEnum;
import com.primihub.sdk.task.param.TaskComponentParam;
import com.primihub.sdk.task.param.TaskPIRParam;
import com.primihub.sdk.task.param.TaskPSIParam;
import com.primihub.sdk.task.param.TaskParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
    private SysUserSecondarydbRepository sysUserSecondarydbRepository;
    @Autowired
    private SysEmailService sysEmailService;
    @Autowired
    private TaskHelper taskHelper;

    public TaskHelper getTaskHelper(){
        return taskHelper;
    }


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
        log.info("end model task grpc modelId:{} modelName:{} end time:{}", req.getDataModel().getModelId(), req.getDataModel().getModelName(), System.currentTimeMillis());
        log.info("Share model task modelId:{} modelName:{}", req.getDataModel().getModelId(), req.getDataModel().getModelName());
        ShareModelVo vo = new ShareModelVo();
        vo.setDataModel(req.getDataModel());
        vo.setDataTask(req.getDataTask());
        vo.setDataModelTask(req.getDataModelTask());
        vo.setDmrList(req.getDmrList());
        vo.setShareOrganId(req.getResourceList().stream().map(ModelProjectResourceVo::getOrganId).collect(Collectors.toList()));
        vo.setDerivationList(req.getDerivationList());
        sendShareModelTask(vo);
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
    public void psiGrpcRun(DataPsiTask psiTask, DataPsi dataPsi,String taskName) {
        DataResource ownDataResource = dataResourceRepository.queryDataResourceByResourceFusionId(dataPsi.getOwnResourceId());
        if (ownDataResource==null){
            ownDataResource = dataResourceRepository.queryDataResourceById(Long.parseLong(dataPsi.getOwnResourceId()));
        }
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
        if (taskName == null){
            dataTask.setTaskName(dataPsi.getResultName());
        }else {
            dataTask.setTaskName(taskName);
        }
        dataTask.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        dataTask.setTaskType(TaskTypeEnum.PSI.getTaskType());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTaskPrRepository.saveDataTask(dataTask);
        String teeResourceId = "";
        if (dataPsi.getTag().equals(2)){
            DataFResourceReq fresourceReq = new DataFResourceReq();
            fresourceReq.setOrganId(dataPsi.getTeeOrganId());
            BaseResultEntity resourceList = otherBusinessesService.getResourceList(fresourceReq);
            if (resourceList.getCode()!=0) {
                psiTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataPsiPrRepository.updateDataPsiTask(psiTask);
                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataTask.setTaskEndTime(System.currentTimeMillis());
                dataTask.setTaskErrorMsg("TEE 机构资源查询失败:"+resourceList.getMsg());
                dataTaskPrRepository.updateDataTask(dataTask);
                return;
            }
            LinkedHashMap<String,Object> data = (LinkedHashMap<String,Object>)resourceList.getResult();
            List<LinkedHashMap<String,Object>> resourceDataList = (List<LinkedHashMap<String,Object>>)data.get("data");
            if (resourceDataList==null || resourceDataList.size()==0) {
                psiTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataPsiPrRepository.updateDataPsiTask(psiTask);
                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataTask.setTaskEndTime(System.currentTimeMillis());
                dataTask.setTaskErrorMsg("TEE 机构资源查询失败:机构下无资源信息");
                dataTaskPrRepository.updateDataTask(dataTask);
                return;
            }
            teeResourceId = resourceDataList.get(0).get("resourceId").toString();
        }
        psiTask.setTaskState(2);
        dataPsiPrRepository.updateDataPsiTask(psiTask);
        log.info("psi available:{}", available);
        if (available == 0) {
            Date date = new Date();
            StringBuilder sb = new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(DateUtil.formatDate(date, DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat())).append("/").append(psiTask.getTaskId()).append(".csv");
            psiTask.setFilePath(sb.toString());
            try {
                TaskPSIParam psiParam = new TaskPSIParam();
                psiParam.setPsiTag(dataPsi.getTag());
                psiParam.setPsiType(dataPsi.getOutputContent());
                psiParam.setClientData(ownDataResource.getResourceFusionId());
                List<String> clientFields = Arrays.asList(ownDataResource.getFileHandleField().split(","));
                List<String> ownKeyword = Arrays.asList(dataPsi.getOwnKeyword().split(","));
                psiParam.setClientIndex(ownKeyword.stream().map(clientFields::indexOf).toArray(Integer[]::new));
                List<String> serverFields = Arrays.asList(resourceColumnNameList.split(","));
                List<String> otherKeyword = Arrays.asList(dataPsi.getOtherKeyword().split(","));
                psiParam.setServerData(resourceId);
                psiParam.setTeeData(teeResourceId);
                psiParam.setServerIndex(otherKeyword.stream().map(serverFields::indexOf).toArray(Integer[]::new));
                psiParam.setOutputFullFilename(psiTask.getFilePath());
                TaskParam taskParam = new TaskParam();
                taskParam.setTaskId(dataTask.getTaskIdName());
                taskParam.setTaskContentParam(psiParam);
                taskHelper.submit(taskParam);
                if (taskParam.getSuccess()){
                    dataTask.setTaskState(TaskStateEnum.SUCCESS.getStateType());
                }else {
                    dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                    dataTask.setTaskErrorMsg(taskParam.getError());
                }
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
        } else {
            psiTask.setTaskState(3);
        }
        dataPsiPrRepository.updateDataPsiTask(psiTask);
        dataTask.setTaskState(psiTask.getTaskState());
        dataTask.setTaskEndTime(System.currentTimeMillis());
        updateTaskState(dataTask);
    }

    @Async
    public void pirGrpcTask(DataTask dataTask, String resourceId, String param) {
        Date date = new Date();
        try {
            String formatDate = DateUtil.formatDate(date, DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat());
            StringBuilder sb = new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(formatDate).append("/").append(dataTask.getTaskIdName()).append(".csv");
            dataTask.setTaskResultPath(sb.toString());
            TaskPIRParam pirParam = new TaskPIRParam();
            pirParam.setQueryParam(param.split(","));
            pirParam.setServerData(resourceId);
            pirParam.setOutputFullFilename(dataTask.getTaskResultPath());
            TaskParam taskParam = new TaskParam();
            taskParam.setTaskContentParam(pirParam);
            taskParam.setTaskId(dataTask.getTaskIdName());
            taskHelper.submit(taskParam);
            if (taskParam.getSuccess()){
                dataTask.setTaskState(TaskStateEnum.SUCCESS.getStateType());
            }else {
                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataTask.setTaskErrorMsg("运行失败:"+taskParam.getError());
            }
        } catch (Exception e) {
            dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
            dataTask.setTaskErrorMsg(e.getMessage());
            log.info("grpc pirSubmitTask Exception:{}", e.getMessage());
            e.printStackTrace();
        }
        dataTask.setTaskEndTime(System.currentTimeMillis());
        updateTaskState(dataTask);
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
                ModelTypeEnum modelTypeEnum = ModelTypeEnum.MODEL_TYPE_MAP.get(Integer.valueOf(modelType.getVal()));
                if (modelTypeEnum==null){
                    dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                    dataTask.setTaskErrorMsg("未能匹配到模型类型信息");
                }else {
                    map.put(PYTHON_GUEST_DATASET, guestDataset);  // 放入合作方资源
                    grpc(dataReasoning, dataTask, modelTypeEnum, map);
                }
            }
        }
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

    private void grpc(DataReasoning dataReasoning, DataTask dataTask, ModelTypeEnum modelTypeEnum, Map<String, Object> map) {
        // 推理任务
        DataTask modelTask = dataTaskRepository.selectDataTaskByTaskId(dataReasoning.getTaskId());
        // 推理模板参数
        ModelOutputPathDto modelOutputPathDto = JSONObject.parseObject(modelTask.getTaskResultContent(), ModelOutputPathDto.class);
        map.put("indicatorFileName", modelOutputPathDto.getIndicatorFileName());
        map.put("guestModelFileName", modelOutputPathDto.getGuestModelFileName());
        map.put("hostModelFileName", modelOutputPathDto.getHostModelFileName());
        map.put("guestLookupTable", modelOutputPathDto.getGuestLookupTable());
        map.put("predictFileName", modelOutputPathDto.getPredictFileName());
        try {
            TaskParam<TaskComponentParam> taskParam = new TaskParam<>(new TaskComponentParam());
            taskParam.setTaskId(dataTask.getTaskIdName());
            taskParam.setJobId("1");
            taskParam.getTaskContentParam().setModelType(modelTypeEnum);
            taskParam.getTaskContentParam().setInfer(true);
            taskParam.getTaskContentParam().setFreemarkerMap(map);
            log.info(taskParam.toString());
            taskHelper.submit(taskParam);
            if (taskParam.getSuccess()){
                dataTask.setTaskState(TaskStateEnum.SUCCESS.getStateType());
                dataTask.setTaskResultPath(modelOutputPathDto.getPredictFileName());
                dataReasoning.setReleaseDate(new Date());
            }else {
                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataTask.setTaskErrorMsg("运行失败:"+taskParam.getError());
            }
        } catch (Exception e) {
            dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
            dataTask.setTaskErrorMsg(e.getMessage());
            log.info("grpc Exception:{}", e.getMessage());
            e.printStackTrace();
        }
        dataReasoning.setReasoningState(dataTask.getTaskState());
        dataTask.setTaskEndTime(System.currentTimeMillis());
        dataTaskPrRepository.updateDataTask(dataTask);
        dataReasoningPrRepository.updateDataReasoning(dataReasoning);
    }
}
