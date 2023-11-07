package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.ComponentsConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.test.TestConfiguration;
import com.primihub.biz.convert.DataModelConvert;
import com.primihub.biz.convert.DataProjectConvert;
import com.primihub.biz.convert.DataTaskConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.dataenum.ModelStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.repository.primarydb.data.DataProjectPrRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataModelRepository;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.crypt.DateUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataModelService {

    @Autowired
    private DataProjectRepository dataProjectRepository;
    @Autowired
    private DataProjectPrRepository dataProjectPrRepository;
    @Autowired
    private DataModelPrRepository dataModelPrRepository;
    @Autowired
    private DataModelRepository dataModelRepository;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private ComponentsConfiguration componentsConfiguration;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;
    @Autowired
    private DataTaskRepository dataTaskRepository;
    @Autowired
    private DataAsyncService dataAsyncService;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private OtherBusinessesService otherBusinessesService;

    public BaseResultEntity getDataModel(Long taskId,Long userId) {
        DataTask task = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (task==null){
            task = dataTaskRepository.selectDataTaskByTaskIdName(String.valueOf(taskId));
            if (task!=null){
                taskId = task.getTaskId();
            }
        }
        DataModelTask modelTask = dataModelRepository.queryModelTaskById(taskId);
        if (modelTask==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        }
        ModelVo modelVo = dataModelRepository.queryModelById(modelTask.getModelId());
        if (modelVo==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        }
        List<ModelResourceVo> modelResourceVos = dataModelRepository.queryModelResource(modelVo.getModelId(),taskId).stream().filter(v->v.getTakePartType()==0).collect(Collectors.toList());
        DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(modelVo.getProjectId(), null);
        if (dataProject!=null){
            List<String> resourceId = modelResourceVos.stream().map(ModelResourceVo::getResourceId).collect(Collectors.toList());
            Map<String, Map> resourceListMap = otherBusinessesService.getResourceListMap(resourceId);
            if (resourceListMap.size()>0){
                for (ModelResourceVo modelResourceVo : modelResourceVos) {
                    Map map = resourceListMap.get(modelResourceVo.getResourceId());
                    if (map!=null) {
                        log.info("resourceId:{}---available:{}", modelResourceVo.getResourceId(), map.get("available"));
                        modelResourceVo.setAvailable(Integer.valueOf(map.get("available").toString()));
                        modelResourceVo.setResourceName(map.get("resourceName") == null ? "" : map.get("resourceName").toString());
                        modelResourceVo.setOrganName(map.get("organName") == null ? "" : map.get("organName").toString());
                        modelResourceVo.setOrganId(map.get("organId") == null ? "" : map.get("organId").toString());
                        modelResourceVo.setFileNum(map.get("resourceRowsCount") == null ? 0 : Integer.parseInt(map.get("resourceRowsCount").toString()));
                        modelResourceVo.setAlignmentNum(modelResourceVo.getFileNum());
                        modelResourceVo.setPrimitiveParamNum(map.get("resourceColumnCount") == null ? 0 : Integer.parseInt(map.get("resourceColumnCount").toString()));
                        modelResourceVo.setModelParamNum(modelResourceVo.getPrimitiveParamNum());
                        modelResourceVo.setResourceType(map.get("resourceType") == null ? 0 : Integer.parseInt(map.get("resourceType").toString()));
                        if (map.get("organId") != null) {
                            modelResourceVo.setParticipationIdentity(dataProject.getCreatedOrganId().equals(map.get("organId").toString()) ? 1 : 2);
                        }
                    }
                }
            }
        }
        if (task.getTaskStartTime()!=null&&task.getTaskEndTime()!=null) {
            modelVo.setTotalTime((task.getTaskEndTime()-task.getTaskStartTime())/1000);
        }
        Map<String,Object> map = new HashMap();
        List<DataComponent> dataComponents = JSONArray.parseArray(modelTask.getComponentJson(),DataComponent.class);
        if (dataComponents.size()!=0){
            Integer sumTime = dataComponents.stream().collect(Collectors.summingLong(DataComponent::getTimeConsuming)).intValue();
            List<DataModelComponentVo> dataModelComponents = dataComponents.stream().map(dataComponent -> DataModelConvert.dataComponentPoConvertDataModelComponentVo(dataComponent, sumTime)).collect(Collectors.toList());
            map.put("modelComponent",dataModelComponents);
        }
        map.put("project", DataProjectConvert.dataProjectConvertDetailsVo(dataProject));
        map.put("model",modelVo);
        map.put("task", DataTaskConvert.dataTaskPoConvertDataModelTaskList(task));
        map.put("modelResources",modelResourceVos);
        map.put("oneself",true);
        if (!baseConfiguration.getAdminUserIds().contains(userId)){
            if (!userId.equals(task.getTaskUserId())){
                map.put("oneself",false);
            }
        }
        if (StringUtils.isNotBlank(modelTask.getPredictFile())){
            String fileContent = FileUtil.getFileContent(modelTask.getPredictFile());
            if (StringUtils.isBlank(fileContent)){
                map.put("anotherQuotas",new HashMap());
            }else {
                try {
                    map.put("anotherQuotas",JSONObject.parseObject(fileContent));
                }catch (Exception e){
                    log.info("anotherQuotas json 内容:{} 转换失败:{}",fileContent,e.getMessage());
                    map.put("anotherQuotas",new HashMap());
                }
            }
        } else {
            map.put("anotherQuotas",new HashMap());
        }
        return BaseResultEntity.success(map);
    }
    
    public BaseResultEntity getDataModelList(PageReq req, String projectName, String modelName, Integer taskStatus,Long projectId) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("pageSize",req.getPageSize());
        paramMap.put("offset",req.getOffset());
        paramMap.put("projectName",projectName);
        paramMap.put("modelName",modelName);
        paramMap.put("taskStatus",taskStatus);
        paramMap.put("projectId",projectId);
        List<ModelListVo> modelListVos = dataModelRepository.queryModelList(paramMap);
        if (modelListVos.size()==0){
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Integer tolal = dataModelRepository.queryModelListCount(paramMap);
        // 状态信息
        Set<Long> modelIds = modelListVos.stream().map(ModelListVo::getModelId).collect(Collectors.toSet());
        List<Map<String, Object>> modelTaskList = dataModelRepository.queryModelTask(modelIds);
        Map<Long, Map<String, Object>> modelTaskMap = modelTaskList.stream().collect(Collectors.toMap(task -> Long.parseLong(task.get("taskId").toString()), Function.identity()));
        for (ModelListVo modelListVo : modelListVos) {
            if (modelTaskMap.containsKey(modelListVo.getLatestTaskId())){
                Map<String, Object> dataMap = modelTaskMap.get(modelListVo.getLatestTaskId());
//                Long taskId = dataMap.get("taskId")!=null?Long.valueOf(dataMap.get("taskId").toString()):null;
                Integer taskState = dataMap.get("taskState")!=null?Integer.valueOf(dataMap.get("taskState").toString()):null;
                String taskIdName = dataMap.get("taskIdName")!=null?dataMap.get("taskIdName").toString():null;
                String taskName = dataMap.get("taskName")!=null?dataMap.get("taskName").toString():null;
                Long taskStartTime = dataMap.get("taskStartTime")!=null?Long.valueOf(dataMap.get("taskStartTime").toString()):null;
                Long taskEndTime = dataMap.get("taskEndTime")!=null?Long.valueOf(dataMap.get("taskEndTime").toString()):null;
//                modelListVo.setLatestTaskId(taskId);
                modelListVo.setLatestTaskIdName(taskIdName);
                modelListVo.setLatestTaskName(taskName);
                modelListVo.setLatestTaskStatus(taskState);
                modelListVo.setLatestTaskStartTime(taskStartTime);
                modelListVo.setLatestTaskEndTime(taskEndTime);
                if (taskStartTime!=null) {
                    modelListVo.setLatestTaskStartDate(new Date(taskStartTime));
                }
                if (taskEndTime!=null) {
                    modelListVo.setTaskEndDate(new Date(taskEndTime));
                }
            }
        }
        return BaseResultEntity.success(new PageDataEntity(tolal,req.getPageSize(),req.getPageNo(),modelListVos));
    }

    public BaseResultEntity getModelComponent() {
        List<ModelComponent> modelComponents  = componentsConfiguration.getModelComponents().stream().filter(modelComponent -> modelComponent.getIsShow()==0).collect(Collectors.toList());
        return BaseResultEntity.success(modelComponents);
    }

    private boolean modelIsRunTask(String modelId){
        if (StringUtils.isBlank(modelId)) {
            return false;
        }
        DataModel dataModel = dataModelRepository.queryDataModelById(Long.parseLong(modelId));
        if (dataModel==null) {
            return false;
        }
        Map<Long, Map<String, Object>> taskMap = dataModelRepository.queryModelLatestTask(new HashSet() {{
            add(modelId);
        }});
        if (taskMap!=null&&!taskMap.isEmpty()){
            Map<String, Object> dataMap = taskMap.get(modelId);
            if (dataMap!=null&&dataMap.get("taskState")!=null&&"2".equals(dataMap.get("taskState").toString())) {
                return true;
            }
        }
        return false;
    }

    public BaseResultEntity saveModelAndComponent(Long userId, DataModelAndComponentReq params) {
        if (modelIsRunTask(params.getModelId())) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL, "模型运行任务中");
        }
        DataModel dataModel = DataModelConvert.dataModelReqConvertPo(params, userId);
        try {
            if (dataModel.getModelId()==null){
                if (params.getProjectId()==null||params.getProjectId()==0L) {
                    return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"缺少项目");
                }
                DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(params.getProjectId(), null);
                if (dataProject==null) {
                    return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"找不到项目");
                }
            }
            if (params.getIsDraft()!=null&&params.getIsDraft()==1){
                Map<String, String> paramValuesMap = getDataAlignmentComponentVals(params.getModelComponents());
                // 模型名称
//                if (StringUtils.isBlank(paramValuesMap.get("modelName"))) {
//                    return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"缺少模型名称");
//                }
                // 取消训练类型保存放入 改为运行模型枚举处理
//                if (StringUtils.isBlank(paramValuesMap.get("trainType")))
//                    return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"缺少训练类型");
                dataModel.setModelName(paramValuesMap.get("modelName"));
                dataModel.setModelDesc(paramValuesMap.get("modelDesc"));
//                dataModel.setTrainType(Integer.parseInt(paramValuesMap.get("trainType")));
                dataModel.setOrganId(organConfiguration.getSysLocalOrganId());
//                dataModel.setModelType(Integer.parseInt(paramValuesMap.get("modelType")));
                dataProjectPrRepository.updateDataProject(dataProjectRepository.selectDataProjectByProjectId(params.getProjectId(), null));
            }
            dataModel.setProjectId(params.getProjectId());
            saveOrGetModelComponentCache(true, userId,params.getProjectId(), params, dataModel);
        } catch (Exception e) {
            log.info(e.getMessage());
            e.printStackTrace();
            return BaseResultEntity.failure(BaseResultEnum.FAILURE);
        }
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("modelId",dataModel.getModelId());
        return BaseResultEntity.success(resultMap);
    }



    public BaseResultEntity getModelComponentDetail(Long modelId, Long userId,Long projectId) {
        return BaseResultEntity.success(getModelComponentReq(modelId,userId,projectId));
    }

    public DataModelAndComponentReq getModelComponentReq(Long modelId, Long userId,Long projectId){
        DataModelAndComponentReq req = null;
        if (modelId==null||modelId==0L){
            req = saveOrGetModelComponentCache(false,userId,projectId, null,null);
        }else{
            ModelComponentJson modelComponent = dataModelRepository.queryModelComponenJsonByUserId(null,0,modelId,null);
            if (modelComponent!=null) {
                if (StringUtils.isNotBlank(modelComponent.getComponentJson())) {
                    req = JSONObject.parseObject(modelComponent.getComponentJson(), DataModelAndComponentReq.class);
                    req.setModelId(modelComponent.getModelId().toString());
                }
            }
        }
        return req;
    }

    private DataModelAndComponentReq saveOrGetModelComponentCache(boolean isSave,Long userId,Long projectId,DataModelAndComponentReq params,DataModel dataModel){
        ModelComponentJson modelComponent = dataModelRepository.queryModelComponenJsonByUserId(userId,0,null,projectId);
        if (isSave){
            dataModel.setComponentJson(JSONObject.toJSONString(params));
            if (modelComponent==null&&(dataModel.getModelId()==null||dataModel.getModelId()==0L)){
                dataModel.setModelUUID(UUID.randomUUID().toString());
                dataModelPrRepository.saveDataModel(dataModel);
            }else {
                if (dataModel.getModelId()==null||dataModel.getModelId()==0L){
                    dataModel.setModelId(modelComponent.getModelId());
                }
                dataModelPrRepository.updateDataModel(dataModel);
            }
        }else {
            if (modelComponent!=null){
                if (StringUtils.isNotBlank(modelComponent.getComponentJson())){
                    params = JSONObject.parseObject(modelComponent.getComponentJson(),DataModelAndComponentReq.class);
                    params.setModelId(modelComponent.getModelId().toString());
                    params.setModelDesc(modelComponent.getModelDesc());
                }
            }
        }
        return params;
    }

    public Map<String,String> getDataAlignmentComponentVals(List<DataComponentReq> modelComponents){
        Map<String,String> paramValuesMap = new HashMap<>();
        if (modelComponents==null||modelComponents.size()==0) {
            return paramValuesMap;
        }
        for (DataComponentReq modelComponent : modelComponents) {
            for (DataComponentValue componentValue : modelComponent.getComponentValues()) {
                paramValuesMap.put(componentValue.getKey(),componentValue.getVal());
            }
        }
        return paramValuesMap;
    }

    public BaseResultEntity deleteModel(Long modelId) {
        DataModel dataModel = dataModelRepository.queryDataModelById(modelId);
        if (dataModel==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"未查询到模型信息");
        }
//        dataModel.setIsDel(1);
//        ShareModelVo vo = new ShareModelVo(dataModel);
//        Map<String,Map<String,Object>> map = dataModelRepository.queryModelLatestTask(new HashSet() {{
//            add(modelId);
//        }});
//        List<Map<String, Object>> taskList = new ArrayList<>();
//        if (map!=null&&!map.isEmpty()){
//            Iterator<Map.Entry<String, Map<String, Object>>> it = map.entrySet().iterator();
//            while (it.hasNext()){
//                Map<String, Object> value = it.next().getValue();
//                if (!value.containsKey("taskState")){
//                    return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"模型任务状态异常");
//                }
//                if (value.get("taskState").equals("2")){
//                    return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"模型任务正在运行无法删除");
//                }
//                taskList.add(value);
//            }
//            for (Map<String, Object> value : taskList) {
//                if (value.containsKey("taskId")){
//                    long taskId = Long.parseLong(value.get("taskId").toString());
//                    dataTaskPrRepository.deleteDataTask(taskId);
//                    dataModelPrRepository.deleteDataModelTask(taskId);
//                }
//            }
//        }
        dataModelPrRepository.deleteModelByModelId(modelId,dataModel.getIsDraft());
        dataModelPrRepository.deleteModelTask(modelId);
//        dataAsyncService.deleteModel(vo);
        return BaseResultEntity.success();
    }


    public BaseResultEntity runTaskModel(Long modelId,Long userId) {
        DataModel dataModel = dataModelRepository.queryDataModelById(modelId);
        if (dataModel==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未查询到模型");
        }
        if (dataModel.getIsDraft().equals(ModelStateEnum.DRAFT.getStateType())) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"模型未保存,请保存后再次尝试");
        }
        if (StringUtils.isBlank(dataModel.getComponentJson())) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未查询到模型组件信息");
        }
//        if (StringUtils.isBlank(dataModel.getModelName())) {
//            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"模型名称不能为空");
//        }
//        if (dataModel.getTrainType()==null)
//            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"模型训练类型不能为空");
        Map<Long, Map<String, Object>> taskMap = dataModelRepository.queryModelLatestTask(new HashSet() {{
            add(modelId);
        }});
        if (taskMap!=null&&!taskMap.isEmpty()){
            Map<String, Object> dataMap = taskMap.get(modelId);
            if (dataMap.get("taskState")!=null&&Integer.parseInt(dataMap.get("taskState").toString())==2) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"任务正在运行中");
            }
        }
        ComponentTaskReq taskReq = new ComponentTaskReq(dataModel);
        DataModelAndComponentReq modelComponentReq = taskReq.getModelComponentReq();
        if (modelComponentReq==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"组件解析失败");
        }
        Set<String> mandatorySet = componentsConfiguration.getModelComponents().stream()
                .filter(modelComponent -> modelComponent.getIsMandatory() == 0)
                .map(ModelComponent::getComponentCode)
                .collect(Collectors.toSet());
//        Set<String> reqSet = modelComponentReq.getModelComponents().stream().map(DataComponentReq::getComponentCode).collect(Collectors.toSet());
//        mandatorySet.removeAll(reqSet);
//        if (!mandatorySet.isEmpty()) {
//            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"缺少必选组件,请检查组件");
//        }
        for (DataComponentReq modelComponent : modelComponentReq.getModelComponents()) {
            BaseResultEntity baseResultEntity = dataAsyncService.executeBeanMethod(true, modelComponent, taskReq);
            if (baseResultEntity.getCode()!=0){
                return baseResultEntity;
            }
        }
        dataModelPrRepository.updateDataModel(taskReq.getDataModel());
        DataTask dataTask = taskReq.getDataTask();
//        dataTask.setTaskIdName(UUID.randomUUID().toString());
        dataTask.setTaskIdName(Long.toString(SnowflakeId.getInstance().nextId()));
        dataTask.setTaskType(TaskTypeEnum.MODEL.getTaskType());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTask.setTaskState(TaskStateEnum.INIT.getStateType());
        dataTask.setTaskUserId(userId);
        dataTask.setCreateDate(new Date());
        dataTaskPrRepository.saveDataTask(dataTask);
//        taskReq.setDataTask(dataTask);
        DataModelTask modelTask = new DataModelTask();
        modelTask.setTaskId(dataTask.getTaskId());
        modelTask.setModelId(dataModel.getModelId());
        dataModelPrRepository.saveDataModelTask(modelTask);
        taskReq.setDataModelTask(modelTask);
        log.info("------------- 模型类型" + dataModel.getModelType());
        taskReq.getDataModel().setModelType(dataModel.getModelType());
        dataAsyncService.runModelTask(taskReq);
        Map<String,Object> returnMap = new HashMap<>();
        returnMap.put("modelId",modelId);
        returnMap.put("taskId",dataTask.getTaskId());
        return BaseResultEntity.success(returnMap);
    }

    public BaseResultEntity restartTaskModel(Long taskId) {
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (dataTask==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未查询到任务信息");
        }
        DataModelTask modelTask = dataModelRepository.queryModelTaskById(taskId);
        if (modelTask==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未查询到模型任务信息");
        }
        DataModel dataModel = dataModelRepository.queryDataModelById(modelTask.getModelId());
        if (dataModel==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未查询到模型信息");
        }
        ComponentTaskReq taskReq = new ComponentTaskReq(dataModel);
        DataModelAndComponentReq modelComponentReq = taskReq.getModelComponentReq();
        if (modelComponentReq==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"组件解析失败");
        }
        for (DataComponentReq modelComponent : modelComponentReq.getModelComponents()) {
            BaseResultEntity baseResultEntity = dataAsyncService.executeBeanMethod(true, modelComponent, taskReq);
            if (baseResultEntity.getCode()!=0){
                return baseResultEntity;
            }
        }
        dataTask.setTaskErrorMsg("");
        dataTask.setTaskState(TaskStateEnum.INIT.getStateType());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTask.setTaskEndTime(dataTask.getTaskStartTime());
        dataTaskPrRepository.updateDataTask(dataTask);
        taskReq.setDataTask(dataTask);
        taskReq.setDataModelTask(modelTask);
        dataModelPrRepository.deleteDataModelResource(dataModel.getModelId());
        dataModelPrRepository.deleteDataComponent(dataModel.getModelId());
        dataModelPrRepository.deleteDataModelComponent(dataModel.getModelId());
        dataAsyncService.runModelTask(taskReq);
        Map<String,Object> returnMap = new HashMap<>();
        returnMap.put("modelId",dataModel.getModelId());
        returnMap.put("taskId",dataTask.getTaskId());
        return BaseResultEntity.success(returnMap);
    }

    public BaseResultEntity getTaskModelComponent(Long taskId) {
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (dataTask==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        }
        if (dataTask.getTaskState()==0) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"请检查任务运行状态");
        }
        DataModelTask modelTask = dataModelRepository.queryModelTaskById(taskId);
        if (modelTask==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        }
        if (StringUtils.isBlank(modelTask.getComponentJson())){
            return BaseResultEntity.success();
        }
        List<DataComponent> dataComponents = JSONObject.parseArray(modelTask.getComponentJson(),DataComponent.class);
        if (dataComponents.size()==0) {
            return BaseResultEntity.success();
        }
        Map<String,Object> map = new HashMap<>();
        map.put("taskState",dataTask.getTaskState());
        map.put("components",dataComponents.stream().map(DataModelConvert::dataComponentPoConvertDataComponentVo).collect(Collectors.toList()));
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getModelPrediction(Long modelId){
        Map result=new HashMap();
        result.put("prediction",TestConfiguration.TEMP);
        return BaseResultEntity.success(result);
    }


    public BaseResultEntity syncModel(ShareModelVo vo) {
//        log.info(JSONObject.toJSONString(vo));
        if (vo.getDataModel()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"dataModel");
        }
        boolean isDel = vo.getDataModel().getIsDel()==1;
        if (!isDel){
            if (vo.getDataModelTask()==null) {
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"dataModelTask");
            }
            if (vo.getDataTask()==null) {
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"dataTask");
            }
            if (StringUtils.isBlank(vo.getDataModel().getModelUUID())) {
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelUUID");
            }
            if (vo.getDmrList()==null||vo.getDmrList().isEmpty()) {
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"dmrList");
            }
        }
        DataModel dataModel = this.dataModelRepository.queryDataModelByUUID(vo.getDataModel().getModelUUID());
        if (dataModel==null){
            DataProject dataProject = this.dataProjectRepository.selectDataProjectByProjectId(null, vo.getProjectId());
            if (dataProject==null) {
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"no project data");
            }
            vo.getDataModel().setProjectId(dataProject.getId());
            dataModelPrRepository.saveDataModel(vo.getDataModel());
        }else {
            if (isDel){
                dataModelPrRepository.deleteModelByModelId(dataModel.getModelId(),dataModel.getIsDraft());
            }else {
                vo.getDataModel().setModelId(dataModel.getModelId());
                dataModelPrRepository.updateDataModel(vo.getDataModel());
            }
        }
        if (isDel){
            List<DataModelTask> taskList = dataModelRepository.queryModelTaskByModelId(new HashMap(){
                {
                    put("modelId",dataModel.getModelId());
                    put("offset",0);
                    put("pageSize",20);
                }
            });
            for (DataModelTask modelTask : taskList) {
                dataModelPrRepository.deleteDataModelTask(modelTask.getTaskId());
                dataTaskPrRepository.deleteDataTask(modelTask.getTaskId());
            }
        }else {
            vo.getDataTask().setIsCooperation(1);
            DataTask dataTask = dataTaskRepository.selectDataTaskByTaskIdName(vo.getDataTask().getTaskIdName());
            if (dataTask==null){
                dataTaskPrRepository.saveDataTask(vo.getDataTask());
            }else {
                vo.getDataTask().setTaskId(dataTask.getTaskId());
                dataTaskPrRepository.updateDataTask(vo.getDataTask());
                dataModelPrRepository.deleteDataModelTask(dataTask.getTaskId());
                dataModelPrRepository.deleteDataModelResourceByTaskId(dataTask.getTaskId());
            }
            vo.getDataModelTask().setTaskId(vo.getDataTask().getTaskId());
            vo.getDataModelTask().setModelId(vo.getDataModel().getModelId());
            dataModelPrRepository.saveDataModelTask(vo.getDataModelTask());
            for (DataModelResource dataModelResource : vo.getDmrList()) {
                dataModelResource.setModelId(vo.getDataModel().getModelId());
                dataModelResource.setTaskId(vo.getDataTask().getTaskId());
            }
            BaseResultEntity derivationResource = dataResourceService.saveDerivationResource(vo.getDerivationList(), null);
            log.info(JSONObject.toJSONString(derivationResource));
            if (derivationResource.getCode()==0){
                List<String> resourceIds = (List<String>) derivationResource.getResult();
                for (String resourceId : resourceIds) {
                    DataModelResource dataModelResource = new DataModelResource(vo.getDataModel().getModelId());
                    dataModelResource.setTaskId(vo.getDataTask().getTaskId());
                    dataModelResource.setResourceId(resourceId);
                    vo.getDmrList().add(dataModelResource);
                }
            }
            dataModelPrRepository.saveDataModelResourceList(vo.getDmrList());
        }
        dataProjectPrRepository.updateDataProject(dataProjectRepository.selectDataProjectByProjectId(null,vo.getProjectId()));
        return BaseResultEntity.success();
    }


    public BaseResultEntity getModelTaskSuccessList(ModelTaskSuccessReq req) {
        if (StringUtils.isNotBlank(req.getStartDate())){
            req.setStartTime(DateUtil.parseDate(req.getStartDate(),DateUtil.DateStyle.DATE_FORMAT_NORMAL.getFormat()).getTime());
        }
        if (StringUtils.isNotBlank(req.getEndDate())){
            req.setEndTime(DateUtil.parseDate(req.getEndDate(),DateUtil.DateStyle.DATE_FORMAT_NORMAL.getFormat()).getTime());
        }
        if (baseConfiguration.getAdminUserIds().contains(req.getUserId())) {
            req.setIsAdmin(1);
        }
        List<ModelTaskSuccessVo> modelTaskSuccessVos = dataModelRepository.queryModelTaskSuccessList(req);
        if (modelTaskSuccessVos.size()==0){
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Integer tolal = dataModelRepository.queryModelTaskSuccessCount(req);
        Set<Long> taskIds = modelTaskSuccessVos.stream().map(ModelTaskSuccessVo::getTaskId).collect(Collectors.toSet());
        List<DataModelTask> dataModelTasks = dataModelRepository.queryModelTaskByTaskIds(taskIds);
        Map<Long,List<Map<String,String>>> taskResource = getTaskResource(dataModelTasks,modelTaskSuccessVos);
        for (ModelTaskSuccessVo modelTaskSuccessVo : modelTaskSuccessVos) {
            if (modelTaskSuccessVo.getTaskEndTime()!=null&&modelTaskSuccessVo.getTaskEndTime()!=0){
                modelTaskSuccessVo.setTaskEndDate(new Date(modelTaskSuccessVo.getTaskEndTime()));
            }
            if(taskResource.containsKey(modelTaskSuccessVo.getTaskId())){
                modelTaskSuccessVo.setProviderOrgans(taskResource.get(modelTaskSuccessVo.getTaskId()));
            }
        }
        return BaseResultEntity.success(new PageDataEntity(tolal,req.getPageSize(),req.getPageNo(),modelTaskSuccessVos));
    }

    private Map<Long,List<Map<String,String>>> getTaskResource(List<DataModelTask> dataModelTasks,List<ModelTaskSuccessVo> modelTaskSuccessVos){
        Map<Long,List<Map<String,String>>> map = new HashMap<>();
        Map<Long, ModelTaskSuccessVo> taskSuccessMap = modelTaskSuccessVos.stream().collect(Collectors.toMap(ModelTaskSuccessVo::getTaskId, Function.identity()));
        for (DataModelTask dataModelTask : dataModelTasks) {
            try {
                List<DataComponent> dataComponentReqs = JSONArray.parseArray(dataModelTask.getComponentJson(), DataComponent.class);
                DataComponent dataSet = dataComponentReqs.stream().filter(req -> "dataSet".equals(req.getComponentCode())).findFirst().orElse(null);
                if (dataSet!=null){
                    List<Map<String,String>> list = new ArrayList<>();
                    List<ModelProjectResourceVo> modelProjectResourceVos = JSONArray.parseArray(dataSet.getDataJson()).getJSONObject(0).getJSONArray("val").toJavaList(ModelProjectResourceVo.class);
                    for (ModelProjectResourceVo modelProjectResourceVo : modelProjectResourceVos) {
                        if (!taskSuccessMap.get(dataModelTask.getTaskId()).getCreatedOrganId().equals(modelProjectResourceVo.getOrganId())){
                            list.add(new HashMap(){
                                {
                                    put("organId",modelProjectResourceVo.getOrganId());
                                    put("organName",modelProjectResourceVo.getOrganName());
                                }
                            });
                        }
                    }
                    map.put(dataModelTask.getTaskId(),list);

                }
            }catch (Exception e){
                log.info(e.getMessage());
            }
        }
        return map;
    }

    public BaseResultEntity saveOrUpdateComponentDraft(ComponentDraftReq req) {
//        try {
//            req.setComponentJson(formatComponent(req.getComponentJson()));
//        }catch (Exception e){
//            log.info(e.getMessage());
//            BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"组件解析失败");
//        }
        DataComponentDraft dataComponentDraft = DataModelConvert.componentDraftReqCovertPo(req);
        List<DataComponentDraftVo> dataComponentDraftVos = dataModelRepository.queryComponentDraftListByUserId(req.getUserId());
        Set<String> nameSet = dataComponentDraftVos.stream().map(DataComponentDraftVo::getDraftName).collect(Collectors.toSet());
        if (dataComponentDraft.getDraftId()!=null && dataComponentDraft.getDraftId()!=0L){
            DataComponentDraft draft = dataModelRepository.queryComponentDraftById(dataComponentDraft.getDraftId());
            if (draft==null) {
                BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"未查询到草稿信息");
            }
            nameSet.remove(draft.getDraftName());
            if (nameSet.contains(req.getDraftName())) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"草稿名称重复");
            }
            dataModelPrRepository.updateComponentDraft(dataComponentDraft);
        }else {
            if (nameSet.contains(req.getDraftName())) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"草稿名称重复");
            }
            int count = dataModelRepository.queryComponentDraftCountByUserId(req.getUserId());
            if (count>=20) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"草稿已到最高20个,请清除其他草稿重试。");
            }
            dataModelPrRepository.saveComponentDraft(dataComponentDraft);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("draftId",dataComponentDraft.getDraftId());
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getComponentDraftList(Long userId) {
        return BaseResultEntity.success(dataModelRepository.queryComponentDraftListByUserId(userId));
    }

    private String formatComponent(String componentJson){
        DataModelAndComponentReq dataModelAndComponentReq = JSONObject.parseObject(componentJson, DataModelAndComponentReq.class);
        for (DataComponentReq modelComponent : dataModelAndComponentReq.getModelComponents()) {
            if("dataSet".equals(modelComponent.getComponentCode())){
                for (DataComponentValue componentValue : modelComponent.getComponentValues()) {
                    componentValue.setVal("");
                }
            }
        }
        return JSONObject.toJSONString(dataModelAndComponentReq);
    }

    public BaseResultEntity deleteComponentDraft(Long draftId,Long userId) {
        DataComponentDraft dataComponentDraft = dataModelRepository.queryComponentDraftById(draftId);
        if (dataComponentDraft==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到草稿信息");
        }
        dataModelPrRepository.deleteComponentDraft(draftId);
        return BaseResultEntity.success();
    }

    public BaseResultEntity updateModelDesc(Long modelId, String modelDesc) {
        DataModel dataModel = dataModelRepository.queryDataModelById(modelId);
        if (dataModel==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到模型信息");
        }
        dataModel.setModelDesc(modelDesc);
        DataModelAndComponentReq dataModelAndComponentReq = JSONObject.parseObject(dataModel.getComponentJson(), DataModelAndComponentReq.class);
        dataModelAndComponentReq.setModelDesc(modelDesc);
        for (DataComponentReq modelComponent : dataModelAndComponentReq.getModelComponents()) {
            if ("model".equals(modelComponent.getComponentCode())){
                for (DataComponentValue componentValue : modelComponent.getComponentValues()) {
                    if ("modelDesc".equals(componentValue.getKey())){
                        componentValue.setVal(modelDesc);
                    }
                }
            }
        }
        dataModel.setComponentJson(JSONObject.toJSONString(dataModelAndComponentReq));
        dataModelPrRepository.updateDataModel(dataModel);
        return BaseResultEntity.success();
    }
}
