package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.parser.ParserConfig;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.test.TestConfiguration;
import com.primihub.biz.convert.DataModelConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.dto.ModelEvaluationDto;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataModelRepository;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
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
    private DataModelPrRepository dataModelPrRepository;
    @Autowired
    private DataModelRepository dataModelRepository;
    @Autowired
    private ModelInitService modelInitService;
    @Autowired
    private DataProjectService dataProjectService;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;
    @Autowired
    private DataTaskRepository dataTaskRepository;

    public BaseResultEntity getDataModel(Long taskId) {
        DataModelTask modelTask = dataModelRepository.queryModelTaskById(taskId);
        if (modelTask==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        ModelVo modelVo = dataModelRepository.queryModelById(modelTask.getModelId());
        if (modelVo==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        }
        DataTask task = dataTaskRepository.selectDataTaskByTaskId(taskId);
        // 查询资源模型
        List<ModelResourceVo> modelResourceVos = dataModelRepository.queryModelResource(modelVo.getModelId(),taskId);
        DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(modelVo.getProjectId(), null);
        if (dataProject!=null){
            List<String> resourceId = modelResourceVos.stream().map(ModelResourceVo::getResourceId).collect(Collectors.toList());
            Map<String, Map> resourceListMap = dataProjectService.getResourceListMap(resourceId, dataProject.getServerAddress());
            if (resourceListMap.size()>0){
                for (ModelResourceVo modelResourceVo : modelResourceVos) {
                    Map map = resourceListMap.get(modelResourceVo.getResourceId());
                    if (map!=null){
                        modelResourceVo.setResourceName(map.get("resourceName")==null?"":map.get("resourceName").toString());
                        modelResourceVo.setOrganName(map.get("organName")==null?"":map.get("organName").toString());
                        modelResourceVo.setOrganId(map.get("organId")==null?"":map.get("organId").toString());
                        modelResourceVo.setFileNum(map.get("resourceRowsCount")==null?0:Integer.parseInt(map.get("resourceRowsCount").toString()));
                        modelResourceVo.setAlignmentNum(modelResourceVo.getFileNum());
                        modelResourceVo.setPrimitiveParamNum(map.get("resourceColumnCount")==null?0:Integer.parseInt(map.get("resourceColumnCount").toString()));
                        modelResourceVo.setModelParamNum(modelResourceVo.getPrimitiveParamNum());
                    }
                }
            }
        }
        Map<String,Object> map = new HashMap();
        List<DataComponent> dataComponents = JSONArray.parseArray(modelTask.getComponentJson(),DataComponent.class);
        if (dataComponents.size()!=0){
            Integer sumTime = dataComponents.stream().collect(Collectors.summingLong(DataComponent::getTimeConsuming)).intValue();
            List<DataModelComponentVo> dataModelComponents = dataComponents.stream().map(dataComponent -> DataModelConvert.dataComponentPoConvertDataModelComponentVo(dataComponent, sumTime)).collect(Collectors.toList());
            map.put("modelComponent",dataModelComponents);
        }
        map.put("model",modelVo);
        map.put("taskState",task.getTaskState());
        map.put("modelResources",modelResourceVos);
        ModelEvaluationDto modelEvaluationDto = null;
        if (StringUtils.isNotBlank(modelTask.getPredictContent())){
            ParserConfig parserConfig = new ParserConfig();
            parserConfig.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
            modelEvaluationDto = JSONObject.parseObject(modelTask.getPredictContent(), ModelEvaluationDto.class,parserConfig);
        }
        if (modelEvaluationDto==null||modelEvaluationDto.getTrain()==null){
            map.put("anotherQuotas",new HashMap());
        }else {
            map.put("anotherQuotas",modelEvaluationDto.getTrain());
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
        Map<Long, Map<String, Object>> modelLatestTaskMap = dataModelRepository.queryModelLatestTask(modelIds);
        for (ModelListVo modelListVo : modelListVos) {
            if (modelLatestTaskMap.containsKey(modelListVo.getModelId())){
                Map<String, Object> dataMap = modelLatestTaskMap.get(modelListVo.getModelId());
                Long taskId = dataMap.get("taskId")!=null?Long.valueOf(dataMap.get("taskId").toString()):null;
                Integer taskState = dataMap.get("taskState")!=null?Integer.valueOf(dataMap.get("taskState").toString()):null;
                modelListVo.setLatestTaskId(taskId);
                modelListVo.setLatestTaskStatus(taskState);
            }
        }
        return BaseResultEntity.success(new PageDataEntity(tolal,req.getPageSize(),req.getPageNo(),modelListVos));
    }

    public BaseResultEntity getModelComponent() {
        List<ModelComponent> modelComponents  = baseConfiguration.getModelComponents().stream().filter(modelComponent -> modelComponent.getIsShow()==0).collect(Collectors.toList());
        return BaseResultEntity.success(modelComponents);
    }

    private boolean modelIsRunTask(String modelId){
        if (StringUtils.isBlank(modelId))
            return false;
        DataModel dataModel = dataModelRepository.queryDataModelById(Long.parseLong(modelId));
        if (dataModel==null)
            return false;
        Map<Long, Map<String, Object>> taskMap = dataModelRepository.queryModelLatestTask(new HashSet() {{
            add(modelId);
        }});
        if (taskMap!=null&&!taskMap.isEmpty()){
            Map<String, Object> dataMap = taskMap.get(modelId);
            if (dataMap!=null&&dataMap.get("taskState")!=null&&"2".equals(dataMap.get("taskState").toString()))
                return true;
        }
        return false;
    }

    public BaseResultEntity saveModelAndComponent(Long userId, DataModelAndComponentReq params) {
        if (modelIsRunTask(params.getModelId()))
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL, "模型运行任务中");
        DataModel dataModel = DataModelConvert.dataModelReqConvertPo(params, userId);
        try {
            if (dataModel.getModelId()==null){
                if (params.getProjectId()==null||params.getProjectId()==0L)
                    return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"缺少项目");
                DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(params.getProjectId(), null);
                if (dataProject==null)
                    return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"找不到项目");
                dataModel.setProjectId(params.getProjectId());
            }
            if (params.getIsDraft()!=null&&params.getIsDraft()==1){
                Map<String, String> paramValuesMap = getDataAlignmentComponentVals(params.getModelComponents());
                // 模型名称
                if (StringUtils.isBlank(paramValuesMap.get("modelName")))
                    return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"缺少模型名称");
                if (StringUtils.isBlank(paramValuesMap.get("trainType")))
                    return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"缺少训练类型");
                dataModel.setModelName(paramValuesMap.get("modelName"));
                dataModel.setModelDesc(paramValuesMap.get("modelDesc"));
                dataModel.setTrainType(Integer.parseInt(paramValuesMap.get("trainType")));
                dataModel.setOrganId(organConfiguration.getSysLocalOrganId());
            }
            saveOrGetModelComponentCache(true, userId,params.getProjectId(), params, dataModel);
        } catch (Exception e) {
            log.info(e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.FAILURE);
        }
        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("modelId",dataModel.getModelId());
        return BaseResultEntity.success(resultMap);
    }

    private String formatModelComponentJson(DataModelAndComponentReq params,Map<String, DataComponent> dataComponentMap){
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

    private BaseResultEntity extracteComponentData(DataModel dataModel, DataModelAndComponentReq params, Map<String, Object> map) {
        List<DataModelComponent> dataModelComponents = new ArrayList<>();
        List<DataComponent> dataComponents = new ArrayList<>();
        List<DataComponentReq> modelComponents = params.getModelComponents();
        for (DataComponentReq modelComponent : modelComponents) {
            List<DataComponentRelationReq> input = modelComponent.getInput();
            List<DataComponentRelationReq> output = modelComponent.getOutput();
            if (input.size()==0&&output.size()==0){
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"流程不合规");
            }
            if (input.size()>0&&output.size()>0){
                for (DataComponentRelationReq inputReq : input) {
                    for (DataComponentRelationReq outReq : output) {
                        dataModelComponents.add(new DataModelComponent(inputReq.getComponentCode(),outReq.getComponentCode(),outReq.getPointType(),outReq.getPointJson()));
                    }
                }
            }else if(input.size()>0&&output.size()==0) {
                for (DataComponentRelationReq inputReq : input) {
                    dataModelComponents.add(new DataModelComponent(inputReq.getComponentCode(),"",inputReq.getPointType(),inputReq.getPointJson()));
                }
            }else if(input.size()==0&&output.size()>0) {
                for (DataComponentRelationReq outputReq : output) {
                    dataModelComponents.add(new DataModelComponent("",outputReq.getComponentCode(),outputReq.getPointType(),outputReq.getPointJson()));
                }
            }
            dataComponents.add(DataModelConvert.dataModelReqConvertDataComponentPo(modelComponent));
        }
        map.put("dataComponents",dataComponents);
        map.put("dataModelComponents",dataModelComponents);
        return BaseResultEntity.success();
    }

    private BaseResultEntity extracteModelData(DataModel dataModel,DataModelAndComponentReq params, Map<String, Object> map){
        List<DataComponentReq> modelComponents = params.getModelComponents();
        if (modelComponents==null||modelComponents.size()==0)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"组件为空");
        Map<String, String> paramValuesMap = getDataAlignmentComponentVals(modelComponents);
        //资源
        if (StringUtils.isBlank(paramValuesMap.get("selectData")))
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"缺少资源");
        List<ModelProjectResourceVo> resourceList = null;
        try {
            resourceList = JSONObject.parseArray(paramValuesMap.get("selectData"), ModelProjectResourceVo.class);
            if (resourceList==null||resourceList.size()==0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"资源信息为空或资源数量为0");
        }catch (Exception e){
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"资源内容无法使用");
        }
        Set<String> resourceIds = resourceList.stream().map(ModelProjectResourceVo::getResourceId).collect(Collectors.toSet());
        if (resourceIds.contains(null)||resourceIds.contains(""))
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"资源ID存在为空的数据");
        // 模型类型
        if (StringUtils.isBlank(paramValuesMap.get("modelType")))
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"缺少模型类型");
        dataModel.setModelType(Integer.parseInt(paramValuesMap.get("modelType")));
        map.put("dataModel",dataModel);
        return BaseResultEntity.success();
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
        if (modelComponents==null||modelComponents.size()==0)
            return paramValuesMap;
        for (DataComponentReq modelComponent : modelComponents) {
            for (DataComponentValue componentValue : modelComponent.getComponentValues()) {
                paramValuesMap.put(componentValue.getKey(),componentValue.getVal());
            }
        }
        return paramValuesMap;
    }

    public BaseResultEntity deleteModel(Long modelId) {
        ModelVo modelVo = dataModelRepository.queryModelById(modelId);
        if (modelVo==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"未查询到模型信息");
        }

        if (modelVo.getIsDraft()==1){
            Map map = dataModelRepository.queryModelLatestTask(new HashSet() {{
                add(modelId);
            }});
            if (map!=null&&!map.isEmpty()){
                return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"该模型有任务");
            }
        }
        dataModelPrRepository.deleteModelByModelId(modelId,modelVo.getIsDraft());
        return BaseResultEntity.success();
    }

    public BaseResultEntity runTaskModel(Long modelId,Long userId) {
        DataModel dataModel = dataModelRepository.queryDataModelById(modelId);
        if (dataModel==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未查询到模型");
        if (dataModel.getIsDraft()==0)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"模型未保存,请保存后再次尝试");
        if (StringUtils.isBlank(dataModel.getComponentJson()))
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未查询到模型组件信息");
        if (StringUtils.isBlank(dataModel.getModelName()))
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"模型名称不能为空");
        if (dataModel.getTrainType()==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"模型训练类型不能为空");
        Map<Long, Map<String, Object>> taskMap = dataModelRepository.queryModelLatestTask(new HashSet() {{
            add(modelId);
        }});
        if (taskMap!=null&&!taskMap.isEmpty()){
            Map<String, Object> dataMap = taskMap.get(modelId);
            if (dataMap.get("taskState")!=null&&Integer.parseInt(dataMap.get("taskState").toString())==2)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"任务正在运行中");
        }
        DataModelAndComponentReq params = JSONObject.parseObject(dataModel.getComponentJson(),DataModelAndComponentReq.class);
        Map<String, Object> map = new HashMap<>();
        BaseResultEntity extracteModelResultEntity = extracteModelData(dataModel, params, map);
        if (extracteModelResultEntity.getCode() != 0) {
            return extracteModelResultEntity;
        }
        BaseResultEntity extracteComponentResultEntity = extracteComponentData(dataModel, params, map);
        if (extracteComponentResultEntity.getCode() != 0) {
            return extracteComponentResultEntity;
        }
        DataTask dataTask = new DataTask();
        dataTask.setTaskIdName(UUID.randomUUID().toString());
        dataTask.setTaskType(TaskTypeEnum.MODEL.getTaskType());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTask.setTaskState(TaskStateEnum.INIT.getStateType());
        dataTask.setTaskUserId(userId);
        dataTaskPrRepository.saveDataTask(dataTask);
        // 保存模型信息
        dataModel = (DataModel) map.get("dataModel");
//        dataModelPrRepository.deleteDataModelResource(dataModel.getModelId());
//        dataModelPrRepository.deleteDataComponent(dataModel.getModelId());
        List<DataComponent> dataComponents = (List<DataComponent>) map.get("dataComponents");
        for (DataComponent dataComponent : dataComponents) {
            dataComponent.setModelId(dataModel.getModelId());
            dataComponent.setTaskId(dataTask.getTaskId());
            dataModelPrRepository.saveDataComponent(dataComponent);
        }
//        dataModelPrRepository.deleteDataModelComponent(dataModel.getModelId());
        Map<String, DataComponent> dataComponentMap = dataComponents.stream().collect(Collectors.toMap(DataComponent::getComponentCode, Function.identity()));
        List<DataModelComponent> dataModelComponents = (List<DataModelComponent>) map.get("dataModelComponents");
        for (DataModelComponent dataModelComponent : dataModelComponents) {
            dataModelComponent.setModelId(dataModel.getModelId());
            dataModelComponent.setTaskId(dataTask.getTaskId());
            dataModelComponent.setInputComponentId(dataModelComponent.getInputComponentCode() == null ? null : dataComponentMap.get(dataModelComponent.getInputComponentCode()) == null ? null : dataComponentMap.get(dataModelComponent.getInputComponentCode()).getComponentId());
            dataModelComponent.setOutputComponentId(dataModelComponent.getInputComponentCode() == null ? null : dataComponentMap.get(dataModelComponent.getOutputComponentCode()) == null ? null : dataComponentMap.get(dataModelComponent.getOutputComponentCode()).getComponentId());
            dataModelPrRepository.saveDataModelComponent(dataModelComponent);
        }
        // 重新组装json
        dataModel.setComponentJson(formatModelComponentJson(params, dataComponentMap));
        dataModel.setIsDraft(1);
        dataModelPrRepository.updateDataModel(dataModel);
        DataModelTask modelTask = new DataModelTask();
        modelTask.setTaskId(dataTask.getTaskId());
        modelTask.setModelId(dataModel.getModelId());
        dataModelPrRepository.saveDataModelTask(modelTask);
        modelInitService.runModelTaskFeign(dataModel,dataTask,modelTask);
        Map<String,Object> returnMap = new HashMap<>();
        returnMap.put("modelId",modelId);
        returnMap.put("taskId",dataTask.getTaskId());
        return BaseResultEntity.success(returnMap);
    }

    public BaseResultEntity restartTaskModel(Long taskId) {
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (dataTask==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未查询到任务信息");
        DataModelTask modelTask = dataModelRepository.queryModelTaskById(taskId);
        if (modelTask==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未查询到模型任务信息");
        DataModel dataModel = dataModelRepository.queryDataModelById(modelTask.getModelId());
        if (dataModel==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未查询到模型信息");
        dataTask.setTaskErrorMsg("");
        dataTask.setTaskState(TaskStateEnum.INIT.getStateType());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTask.setTaskEndTime(System.currentTimeMillis());
        dataTaskPrRepository.updateDataTask(dataTask);
        modelInitService.runModelTaskFeign(dataModel,dataTask,modelTask);
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
        if (dataTask.getTaskState()==0)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"请检查任务运行状态");
        DataModelTask modelTask = dataModelRepository.queryModelTaskById(taskId);
        if (modelTask==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        }
        if (StringUtils.isBlank(modelTask.getComponentJson())){
            return BaseResultEntity.success();
        }
        List<DataComponent> dataComponents = JSONObject.parseArray(modelTask.getComponentJson(),DataComponent.class);
        if (dataComponents.size()==0)
            return BaseResultEntity.success();
        Map<String,Object> map = new HashMap<>();
        map.put("taskState",dataTask.getTaskState());
        map.put("components",dataComponents.stream().map(DataModelConvert::dataComponentPoConvertDataComponentVo).collect(Collectors.toList()));
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getModelPrediction(Long modelId){
        Map result=new HashMap();
        result.put("prediction",TestConfiguration.temp);
        return BaseResultEntity.success(result);
    }


    public BaseResultEntity syncModel(ShareModelVo vo) {
        log.info(JSONObject.toJSONString(vo));
        if (vo.getDataModel()==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"dataModel");
        if (vo.getDataModelTask()==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"dataModelTask");
        if (vo.getDataTask()==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"dataTask");
        if (StringUtils.isBlank(vo.getDataModel().getModelUUID()))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelUUID");
        if (vo.getDmrList()==null||vo.getDmrList().isEmpty())
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"dmrList");
        DataModel dataModel = this.dataModelRepository.queryDataModelByUUID(vo.getDataModel().getModelUUID());
        if (dataModel==null){
            DataProject dataProject = this.dataProjectRepository.selectDataProjectByProjectId(null, vo.getProjectId());
            if (dataProject==null)
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"no project data");
            vo.getDataModel().setProjectId(dataProject.getId());
            dataModelPrRepository.saveDataModel(vo.getDataModel());
        }else {
            vo.getDataModel().setModelId(dataModel.getModelId());
            dataModelPrRepository.updateDataModel(vo.getDataModel());
        }
        vo.getDataTask().setIsCooperation(1);
        dataTaskPrRepository.saveDataTask(vo.getDataTask());
        vo.getDataModelTask().setTaskId(vo.getDataTask().getTaskId());
        vo.getDataModelTask().setModelId(vo.getDataModel().getModelId());
        dataModelPrRepository.saveDataModelTask(vo.getDataModelTask());
        for (DataModelResource dataModelResource : vo.getDmrList()) {
            dataModelResource.setModelId(vo.getDataModel().getModelId());
            dataModelResource.setTaskId(vo.getDataTask().getTaskId());
        }
        dataModelPrRepository.saveDataModelResource(vo.getDmrList());
        return BaseResultEntity.success();
    }


}
