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
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.util.ZipUtils;
import com.primihub.biz.util.snowflake.SnowflakeId;
import com.primihub.sdk.task.TaskHelper;
import com.primihub.sdk.task.param.TaskMPCParam;
import com.primihub.sdk.task.param.TaskParam;
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
    private OrganConfiguration organConfiguration;
    @Autowired
    private TaskHelper taskHelper;

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
            jointStatisticalMap.remove(taskReq.getFreemarkerMap().get(DataConstant.PYTHON_ARBITER_DATASET));
            Map<String,String> newsetidMap = new HashMap<>();
            log.info("jointStatisticalMap-1:{}", JSONObject.toJSONString(jointStatisticalMap));
            if (newest!=null && newest.size()!=0){
                ids = new ArrayList<>();
                for (ModelDerivationDto modelDerivationDto : newest) {
                    ids.add(modelDerivationDto.getNewResourceId());
                    jointStatisticalMap.put(modelDerivationDto.getNewResourceId(),jointStatisticalMap.get(modelDerivationDto.getOriginalResourceId()));
                    jointStatisticalMap.remove(modelDerivationDto.getOriginalResourceId());
                    newsetidMap.put(modelDerivationDto.getOriginalResourceId(),modelDerivationDto.getNewResourceId());
                }

                log.info("newids:{}", ids);
            }
            log.info("exceptionEntityMap-2:{}",JSONObject.toJSONString(jointStatisticalMap));
            String jointStatistical = taskReq.getValueMap().get("jointStatistical");
            if (jointStatistical!=null){
                log.info(jointStatistical);
                Set<String> paths = new HashSet<>();
                JSONArray objects = JSONArray.parseArray(jointStatistical);
                for (int i = 0; i < objects.size(); i++) {
                    JSONObject jsonObject = objects.getJSONObject(i);
                    Iterator<Map.Entry<String, GrpcComponentDto>> iterator = jointStatisticalMap.entrySet().iterator();
                    List<String> rids = new ArrayList<>();
                    while (iterator.hasNext()){
                        Map.Entry<String, GrpcComponentDto> next = iterator.next();
                        next.getValue().setJointStatisticalType(MAP_TYPE.get(jsonObject.getString("type")));
                        rids.add(next.getKey());
                        if (!newsetidMap.isEmpty()){
                            JSONArray features = jsonObject.getJSONArray("features");
                            for (int i1 = 0; i1 < features.size(); i1++) {
                                JSONObject jsonObject1 = features.getJSONObject(i1);
                                if (newsetidMap.containsKey(jsonObject1.getString("resourceId"))){
                                    jsonObject1.put("resourceId",newsetidMap.get(jsonObject1.getString("resourceId")));
                                }
                            }
                        }
                    }
                    TaskParam<TaskMPCParam> taskParam = new TaskParam<>(new TaskMPCParam());
                    taskParam.setTaskId(taskReq.getDataTask().getTaskIdName());
                    taskParam.setJobId(String.valueOf(taskReq.getJob()));
                    taskParam.getTaskContentParam().setTaskName("mpc_statistics");
                    taskParam.getTaskContentParam().setTaskCode("mpc_statistics");
                    taskParam.getTaskContentParam().setResourceIds(rids);
                    taskParam.getTaskContentParam().setParamMap(new HashMap<>());
                    taskParam.getTaskContentParam().getParamMap().put("ColumnInfo",JSONObject.toJSONString(jointStatisticalMap));
                    taskParam.getTaskContentParam().getParamMap().put("TaskDetail",jsonObject.toJSONString());
                    taskHelper.submit(taskParam);
                    if(!taskParam.getSuccess()){
                        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                        taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"组件处理失败:"+taskParam.getError());
                    }else {
                        taskReq.getDataTask().setTaskState(TaskStateEnum.SUCCESS.getStateType());
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
                        for (Map.Entry<String, String> stringStringEntry : MAP_TYPE.entrySet()) {
                            paths.add(newId+"-"+stringStringEntry.getValue()+".csv");
                        }
                    }
                }
                if (!taskReq.getDataTask().getTaskState().equals(TaskStateEnum.FAIL.getStateType())){
                    ZipUtils.pathFileTOZipRegularFile(path,path+".zip",paths);
                    taskReq.getDataTask().setTaskResultPath(path+".zip");
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
