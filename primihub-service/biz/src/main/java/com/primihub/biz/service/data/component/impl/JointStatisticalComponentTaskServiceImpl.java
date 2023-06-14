package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.ComponentsConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
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
import com.primihub.biz.util.ZipUtils;
import com.primihub.biz.util.snowflake.SnowflakeId;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import primihub.rpc.Common;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service("jointStatisticalComponentTaskServiceImpl")
@Slf4j
public class JointStatisticalComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
    private static final Map<String,String> MAP_TYPE = new HashMap(){{
        put("1","average");
        put("2","sum");
        put("3","max");
        put("4","min");
    }};
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private ComponentsConfiguration componentsConfiguration;
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    private DataTaskMonitorService dataTaskMonitorService;
    @Autowired
    private OrganConfiguration organConfiguration;

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
            String path = baseConfiguration.getRunModelFileUrlDirPrefix()+taskReq.getDataTask().getTaskIdName() + File.separator + "mpc";
            Map<String, GrpcComponentDto> jointStatisticalMap = getGrpcComponentDataSetMap(taskReq.getFusionResourceList(),path);
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
                log.info(jointStatistical);
                JSONArray objects = JSONArray.parseArray(jointStatistical);
                for (int i = 0; i < objects.size(); i++) {
                    JSONObject jsonObject = objects.getJSONObject(i);
                    Iterator<Map.Entry<String, GrpcComponentDto>> iterator = jointStatisticalMap.entrySet().iterator();
                    Map<String, Common.Dataset> values = new HashMap<>();
                    int ci = 0;
                    while (iterator.hasNext()){
                        Map.Entry<String, GrpcComponentDto> next = iterator.next();
                        next.getValue().setJointStatisticalType(MAP_TYPE.get(jsonObject.getString("type")));
                        values.put("PARTY"+ci,Common.Dataset.newBuilder().putData("Data_File",next.getKey()).build());
                        ci++;
                    }
                    Common.ParamValue columnInfoParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(JSONObject.toJSONString(jointStatisticalMap).getBytes(StandardCharsets.UTF_8))).build();
                    Common.ParamValue taskDetailParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(jsonObject.toJSONString().getBytes(StandardCharsets.UTF_8))).build();
                    Common.Params params = Common.Params.newBuilder()
                            .putParamMap("ColumnInfo", columnInfoParamValue)
                            .putParamMap("TaskDetail", taskDetailParamValue)
                            .build();
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
                    }
                    String newId = null;
                    String localOrganShortCode = organConfiguration.getLocalOrganShortCode();
                    for (String resourceId : jointStatisticalMap.keySet()) {
                        if (resourceId.contains(localOrganShortCode)){
                            newId = jointStatisticalMap.get(resourceId).getNewDataSetId();
                        }
                    }
                    if (newId==null){
                        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                        taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"组件:未匹配到数据集ID无法打包zip");
                    }else {
                        Set<String> paths = new HashSet<>();
                        for (Map.Entry<String, String> stringStringEntry : MAP_TYPE.entrySet()) {
                            paths.add(newId+"-"+stringStringEntry.getValue()+".csv");
                        }
                        ZipUtils.pathFileTOZipRegularFile(path,path+".zip",paths);
                        taskReq.getDataTask().setTaskResultPath(path+".zip");
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
