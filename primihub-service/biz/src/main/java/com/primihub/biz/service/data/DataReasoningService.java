package com.primihub.biz.service.data;

import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.CommonConstant;
import com.primihub.biz.convert.DataReasoningConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.repository.primarydb.data.DataReasoningPrRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataModelRepository;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.repository.secondarydb.data.DataReasoningRepository;
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
    @Autowired
    private DataProjectService dataProjectService;
    @Resource(name="soaRestTemplate")
    private RestTemplate restTemplate;



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
//        dataAsyncService.runReasoning(dataReasoning,dataReasoningResourceList,modelTask,dataTask);
        DataReasoningResource dataReasoningResource = dataReasoningResourceList.stream().filter(r -> r.getParticipationIdentity().toString().equals("1")).findFirst().get();
        Map<String,Map> resourceListMap = dataProjectService.getResourceListMap(new ArrayList() {{
            add(dataReasoningResource.getResourceId());
        }}, dataProjectResources.get(0).getServerAddress());
        String organId = resourceListMap.get(dataReasoningResource.getResourceId()).get("organId").toString();
        Map<String, Map> organListMap = dataProjectService.getOrganListMap(new ArrayList() {{
            add(organId);
        }}, dataProjectResources.get(0).getServerAddress());
        if (organListMap.containsKey(organId)){
            Map map = organListMap.get(organId);
            String gatewayAddress = map.get("gatewayAddress").toString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(new DataReasoningTaskSyncReq(dataReasoning,dataReasoningResourceList,modelTask,dataTask), headers);
            log.info(CommonConstant.DISPATCH_RUN_REASONING.replace("<address>", gatewayAddress.toString()));
            try {
                BaseResultEntity baseResultEntity = restTemplate.postForObject(CommonConstant.DISPATCH_RUN_REASONING.replace("<address>", gatewayAddress.toString()), request, BaseResultEntity.class);
                log.info("baseResultEntity code:{} msg:{}",baseResultEntity.getCode(),baseResultEntity.getMsg());
            }catch (Exception e){
                log.info("Dispatch gatewayAddress api Exception:{}",e.getMessage());
            }
            log.info("出去");
        }else {
            log.info("下发失败");
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"任务下发失败,中心节点未获取到机构信息");
        }
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

    public BaseResultEntity syncReasoning(DataReasoningTaskSyncReq req) {
        DataReasoning dataReasoning = dataReasoningRepository.selectDataReasoninByIdS(req.getDataReasoning().getReasoningId());
        if (dataReasoning!=null){
            req.getDataReasoning().setId(dataReasoning.getId());
            dataReasoningPrRepository.updateDataReasoning(req.getDataReasoning());
        }else {
            req.getDataReasoning().setId(null);
            dataReasoningPrRepository.saveDataReasoning(req.getDataReasoning());
            List<DataReasoningResource> dataReasoningResourceList = req.getDataReasoningResourceList();
            for (DataReasoningResource dataReasoningResource : dataReasoningResourceList) {
                dataReasoningResource.setReasoningId(req.getDataReasoning().getId());
            }
            dataReasoningPrRepository.saveDataReasoningResources(dataReasoningResourceList);
        }
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskIdName(req.getDataTask().getTaskIdName());
        if (dataTask!=null){
            req.getDataTask().setTaskId(dataTask.getTaskId());
            dataTaskPrRepository.updateDataTask(req.getDataTask());
        }else {
            req.getDataTask().setTaskId(null);
            dataTaskPrRepository.saveDataTask(dataTask);
        }
        return BaseResultEntity.success();
    }
}
