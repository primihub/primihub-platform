package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.convert.DataReasoningConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DataReasoningReq;
import com.primihub.biz.entity.data.req.DataReasoningResourceReq;
import com.primihub.biz.entity.data.req.DataReasoningTaskSyncReq;
import com.primihub.biz.entity.data.req.ReasoningListReq;
import com.primihub.biz.repository.primarydb.data.DataReasoningPrRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataModelRepository;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.repository.secondarydb.data.DataReasoningRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import com.primihub.biz.util.snowflake.SnowflakeId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataReasoningService {
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private DataReasoningPrRepository dataReasoningPrRepository;
    @Autowired
    private DataReasoningRepository dataReasoningRepository;
    @Autowired
    private DataModelRepository dataModelRepository;
    @Autowired
    private DataProjectRepository dataProjectRepository;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;
    @Autowired
    private DataTaskRepository dataTaskRepository;
    @Autowired
    private DataAsyncService dataAsyncService;



    public BaseResultEntity getReasoningList(ReasoningListReq req) {
        if (baseConfiguration.getAdminUserIds().contains(req.getUserId()))
            req.setIsAdmin(1);
        List<DataReasoning> dataReasonings = dataReasoningRepository.selectDataReasoninPage(req);
        if (dataReasonings.size()==0){
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Integer tolal = dataReasoningRepository.selectDataReasoninCount(req);
        return BaseResultEntity.success(new PageDataEntity(tolal,req.getPageSize(),req.getPageNo(),dataReasonings.stream().map(DataReasoningConvert::dataReasoningConvertVo).collect(Collectors.toList())));
    }

    public BaseResultEntity saveReasoning(DataReasoningReq req) {
        DataModelTask modelTask = dataModelRepository.queryModelTaskById(req.getTaskId());
        if (modelTask == null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"没有查询到模型信息");
        DataReasoning dataReasoning = DataReasoningConvert.dataReasoningReqConvertPo(req);
        Set<String> resourceIds = req.getResourceList().stream().map(DataReasoningResourceReq::getResourceId).collect(Collectors.toSet());
        List<DataProjectResource> dataProjectResources = dataProjectRepository.selectProjectResourceByResourceIds(resourceIds);
        if (dataProjectResources.isEmpty()){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"没有查询到资源信息");
        }
        DataTask dataTask1 = dataTaskRepository.selectDataTaskByTaskId(dataReasoning.getTaskId());
        dataReasoning.setTaskId(Long.valueOf(dataTask1.getTaskIdName()));
        dataReasoningPrRepository.saveDataReasoning(dataReasoning);
        Map<String, String> resourceMap = dataProjectResources.stream().collect(Collectors.toMap(DataProjectResource::getResourceId, DataProjectResource::getServerAddress,(key1, key2) -> key2));
        List<DataReasoningResource> dataReasoningResourceList = req.getResourceList().stream().map(r -> DataReasoningConvert.dataReasoningResourceReqConvertPo(r, dataReasoning.getId(), resourceMap.get(r.getResourceId()))).collect(Collectors.toList());
        dataReasoningPrRepository.saveDataReasoningResources(dataReasoningResourceList);
        DataTask dataTask = new DataTask();
        dataTask.setTaskIdName(Long.toString(SnowflakeId.getInstance().nextId()));
        dataTask.setTaskName(dataReasoning.getReasoningName());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTask.setTaskType(TaskTypeEnum.REASONING.getTaskType());
        dataTask.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        dataTask.setTaskUserId(dataReasoning.getUserId());
        dataTaskPrRepository.saveDataTask(dataTask);
        dataAsyncService.runReasoning(dataReasoning,dataReasoningResourceList,modelTask,dataTask);
        Map<String,Object> map = new HashMap<>();
        map.put("id",dataReasoning.getId());
        map.put("reasoningId",dataReasoning.getReasoningId());
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getReasoning(Long id) {
        DataReasoning dataReasoning = dataReasoningRepository.selectDataReasoninById(id);
        if (dataReasoning==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"没有查询到数据");
        return BaseResultEntity.success(DataReasoningConvert.dataReasoningConvertVo(dataReasoning));
    }

    public BaseResultEntity dispatchRunReasoning(DataReasoningTaskSyncReq taskReq) {
        dataReasoningPrRepository.saveDataReasoning(taskReq.getDataReasoning());
        for (DataReasoningResource dataReasoningResource : taskReq.getDataReasoningResourceList()) {
            dataReasoningResource.setReasoningId(taskReq.getDataReasoning().getId());
        }
        dataReasoningPrRepository.saveDataReasoningResources(taskReq.getDataReasoningResourceList());
        taskReq.getDataTask().setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        dataTaskPrRepository.saveDataTask(taskReq.getDataTask());
        DataReasoning dataReasoning = dataReasoningRepository.selectDataReasoninById(taskReq.getDataReasoning().getId());
        log.info("{} - json:{}",dataReasoning,JSONObject.toJSONString(dataReasoning));
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskIdName(taskReq.getDataTask().getTaskIdName());
        log.info("{} - json:{}",dataTask,JSONObject.toJSONString(dataTask));
        dataAsyncService.runReasoning(dataReasoning,taskReq.getDataReasoningResourceList(),taskReq.getModelTask(), dataTask);
        return BaseResultEntity.success();
    }
}
