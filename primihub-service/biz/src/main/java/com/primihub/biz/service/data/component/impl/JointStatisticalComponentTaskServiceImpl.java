package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.ComponentsConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dto.GrpcComponentDto;
import com.primihub.biz.entity.data.dto.ModelDerivationDto;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.data.DataTaskMonitorService;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.util.snowflake.SnowflakeId;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("jointStatisticalComponentTaskServiceImpl")
@Slf4j
public class JointStatisticalComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private ComponentsConfiguration componentsConfiguration;
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    private DataTaskMonitorService dataTaskMonitorService;

    @Override
    public BaseResultEntity check(DataComponentReq req, ComponentTaskReq taskReq) {
        return componentTypeVerification(req,componentsConfiguration.getModelComponents(),taskReq);
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        try {
            List<String> ids = taskReq.getFusionResourceList().stream().map(data -> data.get("resourceId").toString()).collect(Collectors.toList());
            List<ModelDerivationDto> newest = taskReq.getNewest();
            log.info("ids:{}", ids);
            Map<String, GrpcComponentDto> jointStatisticalMap = getGrpcComponentDataSetMap(taskReq.getFusionResourceList(),baseConfiguration.getRunModelFileUrlDirPrefix()+taskReq.getDataTask().getTaskIdName());
            log.info("jointStatisticalMap-1:{}", JSONObject.toJSONString(jointStatisticalMap));
            if (newest!=null && newest.size()!=0){
                ids = new ArrayList<>();
                for (ModelDerivationDto modelDerivationDto : newest) {
                    ids.add(modelDerivationDto.getNewResourceId());
                    jointStatisticalMap.put(modelDerivationDto.getNewResourceId(),jointStatisticalMap.get(modelDerivationDto.getOriginalResourceId()));
                    jointStatisticalMap.remove(modelDerivationDto.getOriginalResourceId());
                }

                log.info("newids:{}", ids);
            }
            log.info("exceptionEntityMap-2:{}",JSONObject.toJSONString(jointStatisticalMap));
            String jointStatistical = taskReq.getValueMap().get("jointStatistical");
            if (jointStatistical!=null){
                taskReq.getDataTask().setTaskResultPath(jointStatisticalMap.get(taskReq.getFreemarkerMap().get(DataConstant.PYTHON_LABEL_DATASET)).getOutputFilePath());
                JSONArray objects = JSONArray.parseArray(jointStatistical);
                for (int i = 0; i < objects.size(); i++) {
                    JSONObject jsonObject = objects.getJSONObject(i);
                    Common.ParamValue columnInfoParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(JSONObject.toJSONString(jointStatisticalMap).getBytes(StandardCharsets.UTF_8))).build();
                    Common.ParamValue taskDetailParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8))).build();
                    Common.Params params = Common.Params.newBuilder()
                            .putParamMap("ColumnInfo", columnInfoParamValue)
                            .putParamMap("TaskDetail", taskDetailParamValue)
                            .build();
                    Map<String, Common.Dataset> values = new HashMap<>();
                    for (String id : jointStatisticalMap.keySet()) {
                        values.put("PARTY"+i,Common.Dataset.newBuilder().putData("Data_File",id).build());
                    }
                    Common.TaskContext taskBuild = Common.TaskContext.newBuilder().setJobId(String.valueOf(taskReq.getJob())).setRequestId(String.valueOf(SnowflakeId.getInstance().nextId())).setTaskId(taskReq.getDataTask().getTaskIdName()).build();
                    Common.Task task = Common.Task.newBuilder()
                            .setType(Common.TaskType.ACTOR_TASK)
                            .setParams(params)
                            .setName("mpc_statistics")
                            .setLanguage(Common.Language.PROTO)
                            .setCode(ByteString.copyFrom("mpc_statistics".getBytes(StandardCharsets.UTF_8)))
                            .setTaskInfo(taskBuild)
                            .putAllPartyDatasets(values)
                            .build();
                    PushTaskRequest request = PushTaskRequest.newBuilder()
                            .setIntendedWorkerId(ByteString.copyFrom("".getBytes(StandardCharsets.UTF_8)))
                            .setTask(task)
                            .setSequenceNumber(11)
                            .setClientProcessedUpTo(22)
                            .setSubmitClientId(ByteString.copyFrom(baseConfiguration.getGrpcClient().getGrpcClientPort().toString().getBytes(StandardCharsets.UTF_8)))
                            .build();
                    log.info("grpc PushTaskRequest :\n{}", request.toString());
                    PushTaskReply reply = workGrpcClient.run(o -> o.submitTask(request));
                    log.info("grpc结果:{}", reply.toString());
                    if(reply.getRetCode() == 2){
                        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                        taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"组件处理失败");
                    }else {
                        dataTaskMonitorService.continuouslyObtainTaskStatus(taskReq.getDataTask(),taskBuild,reply.getPartyCount(),null);
//                        List<ModelDerivationDto> derivationList = new ArrayList<>();
//                        log.info("exceptionEntityMap-3:{}",JSONObject.toJSONString(jointStatisticalMap));
//                        Iterator<String> keyi = jointStatisticalMap.keySet().iterator();
//                        while (keyi.hasNext()){
//                            String key = keyi.next();
//                            GrpcComponentDto value = jointStatisticalMap.get(key);
//                            if (value==null) {
//                                continue;
//                            }
//                            log.info("value:{}",JSONObject.toJSONString(value));
//                            derivationList.add(new ModelDerivationDto(key,"jointStatistical","联合统计-"+jsonObject.getString("type"),value.getNewDataSetId(),value.getOutputFilePath(),value.getDataSetId()));
//                            log.info("derivationList:{}",JSONObject.toJSONString(derivationList));
//                        }
//                        taskReq.getDerivationList().addAll(derivationList);
//                        taskReq.setNewest(derivationList);
//                        // derivation resource datas
//                        log.info(JSONObject.toJSONString(taskReq.getDerivationList()));
//                        BaseResultEntity derivationResource = dataResourceService.saveDerivationResource(derivationList, taskReq.getDataTask().getTaskUserId(),taskReq.getServerAddress());
//                        log.info(JSONObject.toJSONString(derivationResource));
//                        if (!derivationResource.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
//                            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
//                            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"组件处理失败:"+derivationResource.getMsg());
//                        }else {
//                            Map<String, ModelDerivationDto> dtoMap = derivationList.stream().collect(Collectors.toMap(ModelDerivationDto::getNewResourceId, Function.identity()));
//                            List<String> resourceIdLst = (List<String>)derivationResource.getResult();
//                            for (String resourceId : resourceIdLst) {
//                                DataModelResource dataModelResource = new DataModelResource(taskReq.getDataModel().getModelId());
//                                dataModelResource.setTaskId(taskReq.getDataTask().getTaskId());
//                                dataModelResource.setResourceId(resourceId);
//                                dataModelResource.setTakePartType(1);
//                                dataModelPrRepository.saveDataModelResource(dataModelResource);
//                                taskReq.getDmrList().add(dataModelResource);
////                                taskReq.getDataTask().setTaskResultPath(dtoMap.get(resourceId).getPath());
//                            }
//                        }
                    }
                }
            }else {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"组件:jointStatistical不可以为null");
            }
        } catch (Exception e) {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"组件:"+e.getMessage());
            log.info("grpc Exception:{}", e.getMessage());
            e.printStackTrace();
        }
        return BaseResultEntity.success();
    }

}
