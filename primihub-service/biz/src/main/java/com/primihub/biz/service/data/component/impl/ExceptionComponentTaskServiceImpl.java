package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
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

    @Override
    public BaseResultEntity check(DataComponentReq req,  ComponentTaskReq taskReq) {
        return componentTypeVerification(req,baseConfiguration.getModelComponents(),taskReq);
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        List<String> ids = taskReq.getFusionResourceList().stream().map(data -> data.get("resourceId").toString()).collect(Collectors.toList());
        log.info("ids:{}",ids);
        Map<String, String> map =new HashMap<>();
        map.put("dataStr",JSONObject.toJSONString(ids));
        log.info(JSONObject.toJSONString(map));
        String freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_EXCEPTION_PAHT, freeMarkerConfigurer, map);
        log.info(freemarkerContent);
        if (freemarkerContent != null) {
            try {
                Map<String, ExceptionEntity> exceptionEntityMap = getExceptionEntityMap(taskReq.getFusionResourceList());
                taskReq.setDerivationList(exceptionEntityMap);
                log.info("exceptionEntityMap:{}",JSONObject.toJSONString(exceptionEntityMap));
                Common.ParamValue columnInfoParamValue = Common.ParamValue.newBuilder().setValueString(JSONObject.toJSONString(exceptionEntityMap)).build();
                Common.Params params = Common.Params.newBuilder()
                        .putParamMap("ColumnInfo", columnInfoParamValue)
                        .build();
                Common.Task task = Common.Task.newBuilder()
                        .setType(Common.TaskType.ACTOR_TASK)
                        .setParams(params)
                        .setName("ExceptionComponentTask")
                        .setLanguage(Common.Language.PYTHON)
                        .setCodeBytes(ByteString.copyFrom(freemarkerContent.getBytes(StandardCharsets.UTF_8)))
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
                }
                // derivation resource datas
                BaseResultEntity derivationResource = dataResourceService.saveDerivationResource(exceptionEntityMap, taskReq.getDataTask().getTaskUserId(), "异常值处理");
                if (!derivationResource.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg("异常值处理组件处理失败:"+derivationResource.getMsg());
                }else {
                    DataModelResource dataModelResource = new DataModelResource(taskReq.getDataModel().getModelId());
                    dataModelResource.setTaskId(taskReq.getDataTask().getTaskId());
                    dataModelResource.setResourceId(derivationResource.getResult().toString());
                    dataModelPrRepository.saveDataModelResource(dataModelResource);
                    taskReq.getDmrList().add(dataModelResource);
                }
            } catch (Exception e) {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg("异常值处理组件:"+e.getMessage());
                log.info("grpc Exception:{}", e.getMessage());
            }
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
