package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSON;
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
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 缺失值填充
 */
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
        return componentTypeVerification(req, componentsConfiguration.getModelComponents(), taskReq);
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        // 先选择进来的数据
        List<ModelDerivationDto> newest = taskReq.getNewest();
        List<String> ids = null;
        if (CollectionUtils.isNotEmpty(newest)) {
            ids = new ArrayList<>();
            for (ModelDerivationDto modelDerivationDto : newest) {
                ids.add(modelDerivationDto.getNewResourceId());
            }
            log.info("newIds: {}", ids);
        }

        taskReq.getFreemarkerMap().putAll(getComponentVals(req.getComponentValues()));
        String path = baseConfiguration.getRunModelFileUrlDirPrefix() + taskReq.getDataTask().getTaskIdName() + File.separator + "fitTransform";
        Map<String, GrpcComponentDto> fitTransformEntityMap = getGrpcComponentDataSetMap(taskReq.getFusionResourceList(), path, taskReq, ids);
        fitTransformEntityMap.remove(taskReq.getFreemarkerMap().get(DataConstant.PYTHON_ARBITER_DATASET));

        String arbiterDataset = taskReq.getFreemarkerMap().get(DataConstant.PYTHON_ARBITER_DATASET).toString();
        Integer modelType = Integer.valueOf(taskReq.getValueMap().get("modelType"));
        log.info("[模型任务][模型组件] --- [ modelType: {} ]", modelType);
        ModelTypeEnum modelTypeEnum = ModelTypeEnum.MODEL_TYPE_MAP.get(modelType);
        if ("hetero_fitTransform.ftl".equals(modelTypeEnum.getFitTransformFtlPath())){
            taskReq.getFreemarkerMap().remove(DataConstant.PYTHON_ARBITER_DATASET);
        }
        log.info("fitTransform-1: \n{}", JSONObject.toJSONString(fitTransformEntityMap));
        log.info("[模型任务][缺失值组件] 缺失值组件任务提交的参数 [ freemarkerMap: \n{}\n]", JSON.toJSONString(taskReq.getFreemarkerMap()));
        try {
            GrpcComponentDto labelDatasetDto = fitTransformEntityMap.get(taskReq.getFreemarkerMap().get(DataConstant.PYTHON_LABEL_DATASET));
            taskReq.getFreemarkerMap().put("new_" + DataConstant.PYTHON_LABEL_DATASET, labelDatasetDto.getNewDataSetId());
            taskReq.getFreemarkerMap().put("new_" + DataConstant.PYTHON_LABEL_DATASET + "_path", labelDatasetDto.getOutputFilePath());
            GrpcComponentDto guestDatasetDto = fitTransformEntityMap.get(taskReq.getFreemarkerMap().get(DataConstant.PYTHON_GUEST_DATASET));
            taskReq.getFreemarkerMap().put("new_" + DataConstant.PYTHON_GUEST_DATASET, guestDatasetDto.getNewDataSetId());
            taskReq.getFreemarkerMap().put("new_" + DataConstant.PYTHON_GUEST_DATASET + "_path", guestDatasetDto.getOutputFilePath());
            TaskParam<TaskComponentParam> taskParam = new TaskParam<>(new TaskComponentParam());
            taskParam.setTaskId(taskReq.getDataTask().getTaskIdName());
            taskParam.setJobId(String.valueOf(taskReq.getJob()));
            taskParam.getTaskContentParam().setFitTransform(true);
            taskParam.getTaskContentParam().setModelType(ModelTypeEnum.MODEL_TYPE_MAP.get(taskReq.getDataModel().getModelType()));
            taskParam.getTaskContentParam().setFreemarkerMap(taskReq.getFreemarkerMap());
            taskHelper.submit(taskParam);
            if (!taskParam.getSuccess()) {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "组件处理失败:" + taskParam.getError());
            } else {
                List<ModelDerivationDto> derivationList = new ArrayList<>();
                log.info("fitTransform-3: \n{}", JSONObject.toJSONString(fitTransformEntityMap));


                for (String key : fitTransformEntityMap.keySet()) {
                    GrpcComponentDto value = fitTransformEntityMap.get(key);
                    if (value == null) {
                        continue;
                    }
                    log.info("value: \n{}", JSONObject.toJSONString(value));

                    String originResourceId = null;
                    if (CollectionUtils.isNotEmpty(taskReq.getNewest())) {
                        originResourceId = taskReq.getOriginResourceIdMap().get(key);
                    } else {
                        originResourceId = key;
                    }
                    derivationList.add(new ModelDerivationDto(key,
                            ModelDerivationDto.ModelDerivationTypeEnum.FIT_TRANS_FORM.getCode(),
                            ModelDerivationDto.ModelDerivationTypeEnum.FIT_TRANS_FORM.getDesc(),
                            value.getNewDataSetId(), value.getOutputFilePath(),
                            originResourceId
                    ));
                    taskReq.getOriginResourceIdMap().put(value.getNewDataSetId(), originResourceId);
                }
                log.info("derivationList: \n{}", JSONObject.toJSONString(derivationList));
                taskReq.getDerivationList().addAll(derivationList);
                taskReq.setNewest(derivationList);
                // derivation resource data
                log.info("after dataAlign taskReq derivationList: \n{}", JSONObject.toJSONString(taskReq.getDerivationList()));
                BaseResultEntity derivationResource = dataResourceService.saveDerivationResource(derivationList, taskReq.getDataTask().getTaskUserId());
                log.info("dataAlign derivationList save result: \n{}", JSONObject.toJSONString(derivationResource));
                if (!derivationResource.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())) {
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "组件处理失败:" + derivationResource.getMsg());
                } else {
                    /*HashSet dids = new HashSet();
                    dids.add(labelDatasetDto.getNewDataSetId());
                    dids.add(guestDatasetDto.getNewDataSetId());
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
                    }*/

                    List<String> resourceIdLst = (List<String>) derivationResource.getResult();
                    for (String resourceId : resourceIdLst) {
                        DataModelResource dataModelResource = new DataModelResource(taskReq.getDataModel().getModelId());
                        dataModelResource.setTaskId(taskReq.getDataTask().getTaskId());
                        dataModelResource.setResourceId(resourceId);
                        dataModelResource.setTakePartType(1);
                        dataModelPrRepository.saveDataModelResource(dataModelResource);
                        taskReq.getDmrList().add(dataModelResource);
                    }

                    // 更新 label_dataset, guest_dataset
                    Map<String, String> derivationResourceIdMap = derivationList.stream().collect(Collectors.toMap(
                            ModelDerivationDto::getResourceId, ModelDerivationDto::getNewResourceId
                    ));
                    String labelDatasetId = (String) taskReq.getFreemarkerMap().get(DataConstant.PYTHON_LABEL_DATASET);
                    String guestDatasetId = (String) taskReq.getFreemarkerMap().get(DataConstant.PYTHON_GUEST_DATASET);
                    if (derivationResourceIdMap.containsKey(labelDatasetId)) {
                        taskReq.getFreemarkerMap().put(DataConstant.PYTHON_LABEL_DATASET, derivationResourceIdMap.get(labelDatasetId));
                    }
                    if (derivationResourceIdMap.containsKey(guestDatasetId)) {
                        taskReq.getFreemarkerMap().put(DataConstant.PYTHON_GUEST_DATASET, derivationResourceIdMap.get(guestDatasetId));
                    }
                    if ("hetero_fitTransform.ftl".equals(modelTypeEnum.getFitTransformFtlPath())){
                        taskReq.getFreemarkerMap().put(DataConstant.PYTHON_ARBITER_DATASET, arbiterDataset);
                    }

                }
            }

        } catch (Exception e) {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg("缺失值填充错误:" + e.getMessage());
            log.error("grpc Exception:{}", e.getMessage());
            e.printStackTrace();
        }

        return BaseResultEntity.success();
    }
}
