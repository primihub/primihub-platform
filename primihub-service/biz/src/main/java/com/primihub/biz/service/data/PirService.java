package com.primihub.biz.service.data;


import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class PirService {
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;
    @Autowired
    private DataAsyncService dataAsyncService;

    public String getResultFilePath(String taskId,String taskDate){
        return new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(taskDate).append("/").append(taskId).append(".csv").toString();
    }

    public BaseResultEntity pirSubmitTask(String serverAddress,String resourceId, String pirParam) {
        BaseResultEntity dataResource = fusionResourceService.getDataResource(serverAddress, resourceId);
        if (dataResource.getCode()!=0)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"资源查询失败");
        Map<String, Object> pirDataResource = (LinkedHashMap)dataResource.getResult();
        Object resourceRowsCountObj = pirDataResource.get("resourceRowsCount");
        if (resourceRowsCountObj==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"资源行数获取错误");
        Integer resourceRowsCount = (Integer) resourceRowsCountObj - 1;
        DataTask dataTask = new DataTask();
        dataTask.setTaskIdName(UUID.randomUUID().toString());
        dataTask.setTaskName(pirDataResource.get("resourceName").toString());
        dataTask.setTaskState(TaskStateEnum.INIT.getStateType());
        dataTask.setTaskType(TaskTypeEnum.PIR.getTaskType());
        dataTaskPrRepository.saveDataTask(dataTask);
        dataAsyncService.pirGrpcTask(dataTask,resourceId,pirParam,resourceRowsCount);
        Map<String, Object> map = new HashMap<>();
        map.put("taskId",dataTask.getTaskId());
        return BaseResultEntity.success(map);
    }
}
