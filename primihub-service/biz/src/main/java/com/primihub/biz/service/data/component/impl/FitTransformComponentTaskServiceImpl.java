package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.ComponentsConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dto.GrpcComponentDto;
import com.primihub.biz.entity.data.dto.ModelDerivationDto;
import com.primihub.biz.entity.data.po.DataModelResource;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.service.feign.FusionResourceService;
import com.primihub.sdk.task.TaskHelper;
import com.primihub.sdk.task.dataenum.ModelTypeEnum;
import com.primihub.sdk.task.param.TaskComponentParam;
import com.primihub.sdk.task.param.TaskParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

@Service("fitTransformComponentTaskServiceImpl")
@Slf4j
public class FitTransformComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
    @Autowired
    private ComponentsConfiguration componentsConfiguration;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private TaskHelper taskHelper;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private DataModelPrRepository dataModelPrRepository;
    @Override
    public BaseResultEntity check(DataComponentReq req, ComponentTaskReq taskReq) {
        return componentTypeVerification(req,componentsConfiguration.getModelComponents(),taskReq);
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        taskReq.getFreemarkerMap().putAll(getComponentVals(req.getComponentValues()));
        List<String> ids = taskReq.getFusionResourceList().stream().map(data -> data.get("resourceId").toString()).collect(Collectors.toList());
        List<ModelDerivationDto> newest = taskReq.getNewest();
        log.info("ids:{}", ids);
        String path = baseConfiguration.getRunModelFileUrlDirPrefix()+taskReq.getDataTask().getTaskIdName() + File.separator + "fitTransform";
        Map<String, GrpcComponentDto> fitTransformEntityMap = getGrpcComponentDataSetMap(taskReq.getFusionResourceList(),path);
        fitTransformEntityMap.remove(taskReq.getFreemarkerMap().get(DataConstant.PYTHON_ARBITER_DATASET));
        log.info("fitTransform-1:{}", JSONObject.toJSONString(fitTransformEntityMap));
        if (newest!=null && newest.size()!=0){
            ids = new ArrayList<>();
            for (ModelDerivationDto modelDerivationDto : newest) {
                ids.add(modelDerivationDto.getNewResourceId());
                fitTransformEntityMap.put(modelDerivationDto.getNewResourceId(),fitTransformEntityMap.get(modelDerivationDto.getOriginalResourceId()));
                fitTransformEntityMap.remove(modelDerivationDto.getOriginalResourceId());
            }
            log.info("newids:{}", ids);
        }
        try {
            GrpcComponentDto labelDatasetDto = fitTransformEntityMap.get(taskReq.getFreemarkerMap().get(DataConstant.PYTHON_LABEL_DATASET));
            taskReq.getFreemarkerMap().put("new_"+DataConstant.PYTHON_LABEL_DATASET,labelDatasetDto.getNewDataSetId());
            taskReq.getFreemarkerMap().put("new_"+DataConstant.PYTHON_LABEL_DATASET+"_path",labelDatasetDto.getOutputFilePath());
            GrpcComponentDto guestDatasetDto = fitTransformEntityMap.get(taskReq.getFreemarkerMap().get(DataConstant.PYTHON_GUEST_DATASET));
            taskReq.getFreemarkerMap().put("new_"+DataConstant.PYTHON_GUEST_DATASET,guestDatasetDto.getNewDataSetId());
            taskReq.getFreemarkerMap().put("new_"+DataConstant.PYTHON_GUEST_DATASET+"_path",guestDatasetDto.getOutputFilePath());
            TaskParam<TaskComponentParam> taskParam = new TaskParam<>(new TaskComponentParam());
            taskParam.setTaskId(taskReq.getDataTask().getTaskIdName());
            taskParam.setJobId(String.valueOf(taskReq.getJob()));
            taskParam.getTaskContentParam().setFitTransform(true);
            taskParam.getTaskContentParam().setModelType(ModelTypeEnum.MODEL_TYPE_MAP.get(taskReq.getDataModel().getModelType()));
            taskParam.getTaskContentParam().setFreemarkerMap(taskReq.getFreemarkerMap());
            taskHelper.submit(taskParam);
            if(!taskParam.getSuccess()){
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"组件处理失败:"+taskParam.getError());
            }else {
                List<ModelDerivationDto> derivationList = new ArrayList<>();
                log.info("fitTransform-3:{}",JSONObject.toJSONString(fitTransformEntityMap));
                Iterator<String> keyi = fitTransformEntityMap.keySet().iterator();
                while (keyi.hasNext()){
                    String key = keyi.next();
                    GrpcComponentDto value = fitTransformEntityMap.get(key);
                    if (value==null) {
                        continue;
                    }
                    log.info("value:{}",JSONObject.toJSONString(value));
                    derivationList.add(new ModelDerivationDto(key,"fitTransform","缺失值填充",value.getNewDataSetId(),value.getOutputFilePath(),value.getDataSetId()));
                    log.info("derivationList:{}",JSONObject.toJSONString(derivationList));
                }
                taskReq.getDerivationList().addAll(derivationList);
                taskReq.setNewest(derivationList);
                // derivation resource datas
                log.info(JSONObject.toJSONString(taskReq.getDerivationList()));
                BaseResultEntity derivationResource = dataResourceService.saveDerivationResource(derivationList, taskReq.getDataTask().getTaskUserId());
                log.info(JSONObject.toJSONString(derivationResource));
                if (!derivationResource.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"组件处理失败:"+derivationResource.getMsg());
                }else {
                    List<String> resourceIdLst = (List<String>)derivationResource.getResult();
                    for (String resourceId : resourceIdLst) {
                        DataModelResource dataModelResource = new DataModelResource(taskReq.getDataModel().getModelId());
                        dataModelResource.setTaskId(taskReq.getDataTask().getTaskId());
                        dataModelResource.setResourceId(resourceId);
                        dataModelResource.setTakePartType(1);
                        dataModelPrRepository.saveDataModelResource(dataModelResource);
                        taskReq.getDmrList().add(dataModelResource);
                    }
                }
            }
            HashSet dids = new HashSet(){{
                add(labelDatasetDto.getNewDataSetId());
                add(guestDatasetDto.getNewDataSetId());
            }};
            while (true){
                // 休眠一秒等待数据集同步
                BaseResultEntity dataSets = fusionResourceService.getDataSets(dids);
                log.info(JSONObject.toJSONString(dataSets));
                if (dataSets.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
                    List<Object> objectList = (List<Object>) dataSets.getResult();
                    if (objectList.size() == dids.size()){
                        break;
                    }
                }
                Thread.sleep(100L);
            }

        }catch (Exception e){
            e.printStackTrace();
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg("缺失值填充错误:"+e.getMessage());
        }

        return BaseResultEntity.success();
    }
}
