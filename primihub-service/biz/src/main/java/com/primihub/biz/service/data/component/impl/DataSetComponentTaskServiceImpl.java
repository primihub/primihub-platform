package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.po.DataModel;
import com.primihub.biz.entity.data.po.DataModelResource;
import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectResource;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.entity.data.vo.ModelProjectResourceVo;
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.service.data.FusionResourceService;
import com.primihub.biz.service.data.component.ComponentTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("dataSetComponentTaskServiceImpl")
@Slf4j
public class DataSetComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private DataProjectRepository dataProjectRepository;
    @Autowired
    private DataModelPrRepository dataModelPrRepository;
    @Autowired
    private FusionResourceService fusionResourceService;

    @Override
    public BaseResultEntity check(DataComponentReq req, ComponentTaskReq taskReq) {
        BaseResultEntity baseResultEntity = componentTypeVerification(req, baseConfiguration.getModelComponents(),taskReq);
        if (!baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode()))
            return baseResultEntity;
        Map<String,String> valMap = (Map<String,String>)baseResultEntity.getResult();
        try {
            List<ModelProjectResourceVo> resourceList = JSONObject.parseArray(valMap.get("selectData"), ModelProjectResourceVo.class);
            if (resourceList==null || resourceList.size()<=1)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未选择资源或资源缺少");
            DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(taskReq.getDataModel().getProjectId(), null);
            if (dataProject==null)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未查询到项目信息");
            List<DataProjectResource> dataProjectResources = dataProjectRepository.selectProjectResourceByProjectId(dataProject.getProjectId());
            if (dataProjectResources.isEmpty())
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"未查询到项目资源信息");
            Set<String> resourceIds = dataProjectResources.stream().filter(dpr -> dpr.getAuditStatus() == 1).map(DataProjectResource::getResourceId).collect(Collectors.toSet());
            for (ModelProjectResourceVo mprv : resourceList) {
                if (!resourceIds.contains(mprv.getResourceId()))
                    return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"资源["+mprv.getResourceName()+"]审核未通过或移除,不可使用");
            }
            String[] modelResourceIds = resourceList.stream().map(ModelProjectResourceVo::getResourceId).toArray(String[]::new);
            BaseResultEntity baseResult = fusionResourceService.getResourceListById(dataProjectResources.get(0).getServerAddress(), modelResourceIds);
            if (baseResult.getCode()!=0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"联邦资源查询失败:"+baseResult.getMsg());
            List<LinkedHashMap<String,Object>> voList = (List<LinkedHashMap<String,Object>>)baseResult.getResult();
            if (voList == null && voList.size()==0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"联邦资源查询失败:无数据信息");
            List<LinkedHashMap<String, Object>> availableList = voList.stream().filter(data -> Integer.valueOf(data.get("available").toString()).equals("1")).collect(Collectors.toList());
            if (!availableList.isEmpty())
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"联邦资源["+availableList.get(0).get("resourceId").toString()+"],不可使用");
        }catch (Exception e){
            log.info("modelId:{} Failed to convert JSON :{}",taskReq.getDataModel().getModelId(),e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"模型选择资源转换失败");
        }
        return BaseResultEntity.success();
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        List<ModelProjectResourceVo> resourceList = JSONObject.parseArray(req.getComponentValues().get(0).getVal(), ModelProjectResourceVo.class);
        taskReq.setResourceList(resourceList);
        for (ModelProjectResourceVo modelProjectResourceVo : resourceList) {
            if (modelProjectResourceVo.getParticipationIdentity()==1){
                taskReq.getFreemarkerMap().put(DataConstant.PYTHON_LABEL_DATASET,modelProjectResourceVo.getResourceId());
                String fileNaame = StringUtils.isBlank(modelProjectResourceVo.getCalculationField())?"Class":modelProjectResourceVo.getCalculationField();
                taskReq.getFreemarkerMap().put(DataConstant.PYTHON_CALCULATION_FIELD,fileNaame);
                taskReq.getDataModel().setYValueColumn(fileNaame);
                dataModelPrRepository.updateDataModel(taskReq.getDataModel());
            }else {
                taskReq.getFreemarkerMap().put(DataConstant.PYTHON_GUEST_DATASET , modelProjectResourceVo.getResourceId());
            }
            DataModelResource dataModelResource = new DataModelResource(taskReq.getDataModel().getModelId());
            dataModelResource.setTaskId(taskReq.getDataTask().getTaskId());
            dataModelResource.setResourceId(modelProjectResourceVo.getResourceId());
            taskReq.getDmrList().add(dataModelResource);
        }
        dataModelPrRepository.saveDataModelResource(taskReq.getDmrList());
        return BaseResultEntity.success();
    }
}


