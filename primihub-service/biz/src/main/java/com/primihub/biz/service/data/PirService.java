package com.primihub.biz.service.data;


import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.CommonConstant;
import com.primihub.biz.convert.DataTaskConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.po.DataPirTask;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.req.DataPirTaskReq;
import com.primihub.biz.entity.data.req.DataPirTaskSyncReq;
import com.primihub.biz.entity.data.req.DataPsiTaskSyncReq;
import com.primihub.biz.entity.data.vo.DataPirTaskVo;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import com.primihub.biz.util.snowflake.SnowflakeId;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
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
    private DataProjectService dataProjectService;
    @Autowired
    private DataTaskRepository dataTaskRepository;
    @Autowired
    private DataAsyncService dataAsyncService;
    @Resource(name="soaRestTemplate")
    private RestTemplate restTemplate;

    public String getResultFilePath(String taskId,String taskDate){
        return new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(taskDate).append("/").append(taskId).append(".csv").toString();
    }
    public BaseResultEntity pirSubmitTask(String serverAddress,String resourceId, String pirParam,String organId) {
        BaseResultEntity dataResource = otherBusinessesService.getDataResource(serverAddress, resourceId);
        if (dataResource.getCode()!=0) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"资源查询失败");
        }
        Map<String, Object> pirDataResource = (LinkedHashMap)dataResource.getResult();
        int available = Integer.parseInt(pirDataResource.getOrDefault("available","1").toString());
        if (available == 1) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"资源不可用");
        }
        DataTask dataTask = new DataTask();
        dataTask.setTaskIdName(Long.toString(SnowflakeId.getInstance().nextId()));
        dataTask.setTaskName(pirDataResource.get("resourceName").toString());
        dataTask.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        dataTask.setTaskType(TaskTypeEnum.PIR.getTaskType());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTaskPrRepository.saveDataTask(dataTask);
        DataPirTask dataPirTask = new DataPirTask();
        dataPirTask.setTaskId(dataTask.getTaskId());
        dataPirTask.setServerAddress(serverAddress);
        dataPirTask.setRetrievalId(pirParam);
        dataPirTask.setProviderOrganName(pirDataResource.get("organName").toString());
        dataPirTask.setResourceName(pirDataResource.get("resourceName").toString());
        dataPirTask.setResourceId(resourceId);
        dataTaskPrRepository.saveDataPirTask(dataPirTask);
        Map<String, Map> organListMap = dataProjectService.getOrganListMap(new ArrayList() {{
            add(organId);
        }}, serverAddress);
        if (organListMap.containsKey(organId)){
            try {
                Map map = organListMap.get(organId);
                String gatewayAddress = map.get("gatewayAddress").toString();
                String publicKey = (String) map.get("publicKey");
                otherBusinessesService.syncGatewayApiData(new DataPirTaskSyncReq(dataTask,dataPirTask),CommonConstant.DISPATCH_RUN_PIR.replace("<address>", gatewayAddress),publicKey);
            }catch (Exception e){
                log.info("Dispatch gatewayAddress api Exception:{}",e.getMessage());
            }
            log.info("出去");
        }else {
            log.info("下发失败");
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"任务下发失败,中心节点未获取到机构信息");
        }
//        dataAsyncService.pirGrpcTask(dataTask,resourceId,pirParam);
        Map<String, Object> map = new HashMap<>();
        map.put("taskId",dataTask.getTaskId());
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getPirTaskList(DataPirTaskReq req) {
        List<DataPirTaskVo> dataPirTaskVos = dataTaskRepository.selectDataPirTaskPage(req);
        if (dataPirTaskVos.isEmpty()) {
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Integer tolal = dataTaskRepository.selectDataPirTaskCount(req);
        Map<String,LinkedHashMap<String, Object>> resourceMap= new HashMap<>();
        Map<String, List<DataPirTaskVo>> resourceMapList = dataPirTaskVos.stream().collect(Collectors.groupingBy(DataPirTaskVo::getServerAddress));
        Iterator<Map.Entry<String, List<DataPirTaskVo>>> it = resourceMapList.entrySet().iterator();
        while (it.hasNext()){
            Map.Entry<String, List<DataPirTaskVo>> next = it.next();
            String[] ids = next.getValue().stream().map(DataPirTaskVo::getResourceId).toArray(String[]::new);
            BaseResultEntity baseResult = otherBusinessesService.getResourceListById(next.getKey(), ids);
            if (baseResult.getCode()==0){
                List<LinkedHashMap<String,Object>> voList = (List<LinkedHashMap<String,Object>>)baseResult.getResult();
                if (voList != null && voList.size()!=0){
                    resourceMap.putAll(voList.stream().collect(Collectors.toMap(data -> data.get("resourceId").toString(), Function.identity())));
                }
            }
        }
        for (DataPirTaskVo dataPirTaskVo : dataPirTaskVos) {
            if (resourceMap.containsKey(dataPirTaskVo.getResourceId())){
                DataTaskConvert.dataPirTaskPoConvertDataPirTaskVo(dataPirTaskVo,resourceMap.get(dataPirTaskVo.getResourceId()));
            }
        }
        return BaseResultEntity.success(new PageDataEntity(tolal,req.getPageSize(),req.getPageNo(),dataPirTaskVos));
    }
}
