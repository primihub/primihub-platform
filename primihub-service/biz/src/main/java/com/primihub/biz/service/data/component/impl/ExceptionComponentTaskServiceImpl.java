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
import com.primihub.biz.service.data.OtherBusinessesService;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.service.feign.FusionResourceService;
import com.primihub.sdk.task.TaskHelper;
import com.primihub.sdk.task.param.TaskMPCParam;
import com.primihub.sdk.task.param.TaskParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 异常值处理
 */
@Slf4j
@Service("exceptionComponentTaskServiceImpl")
public class ExceptionComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
    @Autowired
    private ComponentsConfiguration componentsConfiguration;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private DataModelPrRepository dataModelPrRepository;
    @Autowired
    private TaskHelper taskHelper;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private FusionResourceService fusionResourceService;

    @Override
    public BaseResultEntity check(DataComponentReq req, ComponentTaskReq taskReq) {
        return componentTypeVerification(req, componentsConfiguration.getModelComponents(), taskReq);
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        try {
            List<ModelDerivationDto> newest = taskReq.getNewest();
            List<LinkedHashMap<String, Object>> fusionResourceList = taskReq.getFusionResourceList();
            List<String> ids = null;
            if (CollectionUtils.isNotEmpty(newest)) {
                ids = new ArrayList<>();
                for (ModelDerivationDto modelDerivationDto : newest) {
                    ids.add(modelDerivationDto.getNewResourceId());
                }
                log.info("newIds: {}", ids);
            }

            String path = baseConfiguration.getRunModelFileUrlDirPrefix() + taskReq.getDataTask().getTaskIdName() + File.separator + "exception";
            Map<String, GrpcComponentDto> exceptionEntityMap = getGrpcComponentDataSetMap(fusionResourceList, path, taskReq, ids);
            exceptionEntityMap.remove(taskReq.getFreemarkerMap().get(DataConstant.PYTHON_ARBITER_DATASET));
            log.info("exceptionEntityMap-1: \n{}", JSONObject.toJSONString(exceptionEntityMap));

            String replaceType = taskReq.getValueMap().get("replaceType");
            if (StringUtils.isEmpty(replaceType)) {
                replaceType = "MAX";
            }
            TaskParam<TaskMPCParam> taskParam = new TaskParam<>(new TaskMPCParam());
            taskParam.setTaskId(taskReq.getDataTask().getTaskIdName());
            taskParam.setJobId(String.valueOf(taskReq.getJob()));
            taskParam.getTaskContentParam().setTaskName("AbnormalProcessTask");
            taskParam.getTaskContentParam().setTaskCode("AbnormalProcessTask");
            taskParam.getTaskContentParam().setResourceIds(new ArrayList<>(exceptionEntityMap.keySet()));
            taskParam.getTaskContentParam().setParamMap(new HashMap<>());
            taskParam.getTaskContentParam().getParamMap().put("ColumnInfo", JSONObject.toJSONString(exceptionEntityMap));
            taskParam.getTaskContentParam().getParamMap().put("Replace_Type", replaceType);
            taskHelper.submit(taskParam);
            if (!taskParam.getSuccess()) {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "组件处理失败:" + taskParam.getError());
            } else {
                List<ModelDerivationDto> derivationList = new ArrayList<>();
                log.info("exceptionEntityMap-3: \n{}", JSONObject.toJSONString(exceptionEntityMap));

                for (String key : exceptionEntityMap.keySet()) {
                    GrpcComponentDto value = exceptionEntityMap.get(key);
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
                            ModelDerivationDto.ModelDerivationTypeEnum.EXCEPTION.getCode(),
                            ModelDerivationDto.ModelDerivationTypeEnum.EXCEPTION.getDesc(),
                            value.getNewDataSetId(), value.getOutputFilePath(),
                            originResourceId));
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
                            ModelDerivationDto::getResourceId,
                            ModelDerivationDto::getNewResourceId
                    ));
                    String labelDatasetId = (String) taskReq.getFreemarkerMap().get(DataConstant.PYTHON_LABEL_DATASET);
                    String guestDatasetId = (String) taskReq.getFreemarkerMap().get(DataConstant.PYTHON_GUEST_DATASET);
                    if (derivationResourceIdMap.containsKey(labelDatasetId)) {
                        taskReq.getFreemarkerMap().put(DataConstant.PYTHON_LABEL_DATASET, derivationResourceIdMap.get(labelDatasetId));
                    }
                    if (derivationResourceIdMap.containsKey(guestDatasetId)) {
                        taskReq.getFreemarkerMap().put(DataConstant.PYTHON_GUEST_DATASET, derivationResourceIdMap.get(guestDatasetId));
                    }
                }
            }

        } catch (Exception e) {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "组件:" + e.getMessage());
            log.error("grpc Exception:{}", e.getMessage());
            e.printStackTrace();
        }
        return BaseResultEntity.success();
    }
}
