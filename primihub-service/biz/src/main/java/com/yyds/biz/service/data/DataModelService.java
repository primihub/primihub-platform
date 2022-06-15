package com.yyds.biz.service.data;

import com.alibaba.fastjson.JSONObject;
import com.yyds.biz.config.base.BaseConfiguration;
import com.yyds.biz.convert.DataModelConvert;
import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.base.BaseResultEnum;
import com.yyds.biz.entity.base.PageDataEntity;
import com.yyds.biz.entity.data.po.*;
import com.yyds.biz.entity.data.req.*;
import com.yyds.biz.entity.data.vo.*;
import com.yyds.biz.entity.sys.po.SysOrgan;
import com.yyds.biz.repository.primarydb.data.DataModelPrRepository;
import com.yyds.biz.repository.secondarydb.data.DataModelRepository;
import com.yyds.biz.repository.secondarydb.data.DataProjectRepository;
import com.yyds.biz.repository.secondarydb.data.DataResourceRepository;
import com.yyds.biz.service.sys.SysOrganService;
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
    private SysOrganService sysOrganService;
    @Autowired
    private ModelInitService modelInitService;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private DataResourceRepository dataResourceRepository;

    public BaseResultEntity saveDataModel(Long userId, Long organId, DataModelReq req) {
        DataModel dataModel = DataModelConvert.dataModelReqConvertPo(req, userId, organId);
        BaseResultEntity baseResultEntity = modelProjectResourceAuthed(dataModel, req.getProjectId());
        if (baseResultEntity.getCode()!=0){
            baseResultEntity.setResult(null);
            return baseResultEntity;
        }
//        initialSampleDataModel(dataModel);
        dataModelPrRepository.saveDataModel(dataModel);
        List<DataProjectResource> dataProjectResources = (List<DataProjectResource>)baseResultEntity.getResult();
        // 资源关联信息处理
        List<DataModelResource> dmrList = new ArrayList<>();
        for (DataProjectResource dpr : dataProjectResources) {
            DataModelResource dataModelResource = new DataModelResource(dataModel.getModelId(), dpr.getResourceId());
            // TODO 模型详情模拟数据-后续删除 liwiehua
            dataModelResource.setAlignmentNum(getrandom(1,10000));
            dataModelResource.setPrimitiveParamNum(getrandom(1,100));
            dataModelResource.setModelParamNum(getrandom(1,100));
            dmrList.add(dataModelResource);
//            dmrList.add(new DataModelResource(dataModel.getModelId(),dpr.getResourceId()));
        }
        dataModelPrRepository.saveDataModelResource(dmrList);
        // TODO 模型详情模拟数据-后续删除 liwiehua
//        initialSampleDataModelQuota(dataModel.getModelId());
        modelInitService.updateDataModel(dataModel);
        Map<String,Object> map = new HashMap<>();
        map.put("modelId",dataModel.getModelId());
        map.put("modelName",dataModel.getModelName());
        map.put("modelDesc",dataModel.getModelDesc());
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getDataModel(Long modelId) {
        // 模型信息
        ModelVo modelVo = dataModelRepository.queryModelById(modelId);
        if (modelVo==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        }
        // 查询资源模型
        List<ModelResourceVo> modelResourceVos = dataModelRepository.queryModelResource(modelId);
        // TODO 补充modelResourceVos 机构名称 - 完成
        Set<Long> organIds = modelResourceVos.stream().map(ModelResourceVo::getOrganId).collect(Collectors.toSet());
        Map<Long, SysOrgan> sysOrganMap = sysOrganService.getSysOrganMap(organIds);
        modelResourceVos.forEach(vo->{
            SysOrgan sysOrgan = sysOrganMap.get(vo.getOrganId());
            vo.setOrganName(sysOrgan==null?"":sysOrgan.getOrganName());
        });
        // 模型评估
        List<ModelQuotaVo> modelQuotaVos = dataModelRepository.queryModelQuotaVoList(modelId);
        Map<String,Object> map = new HashMap();
        List<DataComponent> dataComponents = dataModelRepository.queryModelComponentByParams(modelId, null);
        map.put("model",modelVo);
        if (dataComponents.size()!=0){
            Integer sumTime = dataComponents.stream().collect(Collectors.summingLong(DataComponent::getTimeConsuming)).intValue();
            List<DataModelComponentVo> dataModelComponents = dataComponents.stream().map(dataComponent -> DataModelConvert.dataComponentPoConvertDataModelComponentVo(dataComponent, sumTime)).collect(Collectors.toList());
            map.put("modelComponent",dataModelComponents);
        }
        map.put("modelResources",modelResourceVos);
        map.put("modelQuotas",modelQuotaVos);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getDataModelList(Long userId, Long organId, PageReq req,String projectName,String modelName, Integer taskStatus) {
        List<ModelListVo> modelListVos = dataModelRepository.queryModelList(userId, organId, req.getPageSize(), req.getOffset(),projectName,modelName,taskStatus);
        if (modelListVos.size()==0){
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Integer tolal = dataModelRepository.queryModelListCount(userId, organId,projectName,modelName,taskStatus);
        return BaseResultEntity.success(new PageDataEntity(tolal,req.getPageSize(),req.getPageNo(),modelListVos));
    }

    public static int getrandom(int start,int end) {
        int num=(int) (Math.random()*(end-start+1)+start);
        return num;
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



    public BaseResultEntity saveModelAndComponent(Long userId, Long organId, DataModelAndComponentReq params) {
        if (modelIsRunTask(params.getModelId()))
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL, "模型运行任务中");
        DataModel dataModel = DataModelConvert.dataModelReqConvertPo(params, userId, organId);
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
                List<DataModelResource> dmrList = (List<DataModelResource>) map.get("dmrList");
                dataModelPrRepository.deleteDataModelResource(dataModel.getModelId());
                dataModelPrRepository.saveDataModelResource(dmrList);
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
        if(StringUtils.isBlank(params.getModelDesc()))
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"模型描述不可以为空");
        List<DataComponentReq> modelComponents = params.getModelComponents();
        if (modelComponents==null||modelComponents.size()==0)
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"组件为空");
        // 获取项目、资源、y值字段相关信息
        Map<String, String> paramValuesMap = getDataAlignmentComponentVals(modelComponents);
        if (StringUtils.isBlank(paramValuesMap.get("modelName")))
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"缺少模型名称");
        dataModel.setModelName(paramValuesMap.get("modelName"));
        if (StringUtils.isBlank(paramValuesMap.get("projectId")))
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"缺少项目");
        DataProject dataProject = dataProjectRepository.queryDataProjectById(Long.parseLong(paramValuesMap.get("projectId")));
        if (dataProject==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"找不到项目");
        dataModel.setProjectId(dataProject.getProjectId());
        //资源
        if (StringUtils.isBlank(paramValuesMap.get("selectData")))
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"缺少资源");
        List<DataProjectResource> projectResource = dataProjectRepository.queryProjectResourceByProjectId(dataProject.getProjectId()).stream().filter(pr -> pr.getResourceId().compareTo(Long.parseLong(paramValuesMap.get("selectData"))) == 0).collect(Collectors.toList());
        if (projectResource.size()==0){
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"找不到资源");
        }
//        DataProjectResource dataProjectResource = projectResource.get(0);
        // y 值
        if (StringUtils.isBlank(paramValuesMap.get("yField")))
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"缺少Y值字段");
        dataModel.setYValueColumn(paramValuesMap.get("yField"));
        // 模型类型
        if (StringUtils.isBlank(paramValuesMap.get("modelType")))
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"缺少模型类型");
        dataModel.setModelType(Integer.parseInt(paramValuesMap.get("modelType")));
        BaseResultEntity baseResultEntity = modelProjectResourceAuthed(dataModel, dataProject.getProjectId());
        if (baseResultEntity.getCode()!=0){
            baseResultEntity.setResult(null);
            return baseResultEntity;
        }

        map.put("dataModel",dataModel);
        List<DataProjectResource> dataProjectResources = (List<DataProjectResource>)baseResultEntity.getResult();
        // 资源关联信息处理
        List<DataModelResource> dmrList = new ArrayList<>();
        for (DataProjectResource dpr : dataProjectResources) {
            // TODO 模型详情模拟数据-后续删除 liwiehua
            DataModelResource dataModelResource = new DataModelResource(dataModel.getModelId(), dpr.getResourceId());
            dataModelResource.setAlignmentNum(getrandom(1,10000));
            dataModelResource.setPrimitiveParamNum(getrandom(1,100));
            dataModelResource.setModelParamNum(getrandom(1,100));
            dmrList.add(dataModelResource);
//            dmrList.add(new DataModelResource(dataModel.getModelId(),dpr.getResourceId()));
        }
        map.put("dmrList",dmrList);
        return BaseResultEntity.success();
    }

    private BaseResultEntity modelProjectResourceAuthed(DataModel dataModel,Long projectId){
        List<DataProjectResource> dataProjectResources = dataProjectRepository.queryProjectResourceByProjectId(projectId);
        if (dataProjectResources.size()==0){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"没有资源数据");
        }
        // 判断是否都授权了
        boolean isAuthed = true;
        for (DataProjectResource dpr : dataProjectResources) {
            if(dpr.getIsAuthed()==0){
                isAuthed = false;
                break;
            }
        }
        if (!isAuthed){
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"存在未授权的资源");
        }
        dataModel.setResourceNum(dataProjectResources.size());
        dataModel.dataInit();
        return BaseResultEntity.success(dataProjectResources);
    }




    public BaseResultEntity getModelComponentDetail(Long modelId, Long userId, Long organId) {
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
        return BaseResultEntity.success(req);
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

    public BaseResultEntity runTaskModel(Long modelId, Long userId, Long organId) {
        DataModel dataModel = dataModelRepository.queryDataModelById(modelId);
        if (dataModel==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未查询到模型");
        }
        if (dataModel.getIsDraft()!=1)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"模型未保存");
//        // 处理模拟数据
//        modelInitService.runModelTask(dataModel);
        if (dataModel.getLatestTaskStatus()==1||dataModel.getLatestTaskStatus()==2)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"模型已运行或运行完成");
        dataModel.setLatestTaskStatus(1);
        dataModelPrRepository.updateDataModel(dataModel);
        // feign 调用
        modelInitService.runModelTaskFeign(dataModel);

        Map<String,Object> map = new HashMap<>();
        map.put("modelId",modelId);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getTaskModelComponent(Long modelId, Long userId, Long organId) {
        DataModel dataModel = dataModelRepository.queryDataModelById(modelId);
        if (dataModel==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        if (dataModel.getLatestTaskStatus()==0)
            return BaseResultEntity.success();
        List<DataComponent> dataComponents = dataModelRepository.queryModelComponentByParams(modelId, null);
        if (dataComponents.size()==0)
            return BaseResultEntity.success();
        return BaseResultEntity.success(dataComponents.stream().map(DataModelConvert::dataComponentPoConvertDataComponentVo).collect(Collectors.toList()));
    }
}
