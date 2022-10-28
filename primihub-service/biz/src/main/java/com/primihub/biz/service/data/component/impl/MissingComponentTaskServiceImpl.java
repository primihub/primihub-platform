package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dto.ModelDerivationDto;
import com.primihub.biz.entity.data.po.DataModelResource;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.util.FreemarkerUtil;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("missingComponentTaskServiceImpl")
public class MissingComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
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

    @Override
    public BaseResultEntity check(DataComponentReq req,  ComponentTaskReq taskReq) {
        return componentTypeVerification(req,baseConfiguration.getModelComponents(),taskReq);
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        try {
            Map<String, ExceptionEntity> exceptionEntityMap = getExceptionEntityMap(taskReq.getFusionResourceList());
            List<String> resourceIds = new ArrayList<>();
            if (taskReq.getNewest()!=null && taskReq.getNewest().size()!=0){
                for (ModelDerivationDto modelDerivationDto : taskReq.getNewest()) {
                    resourceIds.add(modelDerivationDto.getNewResourceId());
                    exceptionEntityMap.put(modelDerivationDto.getNewResourceId(),exceptionEntityMap.get(modelDerivationDto.getResourceId()));
                    exceptionEntityMap.remove(modelDerivationDto.getResourceId());
                }
            }else {
                resourceIds.addAll(exceptionEntityMap.keySet());
            }
            log.info("resourceIds:{}",resourceIds);
            log.info("exceptionEntityMap:{}",JSONObject.toJSONString(exceptionEntityMap));
            Common.ParamValue columnInfoParamValue = Common.ParamValue.newBuilder().setValueString(JSONObject.toJSONString(exceptionEntityMap)).build();
            Common.ParamValue dataFileParamValue = Common.ParamValue.newBuilder().setValueString(resourceIds.stream().collect(Collectors.joining(";"))).build();
            Common.Params params = Common.Params.newBuilder()
                    .putParamMap("ColumnInfo", columnInfoParamValue)
                    .putParamMap("Data_File", dataFileParamValue)
                    .build();
            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.ACTOR_TASK)
                    .setParams(params)
                    .setName("ExceptionComponentTaskMissing")
                    .setLanguage(Common.Language.PROTO)
                    .setCodeBytes(ByteString.copyFrom("".getBytes(StandardCharsets.UTF_8)))
                    .setJobId(ByteString.copyFrom(taskReq.getDataTask().getTaskIdName().getBytes(StandardCharsets.UTF_8)))
                    .setTaskId(ByteString.copyFrom(taskReq.getDataTask().getTaskIdName().getBytes(StandardCharsets.UTF_8)))
                    .build();
            log.info("grpc Common.Task :\n{}", task.toString());
            PushTaskRequest request = PushTaskRequest.newBuilder()
                    .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                    .setTask(task)
                    .setSequenceNumber(11)
                    .setClientProcessedUpTo(22)
                    .build();
            PushTaskReply reply = workGrpcClient.run(o -> o.submitTask(request));
            log.info("grpc结果:{}", reply.toString());
            if(reply.getRetCode() == 2){
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg("异常值处理组件处理失败");
            }else {
                List<ModelDerivationDto> derivationList = new ArrayList<>();
                Iterator<Map.Entry<String, ExceptionEntity>> iterator = exceptionEntityMap.entrySet().iterator();
                while (iterator.hasNext()){
                    Map.Entry<String, ExceptionEntity> next = iterator.next();
                    String key = next.getKey();
                    ExceptionEntity value = next.getValue();
                    derivationList.add(new ModelDerivationDto(key,"missing","缺失值处理",value.getNewDataSetId()));
                }
                // derivation resource datas
                BaseResultEntity derivationResource = dataResourceService.saveDerivationResource(derivationList, taskReq.getDataTask().getTaskUserId());
                log.info(JSONObject.toJSONString(derivationResource));
                if (!derivationResource.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg("异常值处理组件处理失败:"+derivationResource.getMsg());
                }else {
                    List<String> resourceIdLst = (List<String>)derivationResource.getResult();
                    for (String resourceId : resourceIdLst) {
                        DataModelResource dataModelResource = new DataModelResource(taskReq.getDataModel().getModelId());
                        dataModelResource.setTaskId(taskReq.getDataTask().getTaskId());
                        dataModelResource.setResourceId(resourceId);
                        dataModelPrRepository.saveDataModelResource(dataModelResource);
                        taskReq.getDmrList().add(dataModelResource);
                    }
                }
            }
        } catch (Exception e) {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg("异常值处理组件:"+e.getMessage());
            log.info("grpc Exception:{}", e.getMessage());
        }
        return BaseResultEntity.success();
    }

    public Map<String,ExceptionEntity> getExceptionEntityMap(List<LinkedHashMap<String,Object>> maps){
        Map<String,ExceptionEntity> map = new HashMap<>();
        for (LinkedHashMap<String, Object> dataMap : maps) {
            List<LinkedHashMap<String, Object>> fieldList = (List<LinkedHashMap<String, Object>>)dataMap.get("fieldList");
            Map<String, Integer> fieldMap = fieldList.stream().collect(Collectors.toMap(d -> d.get("fieldName").toString(), d -> Integer.parseInt(d.get("fieldType").toString())));
            map.put(dataMap.get("resourceId").toString(),new ExceptionEntity(fieldMap));
        }
        return map;
    }

    @Data
    public class ExceptionEntity{
        public ExceptionEntity(Map<String, Integer> columns) {
            this.columns = columns;
        }
        @JsonIgnore
        private Map<String,Integer> columns;
        private String newDataSetId = UUID.randomUUID().toString();
    }
}