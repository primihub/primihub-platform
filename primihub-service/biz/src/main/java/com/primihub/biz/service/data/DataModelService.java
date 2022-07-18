package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.test.TestConfiguration;
import com.primihub.biz.convert.DataModelConvert;
import com.primihub.biz.convert.DataTaskConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataModelRepository;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import com.primihub.biz.service.sys.SysOrganService;
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
                        modelResourceVo.setFileNum(map.get("resourceRowsCount")==null?0:Integer.valueOf(map.get("resourceRowsCount").toString()));
                        modelResourceVo.setAlignmentNum(modelResourceVo.getFileNum());
                        modelResourceVo.setPrimitiveParamNum(map.get("resourceColumnCount")==null?0:Integer.valueOf(map.get("resourceColumnCount").toString()));
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
        map.put("anotherQuotas",new HashMap(){
            {
                put("meanSquaredError",4.725711343212531);
                put("explainedVariance",0.15919721839861312);
                put("meanAbsoluteError",1.9888892110900143);
                put("meanSquaredLogError",0.5848543124612581);
                put("medianAbsoluteError",1.3484169265390695);
                put("r2Score",-4.1600402950393045);
                put("rootMeanSquaredError",22.33234769936758);
            }});
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
        if (dataModel.getLatestTaskStatus()!=null&&dataModel.getLatestTaskStatus()==1)
            return true;
        return false;
    }

    public BaseResultEntity saveModelAndComponent(Long userId, DataModelAndComponentReq params) {
        if (modelIsRunTask(params.getModelId()))
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL, "模型运行任务中");
        DataModel dataModel = DataModelConvert.dataModelReqConvertPo(params, userId);
        Map<String, Object> map = new HashMap<>();
        try {
            if (params.getIsDraft() == 0) {
                // 草稿
                saveOrGetModelComponentCache(true, userId, params, dataModel);
            } else {
                // 保存
                if (dataModel.getModelId() == null)
                    return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "modelId");
                BaseResultEntity extracteModelResultEntity = extracteModelData(dataModel, params, map);
                if (extracteModelResultEntity.getCode() != 0) {
                    return extracteModelResultEntity;
                }
                BaseResultEntity extracteComponentResultEntity = extracteComponentData(dataModel, params, map);
                if (extracteComponentResultEntity.getCode() != 0) {
                    return extracteComponentResultEntity;
                }
                // 保存模型信息
                dataModel = (DataModel) map.get("dataModel");
                dataModelPrRepository.deleteDataModelResource(dataModel.getModelId());
                dataModelPrRepository.deleteDataComponent(dataModel.getModelId());
                List<DataComponent> dataComponents = (List<DataComponent>) map.get("dataComponents");
                for (DataComponent dataComponent : dataComponents) {
                    dataComponent.setModelId(dataModel.getModelId());
                    dataModelPrRepository.saveDataComponent(dataComponent);
                }
                dataModelPrRepository.deleteDataModelComponent(dataModel.getModelId());
                Map<String, DataComponent> dataComponentMap = dataComponents.stream().collect(Collectors.toMap(DataComponent::getComponentCode, Function.identity()));
                List<DataModelComponent> dataModelComponents = (List<DataModelComponent>) map.get("dataModelComponents");
                for (DataModelComponent dataModelComponent : dataModelComponents) {
                    dataModelComponent.setModelId(dataModel.getModelId());
                    dataModelComponent.setInputComponentId(dataModelComponent.getInputComponentCode() == null ? null : dataComponentMap.get(dataModelComponent.getInputComponentCode()) == null ? null : dataComponentMap.get(dataModelComponent.getInputComponentCode()).getComponentId());
                    dataModelComponent.setOutputComponentId(dataModelComponent.getInputComponentCode() == null ? null : dataComponentMap.get(dataModelComponent.getOutputComponentCode()) == null ? null : dataComponentMap.get(dataModelComponent.getOutputComponentCode()).getComponentId());
                    dataModelPrRepository.saveDataModelComponent(dataModelComponent);
                }
                // 重新组装json
                dataModel.setComponentJson(formatModelComponentJson(params, dataComponentMap));
                dataModel.setResourceNum(2);
                dataModelPrRepository.updateDataModel(dataModel);
            }
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
                return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"流程不合规");
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
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"组件为空");
        if(StringUtils.isBlank(params.getModelName()))
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"模型名称不可以为空");
        if (params.getProjectId()==null||params.getProjectId()==0L)
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"缺少项目");
        DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(params.getProjectId(), null);
        if (dataProject==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"找不到项目");
        // 获取项目、资源、y值字段相关信息
        Map<String, String> paramValuesMap = getDataAlignmentComponentVals(modelComponents);
        dataModel.setModelName(params.getModelName());
        dataModel.setModelDesc(params.getModelDesc());
        dataModel.setProjectId(params.getProjectId());
        //资源
        if (StringUtils.isBlank(paramValuesMap.get("selectData")))
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"缺少资源");
        // 模型类型
        if (StringUtils.isBlank(paramValuesMap.get("modelType")))
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"缺少模型类型");
        dataModel.setModelType(Integer.parseInt(paramValuesMap.get("modelType")));
        map.put("dataModel",dataModel);
        return BaseResultEntity.success();
    }

    public BaseResultEntity getModelComponentDetail(Long modelId, Long userId) {
        return BaseResultEntity.success(getModelComponentReq(modelId,userId));
    }

    public DataModelAndComponentReq getModelComponentReq(Long modelId, Long userId){
        DataModelAndComponentReq req = null;
        if (modelId==null||modelId==0L){
            req = saveOrGetModelComponentCache(false,userId, null,null);
        }else{
            ModelComponentJson modelComponent = dataModelRepository.queryModelComponenJsonByUserId(userId,0,modelId);
            if (modelComponent!=null) {
                if (StringUtils.isNotBlank(modelComponent.getComponentJson())) {
                    req = JSONObject.parseObject(modelComponent.getComponentJson(), DataModelAndComponentReq.class);
                    req.setModelId(modelComponent.getModelId().toString());
                }
            }
        }
        return req;
    }

    private DataModelAndComponentReq saveOrGetModelComponentCache(boolean isSave,Long userId,DataModelAndComponentReq params,DataModel dataModel){
        ModelComponentJson modelComponent = dataModelRepository.queryModelComponenJsonByUserId(userId,0,null);
        if (isSave){
            dataModel.setComponentJson(JSONObject.toJSONString(params));
            if (modelComponent==null&&(dataModel.getModelId()==null||dataModel.getModelId()==0L)){
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
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"模型已生成");
        }
        dataModelPrRepository.deleteModelByModelId(modelId);
        return BaseResultEntity.success();
    }

    public BaseResultEntity runTaskModel(Long modelId,Long userId) {
        DataModel dataModel = dataModelRepository.queryDataModelById(modelId);
        if (dataModel==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未查询到模型");
        }
        if (dataModel.getIsDraft()!=1)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"模型未保存");
        DataTask dataTask = new DataTask();
        dataTask.setTaskIdName(UUID.randomUUID().toString());
        dataTask.setTaskType(TaskTypeEnum.MODEL.getTaskType());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTask.setTaskState(TaskStateEnum.INIT.getStateType());
        dataTask.setTaskUserId(userId);
        dataTaskPrRepository.saveDataTask(dataTask);
        modelInitService.runModelTaskFeign(dataModel,dataTask);
        Map<String,Object> map = new HashMap<>();
        map.put("modelId",modelId);
        map.put("taskId",dataTask.getTaskId());
        return BaseResultEntity.success(map);
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
        return BaseResultEntity.success(dataComponents.stream().map(DataModelConvert::dataComponentPoConvertDataComponentVo).collect(Collectors.toList()));
    }

    public BaseResultEntity getModelPrediction(Long modelId){
        Map result=new HashMap();
        result.put("prediction",TestConfiguration.temp);
        return BaseResultEntity.success(result);
    }


}
