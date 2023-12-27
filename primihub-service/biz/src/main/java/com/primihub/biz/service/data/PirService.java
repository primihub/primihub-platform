package com.primihub.biz.service.data;


import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.convert.DataTaskConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.base.DataPirKeyQuery;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.po.DataPirTask;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.req.DataPirReq;
import com.primihub.biz.entity.data.req.DataPirTaskReq;
import com.primihub.biz.entity.data.vo.DataPirTaskDetailVo;
import com.primihub.biz.entity.data.vo.DataPirTaskVo;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PirService {
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;
    @Autowired
    private DataTaskRepository dataTaskRepository;
    @Autowired
    private DataAsyncService dataAsyncService;

    public String getResultFilePath(String taskId,String taskDate){
        return new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(taskDate).append("/").append(taskId).append(".csv").toString();
    }
    public BaseResultEntity pirSubmitTask(DataPirReq req, String pirParam) {
        BaseResultEntity dataResource = otherBusinessesService.getDataResource(req.getResourceId());
        if (dataResource.getCode()!=0) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"资源查询失败");
        }
        Map<String, Object> pirDataResource = (LinkedHashMap)dataResource.getResult();
        int available = Integer.parseInt(pirDataResource.getOrDefault("available","1").toString());
        if (available == 1) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"资源不可用");
        }
        // dataResource columnName list
        String resourceColumnNames = pirDataResource.getOrDefault("resourceColumnNameList", "").toString();
        if (StringUtils.isBlank(resourceColumnNames)){
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"获取资源字段列表失败");
        }
        String[] resourceColumnNameArray = resourceColumnNames.split(",");
        if (resourceColumnNameArray.length == 0) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"获取资源字段列表为空");
        }

        String[] queryColumnNames = {
                resourceColumnNameArray[0]
        };
        // convert pirparam to query array
        List<DataPirKeyQuery> dataPirKeyQueries = convertPirParamToQueryArray(pirParam,queryColumnNames);

        DataTask dataTask = new DataTask();
//        dataTask.setTaskIdName(UUID.randomUUID().toString());
        dataTask.setTaskIdName(Long.toString(SnowflakeId.getInstance().nextId()));
        dataTask.setTaskName(req.getTaskName());
        dataTask.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        dataTask.setTaskType(TaskTypeEnum.PIR.getTaskType());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTaskPrRepository.saveDataTask(dataTask);
        DataPirTask dataPirTask = new DataPirTask();
        dataPirTask.setTaskId(dataTask.getTaskId());
        // retrievalId will rent in web ,need to be readable
        dataPirTask.setRetrievalId(pirParam);
        dataPirTask.setProviderOrganName(pirDataResource.get("organName").toString());
        dataPirTask.setResourceName(pirDataResource.get("resourceName").toString());
        dataPirTask.setResourceId(req.getResourceId());
        dataTaskPrRepository.saveDataPirTask(dataPirTask);
        dataAsyncService.pirGrpcTask(dataTask,dataPirTask,resourceColumnNames,dataPirKeyQueries);
        Map<String, Object> map = new HashMap<>();
        map.put("taskId",dataTask.getTaskId());
        return BaseResultEntity.success(map);
    }

    private static List<DataPirKeyQuery> convertPirParamToQueryArray(String pirParam, String[] resourceColumnNameArray) {
        DataPirKeyQuery dataPirKeyQuery = new DataPirKeyQuery();
        dataPirKeyQuery.setKey(resourceColumnNameArray);
        String[] array = {
                pirParam
        };
        List<String[]> queries = new ArrayList<>(resourceColumnNameArray.length);
        for (int i = 0; i < resourceColumnNameArray.length; i++) {
            queries.add(i, array);
        }
        dataPirKeyQuery.setQuery(queries);
        return Collections.singletonList(dataPirKeyQuery);
    }

    public BaseResultEntity getPirTaskList(DataPirTaskReq req) {
        List<DataPirTaskVo> dataPirTaskVos = dataTaskRepository.selectDataPirTaskPage(req);
        if (dataPirTaskVos.isEmpty()) {
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Integer tolal = dataTaskRepository.selectDataPirTaskCount(req);
        Map<String,LinkedHashMap<String, Object>> resourceMap= new HashMap<>();
        List<String> ids = dataPirTaskVos.stream().map(DataPirTaskVo::getResourceId).collect(Collectors.toList());
        BaseResultEntity baseResult = otherBusinessesService.getResourceListById(ids);
        if (baseResult.getCode()==0){
            List<LinkedHashMap<String,Object>> voList = (List<LinkedHashMap<String,Object>>)baseResult.getResult();
            if (voList != null && voList.size()!=0){
                resourceMap.putAll(voList.stream().collect(Collectors.toMap(data -> data.get("resourceId").toString(), Function.identity())));
            }
        }
        for (DataPirTaskVo dataPirTaskVo : dataPirTaskVos) {
            if (resourceMap.containsKey(dataPirTaskVo.getResourceId())){
                DataTaskConvert.dataPirTaskPoConvertDataPirTaskVo(dataPirTaskVo,resourceMap.get(dataPirTaskVo.getResourceId()));
            }
        }
        return BaseResultEntity.success(new PageDataEntity(tolal,req.getPageSize(),req.getPageNo(),dataPirTaskVos));
    }

    public BaseResultEntity getPirTaskDetail(Long taskId) {
        DataPirTask task = dataTaskRepository.selectPirTaskById(taskId);
        if (task==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到任务信息");
        }
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (dataTask==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到任务详情");
        }
        DataPirTaskDetailVo vo = new DataPirTaskDetailVo();
        List<LinkedHashMap<String, Object>> list = null;
        if (StringUtils.isNotEmpty(dataTask.getTaskResultPath())){
            vo.setList(FileUtil.getCsvData(dataTask.getTaskResultPath(), 50));
        }
        vo.setTaskName(dataTask.getTaskName());
        vo.setTaskIdName(dataTask.getTaskIdName());
        vo.setTaskState(dataTask.getTaskState());
        vo.setOrganName(task.getProviderOrganName());
        vo.setResourceName(task.getResourceName());
        vo.setResourceId(task.getResourceId());
        vo.setRetrievalId(task.getRetrievalId());
        vo.setTaskError(dataTask.getTaskErrorMsg());
        vo.setCreateDate(dataTask.getCreateDate());
        vo.setTaskStartTime(dataTask.getTaskStartTime());
        vo.setTaskEndTime(dataTask.getTaskEndTime());
        return BaseResultEntity.success(vo);
    }


}
