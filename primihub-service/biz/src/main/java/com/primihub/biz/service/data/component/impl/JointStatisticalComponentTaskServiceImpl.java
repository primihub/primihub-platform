package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
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
import com.primihub.sdk.task.TaskHelper;
import com.primihub.sdk.task.param.TaskMPCParam;
import com.primihub.sdk.task.param.TaskParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;

/**
 * 联合统计
 */
@Service("jointStatisticalComponentTaskServiceImpl")
@Slf4j
public class JointStatisticalComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
    private static final Map<String, String> MAP_TYPE = new HashMap<String, String>() {{
        put("1", "average");
        put("2", "sum");
        put("3", "max");
        put("4", "min");
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
        return componentTypeVerification(req, componentsConfiguration.getModelComponents(), taskReq);
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        try {
            List<ModelDerivationDto> newest = taskReq.getNewest();
            List<String> ids = null;

            // 最新衍生数据的 原始数据Id: 最新数据Id
            Map<String,String> newSetIdMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(newest)) {
                ids = new ArrayList<>();
                for (ModelDerivationDto modelDerivationDto : newest) {
                    ids.add(modelDerivationDto.getNewResourceId());
                    newSetIdMap.put(modelDerivationDto.getOriginalResourceId(), modelDerivationDto.getNewResourceId());
                }
                log.info("newIds: {}", ids);
            }

            String path = baseConfiguration.getRunModelFileUrlDirPrefix() + taskReq.getDataTask().getTaskIdName() + File.separator + "mpc";
            // resourceId 其对应的旧资源的 Field信息
            Map<String, GrpcComponentDto> jointStatisticalMap = getGrpcComponentDataSetMap(taskReq.getFusionResourceList(), path, taskReq, ids);
            jointStatisticalMap.remove(taskReq.getFreemarkerMap().get(DataConstant.PYTHON_ARBITER_DATASET));
            log.info("jointStatisticalMap-1: \n{}", JSONObject.toJSONString(jointStatisticalMap));

            String jointStatistical = taskReq.getValueMap().get("jointStatistical");
            if (jointStatistical != null) {
                log.info("[模型任务][联合统计] 模型任务提交参数 jointStatistical [jointStatistical: {}]", jointStatistical);
                Set<String> paths = new HashSet<>();
                JSONArray objects = JSONArray.parseArray(jointStatistical);
                for (int i = 0; i < objects.size(); i++) {
                    JSONObject jsonObject = objects.getJSONObject(i);
                    Iterator<Map.Entry<String, GrpcComponentDto>> iterator = jointStatisticalMap.entrySet().iterator();
                    List<String> rids = new ArrayList<>();
                    while (iterator.hasNext()) {
                        Map.Entry<String, GrpcComponentDto> next = iterator.next();
                        next.getValue().setJointStatisticalType(MAP_TYPE.get(jsonObject.getString("type")));
                        rids.add(next.getKey());
                        if (CollectionUtils.isNotEmpty(newest)) {
                            JSONArray features = jsonObject.getJSONArray("features");
                            for (int i1 = 0; i1 < features.size(); i1++) {
                                JSONObject jsonObject1 = features.getJSONObject(i1);
                                if (newSetIdMap.containsKey(jsonObject1.getString("resourceId"))) {
                                    // 如何把旧的资源id替换成新的资源id
                                    jsonObject1.put("resourceId", newSetIdMap.get(jsonObject1.getString("resourceId")));
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
                    taskParam.getTaskContentParam().getParamMap().put("ColumnInfo", JSONObject.toJSONString(jointStatisticalMap));
                    taskParam.getTaskContentParam().getParamMap().put("TaskDetail", jsonObject.toJSONString());
                    taskHelper.submit(taskParam);
                    if (!taskParam.getSuccess()) {
                        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                        taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "组件处理失败:" + taskParam.getError());
                    } else {
                        taskReq.getDataTask().setTaskState(TaskStateEnum.SUCCESS.getStateType());
                    }
                    String newId = null;
                    String localOrganShortCode = organConfiguration.getLocalOrganShortCode();
                    for (String resourceId : jointStatisticalMap.keySet()) {
                        if (resourceId.contains(localOrganShortCode)) {
                            newId = jointStatisticalMap.get(resourceId).getNewDataSetId();
                        }
                    }
                    if (newId == null) {
                        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                        taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "组件:未匹配到数据集ID无法打包zip");
                    } else {
                        for (Map.Entry<String, String> stringStringEntry : MAP_TYPE.entrySet()) {
                            paths.add(newId + "-" + stringStringEntry.getValue() + ".csv");
                        }
                    }
                }
                if (!taskReq.getDataTask().getTaskState().equals(TaskStateEnum.FAIL.getStateType())) {
                    ZipUtils.pathFileTOZipRegularFile(path, path + ".zip", paths);
                    taskReq.getDataTask().setTaskResultPath(path + ".zip");
                }
            } else {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "组件:jointStatistical不可以为null");
            }
        } catch (Exception e) {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "组件:" + e.getMessage());
            log.info("grpc Exception:{}", e.getMessage());
            e.printStackTrace();
        }
        return BaseResultEntity.success();
    }

}
