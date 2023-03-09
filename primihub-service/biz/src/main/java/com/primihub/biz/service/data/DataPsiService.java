package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.convert.DataPsiConvert;
import com.primihub.biz.convert.DataResourceConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.entity.data.vo.DataOrganPsiTaskVo;
import com.primihub.biz.entity.data.vo.DataPsiTaskVo;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.repository.primarydb.data.DataPsiPrRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataPsiRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataPsiService {

    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private DataPsiRepository dataPsiRepository;
    @Autowired
    private DataPsiPrRepository dataPsiPrRepository;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private DataAsyncService dataAsyncService;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;
    @Autowired
    private DataTaskRepository dataTaskRepository;
    @Autowired
    private OrganConfiguration organConfiguration;


    public BaseResultEntity getPsiResourceList(DataResourceReq req, Long organId) {
        return dataResourceService.getDataResourceList(req,null);
    }

    public BaseResultEntity getPsiResourceAllocationList(PageReq req, String organId, String serverAddress,String resourceName) {
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        String localOrganId = organConfiguration.getSysLocalOrganId();
        if(StringUtils.isBlank(organId) || sysLocalOrganInfo==null || StringUtils.isBlank(sysLocalOrganInfo.getOrganId()) || sysLocalOrganInfo.getOrganId().equals(organId)){
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("organId",organId);
            paramMap.put("offset",req.getOffset());
            paramMap.put("pageSize",req.getPageSize());
            paramMap.put("resourceName",resourceName);
            paramMap.put("resourceState",0);
            List<DataResource> dataResources = dataResourceRepository.queryDataResource(paramMap);
            if (dataResources.size()==0){
                return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
            }
            Integer count = dataResourceRepository.queryDataResourceCount(paramMap);
            Set<Long> resourceIds = dataResources.stream().map(DataResource::getResourceId).collect(Collectors.toSet());
            List<DataFileField> dataFileField = dataResourceRepository.queryDataFileField(new HashMap() {{
                put("resourceIds", resourceIds);
//                put("relevance", 1);
            }});
            Map<Long, List<DataFileField>> fileFieldMap = dataFileField.stream().collect(Collectors.groupingBy(DataFileField::getResourceId));
            return BaseResultEntity.success(new PageDataEntity(count.intValue(),req.getPageSize(),req.getPageNo(),dataResources.stream().map(re-> DataResourceConvert.DataResourcePoConvertAllocationVo(re,fileFieldMap.get(re.getResourceId()),localOrganId)).collect(Collectors.toList())));
        }else if (!sysLocalOrganInfo.getOrganId().equals(organId)){
            if (StringUtils.isBlank(serverAddress))
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
            DataFResourceReq fResourceReq = new DataFResourceReq();
            fResourceReq.setPageNo(req.getPageNo());
            fResourceReq.setPageSize(req.getPageSize());
            fResourceReq.setServerAddress(serverAddress);
            fResourceReq.setOrganId(organId);
            fResourceReq.setResourceName(resourceName);
            BaseResultEntity baseResult = fusionResourceService.getResourceList(fResourceReq);
            if (baseResult.getCode()!=0)
                return baseResult;
            Map<String,Object> pageData = (LinkedHashMap<String,Object>)baseResult.getResult();
            List<LinkedHashMap<String,Object>> voList = (List<LinkedHashMap<String,Object>>)pageData.getOrDefault("data",new ArrayList<>());
            return BaseResultEntity.success(new PageDataEntity(Integer.valueOf(pageData.getOrDefault("total","0").toString()),Integer.valueOf(pageData.getOrDefault("pageSize","0").toString()),Integer.valueOf(pageData.getOrDefault("index","0").toString()),voList.stream().map(fr->DataResourceConvert.fusionResourceConvertAllocationVo(fr,localOrganId)).collect(Collectors.toList())));
        }else {
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
    }

    public BaseResultEntity saveDataPsi(DataPsiReq req, Long userId) {
        if (StringUtils.isBlank(req.getServerAddress())){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        }
        DataPsi dataPsi = DataPsiConvert.DataPsiReqConvertPo(req);
        dataPsi.setUserId(userId);
        dataPsiPrRepository.saveDataPsi(dataPsi);
        DataPsiTask task = new DataPsiTask();
        task.setPsiId(dataPsi.getId());
//        task.setTaskId(UUID.randomUUID().toString());
        task.setTaskId(Long.toString(SnowflakeId.getInstance().nextId()));
        task.setTaskState(0);
        if (dataPsi.getResultOrganIds().indexOf(",")!=-1){
            task.setAscription("双方获取");
            task.setAscriptionType(1);
        }else {
            task.setAscription("一方获取");
            task.setAscriptionType(0);
        }
        if (dataPsi.getOutputContent()==0){
            task.setAscription(task.getAscription()+"交集");
        }else {
            task.setAscription(task.getAscription()+"差集");
        }
        task.setCreateDate(new Date());
        dataPsiPrRepository.saveDataPsiTask(task);
        DataTask dataTask = new DataTask();
        dataTask.setTaskIdName(task.getTaskId());
        dataTask.setTaskName(dataPsi.getResultName());
        dataTask.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        dataTask.setTaskType(TaskTypeEnum.PSI.getTaskType());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTaskPrRepository.saveDataTask(dataTask);
        dataAsyncService.psiGrpcRun(task,dataPsi,dataTask);
        Map<String, Object> map = new HashMap<>();
        map.put("dataPsi",dataPsi);
        map.put("dataPsiTask",DataPsiConvert.DataPsiTaskConvertVo(task));
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getPsiTaskList(PageReq req,String resultName) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("offset",req.getOffset());
        paramMap.put("pageSize",req.getPageSize());
        paramMap.put("resultName",resultName);
        List<DataPsiTaskVo> dataPsiTaskVos = dataPsiRepository.selectPsiTaskPage(paramMap);
        if (dataPsiTaskVos.size()==0){
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Long count = dataPsiRepository.selectPsiTaskPageCount(paramMap);
        return BaseResultEntity.success(new PageDataEntity(count.intValue(),req.getPageSize(),req.getPageNo(),dataPsiTaskVos));
    }

    public BaseResultEntity getOrganPsiTask(Long userId, String resultName, PageReq req) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userId",userId);
        paramMap.put("offset",req.getOffset());
        paramMap.put("pageSize",req.getPageSize());
        paramMap.put("resultName",resultName);
        List<DataOrganPsiTaskVo> dataOrganPsiTaskVos = dataPsiRepository.selectOrganPsiTaskPage(paramMap);
        if (dataOrganPsiTaskVos.size()==0)
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        Long count = dataPsiRepository.selectOrganPsiTaskPageCount(paramMap);
        return BaseResultEntity.success(new PageDataEntity(count.intValue(),req.getPageSize(),req.getPageNo(),dataOrganPsiTaskVos));
    }

    public BaseResultEntity getPsiTaskDetails(Long taskId, Long userId) {
        DataPsiTask task = dataPsiRepository.selectPsiTaskById(taskId);
        if (task==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到任务信息");
        DataPsi dataPsi = dataPsiRepository.selectPsiById(task.getPsiId());
        if (dataPsi==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到PSI信息");
        DataResource dataResource = dataResourceRepository.queryDataResourceByResourceFusionId(dataPsi.getOwnResourceId());
        Map<String, Object> otherDataResource = null;
        if (dataPsi.getOtherOrganId().equals(organConfiguration.getSysLocalOrganId())){
            DataResource otherResource = dataResourceRepository.queryDataResourceByResourceFusionId(dataPsi.getOwnResourceId());
            otherDataResource = new LinkedHashMap<>();
            otherDataResource.put("organName",organConfiguration.getSysLocalOrganName());
            otherDataResource.put("resourceName",otherResource.getResourceName());
        }else {
            BaseResultEntity baseResult = fusionResourceService.getDataResource(dataPsi.getServerAddress(), dataPsi.getOtherResourceId());
            if (baseResult.getCode()==0){
                otherDataResource = (LinkedHashMap)baseResult.getResult();
            }
        }
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        return BaseResultEntity.success(DataPsiConvert.DataPsiConvertVo(task,dataPsi,dataResource,otherDataResource,sysLocalOrganInfo));
    }

    public DataPsiTask selectPsiTaskById(Long taskId){
        return dataPsiRepository.selectPsiTaskById(taskId);
    }
    public DataPsi selectPsiById(Long psiId){
        return dataPsiRepository.selectPsiById(psiId);
    }

    public BaseResultEntity delPsiTask(Long taskId, Long userId) {
        DataPsiTask task = dataPsiRepository.selectPsiTaskById(taskId);
        if (task==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        if (task.getTaskState()==2)
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"运行中无法删除");
        dataPsiPrRepository.delPsiTask(task.getId());
        dataPsiPrRepository.delPsi(task.getPsiId());
        return BaseResultEntity.success();
    }

    public BaseResultEntity cancelPsiTask(Long taskId, Long userId) {
        DataPsiTask task = dataPsiRepository.selectPsiTaskById(taskId);
        task.setTaskState(4);
        dataPsiPrRepository.updateDataPsiTask(task);
        return BaseResultEntity.success();
    }

    public BaseResultEntity retryPsiTask(Long taskId, Long userId) {
        DataPsiTask task = dataPsiRepository.selectPsiTaskById(taskId);
        if (task.getTaskState()==1||task.getTaskState()==2)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"运行中或完成");
        if (task.getTaskState()<3)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"无法运行");
        DataPsi dataPsi = dataPsiRepository.selectPsiById(task.getPsiId());
        task.setTaskState(2);
        dataPsiPrRepository.updateDataPsiTask(task);
//        psiAsyncService.psiGrpcRun(task,dataPsi);
        return BaseResultEntity.success();
    }

    public BaseResultEntity runPsi(DataPsiTask psiTask, DataPsi dataPsi,DataTask dataTask){
        log.info("进入保存");
        syncPsi(psiTask,dataPsi,dataTask);
        log.info("结束保存");
        dataAsyncService.psiGrpcRun(psiTask,dataPsi,dataTask);
        log.info("结束运行");
        return BaseResultEntity.success();
    }

    public BaseResultEntity syncPsi(DataPsiTask psiTask, DataPsi dataPsi,DataTask dataTask) {
        DataTask dt = dataTaskRepository.selectDataTaskByTaskIdName(dataTask.getTaskIdName());
        if (dt==null){
            psiTask.setId(null);
            dataPsi.setId(null);
            dataTask.setTaskId(null);
            dataPsiPrRepository.saveDataPsi(dataPsi);
            psiTask.setPsiId(dataPsi.getId());
            dataPsiPrRepository.saveDataPsiTask(psiTask);
            dataTaskPrRepository.saveDataTask(dataTask);
        }else {
            dataTask.setTaskId(dt.getTaskId());
            dataTaskPrRepository.updateDataTask(dataTask);
            psiTask.setPsiId(dataPsi.getId());
            dataPsiPrRepository.updateDataPsiTaskByTaskId(psiTask);
        }
        return BaseResultEntity.success();
    }
}
