package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.constant.CommonConstant;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.convert.DataTaskConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.dataenum.DataFusionCopyEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.dto.LokiDto;
import com.primihub.biz.entity.data.dto.LokiResultContentDto;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DataTaskReq;
import com.primihub.biz.entity.data.req.PageReq;
import com.primihub.biz.entity.data.vo.DataTaskVo;
import com.primihub.biz.entity.data.vo.ShareModelVo;
import com.primihub.biz.entity.data.vo.ShareProjectVo;
import com.primihub.biz.entity.sys.config.LokiConfig;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.entity.sys.po.SysOrganFusion;
import com.primihub.biz.repository.primarydb.data.*;
import com.primihub.biz.repository.secondarydb.data.*;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.sys.SysSseEmitterService;
import com.primihub.biz.service.sys.SysUserService;
import com.primihub.biz.service.sys.SysWebSocketService;
import com.primihub.biz.util.comm.CommStorageUtil;
import com.primihub.biz.util.crypt.DateUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import com.primihub.sdk.task.TaskHelper;
import com.primihub.sdk.task.param.TaskParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Resource;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataTaskService {
    @Autowired
    private DataTaskRepository dataTaskRepository;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;
    @Autowired
    private DataResourcePrRepository dataResourcePrRepository;
    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private DataCopyPrimarydbRepository dataCopyPrimarydbRepository;
    @Autowired
    private DataCopyService dataCopyService;
    @Autowired
    private DataProjectRepository dataProjectRepository;
    @Autowired
    private DataProjectPrRepository dataProjectPrRepository;
    @Resource(name="soaRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private DataModelRepository dataModelRepository;
    @Autowired
    private SysSseEmitterService sseEmitterService;
    @Autowired
    private SysWebSocketService webSocketService;
    @Autowired
    private DataAsyncService dataAsyncService;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private SysOrganSecondarydbRepository sysOrganSecondarydbRepository;
    @Autowired
    private DataPsiRepository dataPsiRepository;
    @Autowired
    private DataPsiPrRepository dataPsiPrRepository;

    public List<DataFileField> batchInsertDataFileField(DataResource dataResource) {
        List<DataFileField> fileFieldList = new ArrayList<>();
        String fileHandleField = dataResource.getFileHandleField();
        int i = 1;
        if (StringUtils.isNotBlank(fileHandleField)) {
            String[] fieldSplit = fileHandleField.split(",");
            for (String fieldName : fieldSplit) {
                if (StringUtils.isNotBlank(fieldName)) {
                    if (fieldName.substring(0, 1).matches(DataConstant.MATCHES)) {
                        fileFieldList.add(new DataFileField(dataResource.getFileId(), dataResource.getResourceId(), fieldName, null));
                    } else {
                        fileFieldList.add(new DataFileField(dataResource.getFileId(), dataResource.getResourceId(), fieldName, DataConstant.FIELD_NAME_AS + i));
                        i++;
                    }
                } else {
                    fileFieldList.add(new DataFileField(dataResource.getFileId(), dataResource.getResourceId(), fieldName, DataConstant.FIELD_NAME_AS + i));
                    i++;
                }
            }
            dataResourcePrRepository.saveResourceFileFieldBatch(fileFieldList);
        }
        return fileFieldList;
    }


    public void batchDataFusionResource(String paramStr){
        log.info(paramStr);
        SysOrgan sysOrgan = JSONObject.parseObject(paramStr, SysOrgan.class);
        Long maxId=dataResourceRepository.findMaxDataResource();
        if (maxId==null) {
            return;
        }
        if (maxId==1L){
            maxId++;
        }
        DataFusionCopyTask task = new DataFusionCopyTask(1,1L,maxId, DataFusionCopyEnum.FUSION_RESOURCE.getTableName(), sysOrgan.getOrganGateway(),sysOrgan.getOrganId());
        dataCopyPrimarydbRepository.saveCopyInfo(task);
        dataCopyService.handleFusionCopyTask(task);

    }

    public void singleDataFusionResource(String paramStr){
//        log.info(paramStr);
        DataResource dataResource = JSONObject.parseObject(paramStr, DataResource.class);
        List<SysOrgan> sysOrgans = sysOrganSecondarydbRepository.selectSysOrganByExamine();
        for (SysOrgan sysOrgan : sysOrgans) {
            DataFusionCopyTask task= new DataFusionCopyTask(2,-1L,dataResource.getResourceId(), DataFusionCopyEnum.FUSION_RESOURCE.getTableName(), sysOrgan.getOrganGateway(),sysOrgan.getOrganId());
            dataCopyPrimarydbRepository.saveCopyInfo(task);
            dataCopyService.handleFusionCopyTask(task);
        }
    }


    public void spreadProjectData(String paramStr){
        String sysLocalOrganId = organConfiguration.getSysLocalOrganId();
        log.info(paramStr);
        ShareProjectVo shareProjectVo = JSONObject.parseObject(paramStr, ShareProjectVo.class);
        shareProjectVo.supplement();
        shareProjectVo.getProjectOrgans().addAll(dataProjectRepository.selectDataProjcetOrganByProjectId(shareProjectVo.getProjectId()));
        if (shareProjectVo.getProjectResources().size()==0) {
            shareProjectVo.getProjectResources().addAll(dataProjectRepository.selectProjectResourceByProjectId(shareProjectVo.getProjectId()));
        }
        List<DataProjectOrgan> dataProjectOrgans = shareProjectVo.getProjectOrgans();
        if (dataProjectOrgans.size()==0) {
            return;
        }
        List<String> organIds = dataProjectOrgans.stream().map(DataProjectOrgan::getOrganId).collect(Collectors.toList());
        Map<String, SysOrgan> organListMap = otherBusinessesService.getOrganListMap(organIds);
        List<String> organNames = new ArrayList<>();
        for (DataProjectOrgan dataProjectOrgan : dataProjectOrgans) {
            if (!sysLocalOrganId.equals(dataProjectOrgan.getOrganId())){
                if (organListMap.containsKey(dataProjectOrgan.getOrganId())){
                    SysOrgan sysOrgan = organListMap.get(dataProjectOrgan.getOrganId());
                    Object gatewayAddress = sysOrgan==null?null:sysOrgan.getOrganGateway();
                    if (gatewayAddress==null&&StringUtils.isBlank(gatewayAddress.toString())){
                        log.info("projectId:{} - OrganId:{} gatewayAddress null",dataProjectOrgan.getProjectId(),dataProjectOrgan.getOrganId());
                        return;
                    }
                    String url = CommonConstant.PROJECT_SYNC_API_URL.replace("<address>", gatewayAddress.toString());
                    String publicKey = sysOrgan.getPublicKey();
                    organNames.add(sysOrgan.getOrganName());
                    log.info("projectId:{} - OrganId:{} gatewayAddress api start:{}",dataProjectOrgan.getProjectId(),dataProjectOrgan.getOrganId(),System.currentTimeMillis());
                    otherBusinessesService.syncGatewayApiData(shareProjectVo,url,publicKey);
                    log.info("projectId:{} - OrganId:{} gatewayAddress api end:{}",dataProjectOrgan.getProjectId(),dataProjectOrgan.getOrganId(),System.currentTimeMillis());
                }
            }
        }
        DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(null, shareProjectVo.getProjectId());
        dataProject.setResourceNum(dataProjectRepository.selectProjectResourceByProjectId(shareProjectVo.getProjectId()).size());
//        dataProject.setProviderOrganNames(StringUtils.join(organNames,","));
        dataProjectPrRepository.updateDataProject(dataProject);
    }

    public void spreadModelData(String paramStr){
//        log.info(paramStr);
        ShareModelVo shareModelVo = JSONObject.parseObject(paramStr, ShareModelVo.class);
        if (shareModelVo.getShareOrganId() == null && shareModelVo.getShareOrganId().isEmpty()) {
            log.info("no shareOrganId");
            return;
        }
        if (shareModelVo.getDataModel()!=null&&shareModelVo.getDataModel().getProjectId()!=null&&shareModelVo.getDataModel().getProjectId()!=0L){
            DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(shareModelVo.getDataModel().getProjectId(), null);
            if (dataProject==null){
                log.info("spread modelUUID:{},no project data",shareModelVo.getDataModel().getModelUUID());
                return;
            }
            shareModelVo.init(dataProject);
            shareModelVo.getDataModel().setProjectId(null);
            Map<String, SysOrgan> organListMap = otherBusinessesService.getOrganListMap(shareModelVo.getShareOrganId());
            for (String organId : shareModelVo.getShareOrganId()) {
                if (!organId.equals(organConfiguration.getSysLocalOrganId())&&organListMap.containsKey(organId)){
                    SysOrgan sysOrgan = organListMap.get(organId);
                    Object gatewayAddress = sysOrgan==null?null:sysOrgan.getOrganGateway();
                    if (gatewayAddress==null&&StringUtils.isBlank(gatewayAddress.toString())){
                        log.info("OrganId:{} gatewayAddress null",organId);
                        return;
                    }
                    log.info("OrganId:{} gatewayAddress api start:{}",organId,System.currentTimeMillis());
                    String url = CommonConstant.MODEL_SYNC_API_URL.replace("<address>", gatewayAddress.toString());
                    String publicKey = sysOrgan.getPublicKey();
                    otherBusinessesService.syncGatewayApiData(shareModelVo,url,publicKey);
                    log.info("modelUUID:{} - OrganId:{} gatewayAddress api end:{}",shareModelVo.getDataModel().getModelUUID(),organId,System.currentTimeMillis());
                }
            }
        }
    }

    public BaseResultEntity getModelTaskList(Long modelId,PageReq req) {
        Map<String,Object> map = new HashMap<>();
        map.put("modelId",modelId);
        map.put("offset",req.getOffset());
        map.put("pageSize",req.getPageSize());
        List<DataModelTask> dataModelTasks = dataModelRepository.queryModelTaskByModelId(map);
        if (dataModelTasks.size()==0) {
            return BaseResultEntity.success(new PageDataEntity(0, req.getPageSize(), req.getPageNo(),new ArrayList()));
        }
        Integer count = dataModelRepository.queryModelTaskByModelIdCount(modelId);
        Set<Long> taskId = dataModelTasks.stream().map(DataModelTask::getTaskId).collect(Collectors.toSet());
        List<DataTask> dataTaskList = dataTaskRepository.selectDataTaskByTaskIds(taskId);
        return BaseResultEntity.success(new PageDataEntity(count,req.getPageSize(),req.getPageNo(),dataTaskList.stream().map(DataTaskConvert::dataTaskPoConvertDataModelTaskList).collect(Collectors.toList())));
    }

    public DataTask getDataTaskById(Long taskId,Long modelId){
        if (modelId!=null&&modelId!=0L){
            Map<String, Object> map = new HashMap<>();
            map.put("modelId",modelId);
            map.put("offset",0);
            map.put("pageSize",100);
            List<DataModelTask> dataModelTasks = dataModelRepository.queryModelTaskByModelId(map);
            taskId = dataModelTasks.stream().mapToLong(DataModelTask::getTaskId).max().orElse(0);
        }
        if (taskId!=null&&taskId!=0L) {
            return dataTaskRepository.selectDataTaskByTaskId(taskId);
        }
        return null;
    }

    public BaseResultEntity getTaskData(Long taskId) {
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (dataTask==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"为查询到任务信息");
        }
        return BaseResultEntity.success(DataTaskConvert.dataTaskPoConvertDataModelTaskList(dataTask));
    }

    public BaseResultEntity deleteTaskData(Long taskId) {
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (dataTask==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"无任务信息");
        }
        if (dataTask.getTaskState()==2) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"任务运行中无法删除");
        }
        if (dataTask.getTaskType().equals(TaskTypeEnum.MODEL.getTaskType())){
            dataAsyncService.deleteModelTask(dataTask);
            dataTask.setTaskState(TaskStateEnum.DELETE.getStateType());
            dataTaskPrRepository.updateDataTask(dataTask);
        }else {
            dataTaskPrRepository.deleteDataTask(taskId);
        }
        return BaseResultEntity.success();
    }


    public BaseResultEntity cancelTask(String taskId) {
        DataTask rawDataTask = dataTaskRepository.selectDataTaskByTaskIdName(taskId);
        if (rawDataTask==null){
            rawDataTask = dataTaskRepository.selectDataTaskByTaskId(Long.valueOf(taskId));
        }
        if (rawDataTask==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"无任务信息");
        }
        if (!rawDataTask.getTaskState().equals(TaskStateEnum.IN_OPERATION.getStateType())) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"无法取消,任务状态不是运行中");
        }
        TaskParam taskParam = dataAsyncService.getTaskHelper().killTask(rawDataTask.getTaskIdName());
        if (taskParam.getSuccess()){
            rawDataTask.setTaskState(TaskStateEnum.CANCEL.getStateType());
            rawDataTask.setTaskEndTime(System.currentTimeMillis());
            dataTaskPrRepository.updateDataTask(rawDataTask);
            if (rawDataTask.getTaskType().equals(TaskTypeEnum.PSI.getTaskType())){
                DataPsiTask task = dataPsiRepository.selectPsiTaskByTaskId(rawDataTask.getTaskIdName());
                task.setTaskState(rawDataTask.getTaskState());
                dataPsiPrRepository.updateDataPsiTask(task);
            }
            return BaseResultEntity.success(taskId);
        }
        return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,taskParam.getError());
    }

    public BaseResultEntity getTaskLogInfo(Long taskId) {
        LokiConfig lokiConfig = baseConfiguration.getLokiConfig();
        if (lokiConfig == null || StringUtils.isBlank(lokiConfig.getAddress())
                || StringUtils.isBlank(lokiConfig.getJob()) || StringUtils.isBlank(lokiConfig.getContainer())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"请检查loki配置信息");
        }
        DataTask rawDataTask = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (rawDataTask==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"无任务信息");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("address",lokiConfig.getAddress());
        map.put("job",lokiConfig.getJob());
        map.put("container",lokiConfig.getContainer());
        map.put("taskIdName", rawDataTask.getTaskIdName());
        if (rawDataTask.getTaskStartTime()==null){
            map.put("start",(System.currentTimeMillis()/1000));
        }else {
            map.put("start",(rawDataTask.getTaskStartTime()/1000));
        }
        return BaseResultEntity.success(map);
    }

    public File getLogFile(DataTask dataTask) {
        String filePath = baseConfiguration.getRunModelFileUrlDirPrefix()+ dataTask.getTaskIdName() + File.separator + DataConstant.TASK_LOG_FILE_NAME;
        log.info(filePath);
        File file = new File(filePath);
        if (!file.exists()){
            File runFile = new File(baseConfiguration.getRunModelFileUrlDirPrefix()+ dataTask.getTaskIdName());
            if (!runFile.exists()) {
                runFile.mkdirs();
            }
            generateLogFile(file,dataTask);
        }
        return file;
    }

    public void generateLogFile(File file,DataTask dataTask){
        try {
            List<String[]> lokiLogList = getLokiLogList(dataTask.getTaskIdName(), dataTask.getTaskStartTime()/1000);
            if (lokiLogList==null || lokiLogList.isEmpty()) {
                return;
            }
            log.info("{}",lokiLogList.size());
            boolean next = true;
            Long nextTsNs = 0L;
            Long ts = 0L;
            file.createNewFile();
            FileOutputStream fos=new FileOutputStream(file);
            OutputStreamWriter osw=new OutputStreamWriter(fos, "UTF-8");
            BufferedWriter bw=new BufferedWriter(osw);
            while (next){
                for(String[] logInfo:lokiLogList){
                    long logId = Long.parseLong(logInfo[0]);
                    if (logId>nextTsNs){
                        nextTsNs = logId;
                        JSONObject jsonObject = JSONObject.parseObject(logInfo[1]);
                        bw.write(jsonObject.get("log").toString());
                        ts =  DateUtil.parseDate(jsonObject.get("time").toString(), DateUtil.DateStyle.UTC_DEFAULT.getFormat()).getTime();
                    }
                }
                if (lokiLogList.size()==100){
                    lokiLogList = getLokiLogList(dataTask.getTaskIdName(), ts/1000);
                }else {
                    next = false;
                }
            }
            bw.close();
            osw.close();
            fos.close();
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<String[]> getLokiLogList(String taskId,Long start){
        LokiConfig lokiConfig = baseConfiguration.getLokiConfig();
        if (lokiConfig==null || StringUtils.isBlank(lokiConfig.getAddress())|| StringUtils.isBlank(lokiConfig.getJob())
        ||StringUtils.isBlank(lokiConfig.getContainer())) {
            return null;
        }
        String query = "query={job =\""+lokiConfig.getJob()+"\", container=\""+lokiConfig.getContainer()+"\"} |= \""+taskId+"\"";
        String url = "http://"+lokiConfig.getAddress()+"/loki/api/v1/query_range?start="+start+"&direction=forward&"+query;
        log.info(url);
        LokiDto lokiDto = restTemplate.getForObject(url, LokiDto.class);
        if (lokiDto==null || lokiDto.getData()==null || lokiDto.getData().getResult()==null || lokiDto.getData().getResult().size()==0) {
            return null;
        }
        List<String[]> logArray = new ArrayList<>();
        List<LokiResultContentDto> result = lokiDto.getData().getResult();
        for (LokiResultContentDto lokiResultContentDto : result) {
            logArray.addAll(lokiResultContentDto.getValues());
        }
        return logArray;
    }


    public BaseResultEntity getTaskList(DataTaskReq req) {
        List<DataTaskVo> dataTaskVos = dataTaskRepository.selectDataTaskList(req);
        if (dataTaskVos.size()==0) {
            return BaseResultEntity.success(new PageDataEntity(0, req.getPageSize(), req.getPageNo(),new ArrayList()));
        }
        Integer count = dataTaskRepository.selectDataTaskListCount(req);
        return BaseResultEntity.success(new PageDataEntity(count, req.getPageSize(), req.getPageNo(),dataTaskVos));
    }

    public SseEmitter connectSseTask(String taskId,Integer all) {
        boolean isReal = true;
        DataTask dataTask = null;
        if (StringUtils.isBlank(taskId)){
            taskId = SnowflakeId.getInstance().toString();
            isReal = false;
        }else {
            dataTask  = dataTaskRepository.selectDataTaskByTaskId(Long.valueOf(taskId));
            if (dataTask==null){
                taskId = String.valueOf(SnowflakeId.getInstance().nextId());
                isReal = false;
            }else {
                taskId = dataTask.getTaskIdName();
            }
        }
        if(CommStorageUtil.getSseEmitterMap().containsKey(taskId)){
            sseEmitterService.removeKey(taskId);
        }
        SseEmitter sseEmitter = sseEmitterService.connect(taskId);
        if (!isReal){
            sseEmitterService.sendMessage(taskId,"未查询到任务信息");
        }else {
            // 创建web
            LokiConfig lokiConfig = baseConfiguration.getLokiConfig();
            if (lokiConfig==null || StringUtils.isBlank(lokiConfig.getAddress())|| StringUtils.isBlank(lokiConfig.getJob())
                    ||StringUtils.isBlank(lokiConfig.getContainer())){
                sseEmitterService.sendMessage(taskId,"确实日志loki配置,请检查base.json文件");
            }else {
                try {
                    String query = "";
                    if (all == 1){
                        query = "{job =\""+lokiConfig.getJob()+"\", container=\""+lokiConfig.getContainer()+"\"}";
                    }else {
                        query = "{job =\""+lokiConfig.getJob()+"\", container=\""+lokiConfig.getContainer()+"\"} |= \""+taskId+"\"";
                    }
                    String url = "ws://"+lokiConfig.getAddress()+"/loki/api/v1/tail?start="+(dataTask.getTaskStartTime()/1000)+"&direction=forward&query="+URLEncoder.encode(query, "UTF-8");
                    log.info(url);
                    webSocketService.connect(taskId,url);
                }catch (Exception e){
                    log.info(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        return sseEmitter;
    }

    public void removeSseTask(String taskId) {
        sseEmitterService.removeKey(taskId);
    }

    public BaseResultEntity updateTaskDesc(Long taskId, String taskDesc) {
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (dataTask==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"无任务信息");
        }
        dataTask.setTaskDesc(taskDesc);
        dataTaskPrRepository.updateDataTask(dataTask);
        return BaseResultEntity.success();
    }
}

