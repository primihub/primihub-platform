package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dto.GrpcComponentDto;
import com.primihub.biz.entity.data.dto.ModelDerivationDto;
import com.primihub.biz.entity.data.po.DataModelResource;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.data.DataTaskMonitorService;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.util.FreemarkerUtil;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("exceptionComponentTaskServiceImpl")
public class ExceptionComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private DataModelPrRepository dataModelPrRepository;
    @Autowired
    private DataTaskMonitorService dataTaskMonitorService;

    @Override
    public BaseResultEntity check(DataComponentReq req,  ComponentTaskReq taskReq) {
        return componentTypeVerification(req,baseConfiguration.getModelComponents(),taskReq);
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        try {
            List<String> ids = taskReq.getFusionResourceList().stream().map(data -> data.get("resourceId").toString()).collect(Collectors.toList());
            List<ModelDerivationDto> newest = taskReq.getNewest();
            log.info("ids:{}", ids);
            Map<String, GrpcComponentDto> exceptionEntityMap = getExceptionEntityMap(taskReq.getFusionResourceList());
            log.info("exceptionEntityMap-1:{}",JSONObject.toJSONString(exceptionEntityMap));
            if (newest!=null && newest.size()!=0){
                ids = new ArrayList<>();
                for (ModelDerivationDto modelDerivationDto : newest) {
                    ids.add(modelDerivationDto.getNewResourceId());
                    exceptionEntityMap.put(modelDerivationDto.getNewResourceId(),exceptionEntityMap.get(modelDerivationDto.getOriginalResourceId()));
                    exceptionEntityMap.remove(modelDerivationDto.getOriginalResourceId());
                }

                log.info("newids:{}", ids);
            }
            String jobId = String.valueOf(taskReq.getJob());
            log.info("exceptionEntityMap-2:{}",JSONObject.toJSONString(exceptionEntityMap));
            String replaceType = taskReq.getValueMap().get("replaceType");
            if (StringUtils.isEmpty(replaceType)){
                replaceType = "MAX";
            }
            Common.ParamValue columnInfoParamValue = Common.ParamValue.newBuilder().setValueString(JSONObject.toJSONString(exceptionEntityMap)).build();
            Common.ParamValue dataFileParamValue = Common.ParamValue.newBuilder().setValueString(ids.stream().collect(Collectors.joining(";"))).build();
            Common.ParamValue replaceTypeParamValue = Common.ParamValue.newBuilder().setValueString(replaceType).build();
            Common.Params params = Common.Params.newBuilder()
                    .putParamMap("ColumnInfo", columnInfoParamValue)
                    .putParamMap("Data_File", dataFileParamValue)
                    .putParamMap("Replace_Type", replaceTypeParamValue)
                    .build();
            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.ACTOR_TASK)
                    .setParams(params)
                    .setName("AbnormalProcessTask")
                    .setLanguage(Common.Language.PROTO)
                    .setCode(ByteString.copyFrom("AbnormalProcessTask".getBytes(StandardCharsets.UTF_8)))
                    .setJobId(ByteString.copyFrom(jobId.getBytes(StandardCharsets.UTF_8)))
                    .setTaskId(ByteString.copyFrom(taskReq.getDataTask().getTaskIdName().getBytes(StandardCharsets.UTF_8)))
                    .addInputDatasets("Data_File")
                    .build();
            log.info("grpc Common.Task :\n{}", task.toString());
            PushTaskRequest request = PushTaskRequest.newBuilder()
                    .setIntendedWorkerId(ByteString.copyFrom("".getBytes(StandardCharsets.UTF_8)))
                    .setTask(task)
                    .setSequenceNumber(11)
                    .setClientProcessedUpTo(22)
                    .setSubmitClientId(ByteString.copyFrom(baseConfiguration.getGrpcClient().getGrpcClientPort().toString().getBytes(StandardCharsets.UTF_8)))
                    .build();
            PushTaskReply reply = workGrpcClient.run(o -> o.submitTask(request));
            log.info("grpc结果:{}", reply.toString());
            if(reply.getRetCode() == 2){
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"组件处理失败");
            }else {
                dataTaskMonitorService.verifyWhetherTheTaskIsSuccessfulAgain(taskReq.getDataTask(), jobId,ids.size(),null);
                List<ModelDerivationDto> derivationList = new ArrayList<>();
                log.info("exceptionEntityMap-3:{}",JSONObject.toJSONString(exceptionEntityMap));
                Iterator<String> keyi = exceptionEntityMap.keySet().iterator();
                while (keyi.hasNext()){
                    String key = keyi.next();
                    GrpcComponentDto value = exceptionEntityMap.get(key);
                    if (value==null)
                        continue;
                    log.info("value:{}",JSONObject.toJSONString(value));
                    derivationList.add(new ModelDerivationDto(key,"missing","异常值处理",value.getNewDataSetId(),null,value.getDataSetId()));
                    log.info("derivationList:{}",JSONObject.toJSONString(derivationList));
                }
                taskReq.getDerivationList().addAll(derivationList);
                taskReq.setNewest(derivationList);
                // derivation resource datas
                log.info(JSONObject.toJSONString(taskReq.getDerivationList()));
                BaseResultEntity derivationResource = dataResourceService.saveDerivationResource(derivationList, taskReq.getDataTask().getTaskUserId(),taskReq.getServerAddress());
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
        } catch (Exception e) {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"组件:"+e.getMessage());
            log.info("grpc Exception:{}", e.getMessage());
            e.printStackTrace();
        }
        return BaseResultEntity.success();
    }

    public Map<String,GrpcComponentDto> getExceptionEntityMap(List<LinkedHashMap<String,Object>> maps){
        Map<String, GrpcComponentDto> map = new HashMap<>();
        for (LinkedHashMap<String, Object> dataMap : maps) {
            List<LinkedHashMap<String, Object>> fieldList = (List<LinkedHashMap<String, Object>>)dataMap.get("fieldList");
            Map<String, Integer> fieldMap = fieldList.stream().collect(Collectors.toMap(d -> d.get("fieldName").toString(), d -> Integer.parseInt(d.get("fieldType").toString())));
            map.put(dataMap.get("resourceId").toString(),new GrpcComponentDto(fieldMap,dataMap.get("resourceId").toString()));
        }
        return map;
    }
}
