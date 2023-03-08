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
import com.primihub.biz.entity.data.dataenum.ModelStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.dto.LokiDto;
import com.primihub.biz.entity.data.dto.LokiResultContentDto;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DataPirTaskSyncReq;
import com.primihub.biz.entity.data.req.DataTaskReq;
import com.primihub.biz.entity.data.req.PageReq;
import com.primihub.biz.entity.data.vo.DataTaskVo;
import com.primihub.biz.entity.data.vo.ShareModelVo;
import com.primihub.biz.entity.data.vo.ShareProjectVo;
import com.primihub.biz.entity.sys.config.LokiConfig;
import com.primihub.biz.entity.sys.po.SysFile;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrganFusion;
import com.primihub.biz.repository.primarydb.data.*;
import com.primihub.biz.repository.resourceprimarydb.data.DataResourcePrimaryRepository;
import com.primihub.biz.repository.resourcesecondarydb.data.DataResourceSecondaryRepository;
import com.primihub.biz.repository.secondarydb.data.*;
import com.primihub.biz.service.sys.SysSseEmitterService;
import com.primihub.biz.service.sys.SysWebSocketService;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.comm.CommStorageUtil;
import com.primihub.biz.util.crypt.DateUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
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
    private DataResourcePrimaryRepository dataResourcePrimaryRepository;
    @Autowired
    private DataMpcRepository dataMpcRepository;
    @Autowired
    private DataMpcPrRepository dataMpcPrRepository;
    @Autowired
    private DataResourceSecondaryRepository dataResourceSecondaryRepository;
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
    @Autowired
    private DataProjectService dataProjectService;
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

    public Map<String, Object> initializeTableStructure(SysFile sysFile, List<DataFileField> dataFileFieldList) {
        Map<String, Object> map = new HashMap<>();
        map.put("tableName", sysFile.getFileName());
        List<String> columnList = new ArrayList<>();
        for (DataFileField dataFileField : dataFileFieldList) {
            if (StringUtils.isNotBlank(dataFileField.getFieldAs())) {
                columnList.add(dataFileField.getFieldAs());
            } else {
                columnList.add(dataFileField.getFieldName());
            }
        }
        map.put("columnList", columnList);
        dataResourcePrimaryRepository.dropDataTable(sysFile.getFileName());
        dataResourcePrimaryRepository.createDataTable(map);
        return map;
    }

    public void initializeTableData(Map<String, Object> map, List<String> fileContent) {
        List<String> columnList = (List<String>) map.get("columnList");
        List<List<Object>> dataList = new ArrayList();
        List<Object> lineContenList = null;
        fileContent.remove(0);
        for (String lineContent : fileContent) {
            String[] lineContentSplit = lineContent.split(",");
            if (lineContentSplit.length > 0) {
                lineContenList = new ArrayList<>();
                for (int i = 0; i < columnList.size(); i++) {
                    String val = "";
                    try {
                        val = lineContentSplit[i];
                    } catch (Exception e) {
                        log.info("解析:{} - index:{} - lineContent:{} - 异常：{}", map.get("tableName"), i, lineContent, e.getMessage());
                    }
                    lineContenList.add(val);
                }
                dataList.add(lineContenList);
            }
        }
        int totalPage = dataList.size() % DataConstant.INSERT_DATA_TABLE_PAGESIZE == 0 ? dataList.size() / DataConstant.INSERT_DATA_TABLE_PAGESIZE : dataList.size() / DataConstant.INSERT_DATA_TABLE_PAGESIZE + 1;
        for (int i = 0; i < totalPage; i++) {
            map.put("dataList", dataList.stream().skip(i * DataConstant.INSERT_DATA_TABLE_PAGESIZE).limit(DataConstant.INSERT_DATA_TABLE_PAGESIZE).collect(Collectors.toList()));
            dataResourcePrimaryRepository.insertDataTable(map);
        }
    }

    public void handleRunSql(String paramStr) {
        JSONObject jsonObject = JSONObject.parseObject(paramStr);
        Long taskId = jsonObject.getLong("taskId");
        String scriptSqlContent = jsonObject.getString("scriptSqlContent");
        DataMpcTask dataMpcTask = dataMpcRepository.selectDataMpcTaskById(taskId);
        dataMpcTask.setTaskStatus(2);
        this.dataMpcPrRepository.updateDataMpcTask(dataMpcTask);
        try {
            List<Map> all = dataResourceSecondaryRepository.findAll(scriptSqlContent);
            if (all != null && all.size() > 0) {
                Set columnSet = new TreeSet(all.get(0).keySet());
                if (columnSet.size()>0){
                    List<String> dataList = new ArrayList<>();
                    dataList.add(StringUtils.join(columnSet.toArray(),","));
                    StringBuilder sb=new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append("mpc").append("/").append(DateUtil.formatDate(new Date(),DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat()));
                    String filePath = sb.toString();
                    List<String> dataValList = null;
                    for (Map map : all) {
                        dataValList = new ArrayList<>();
                        for (Object key : columnSet) {
                            dataValList.add(map.get(key)==null?"":map.get(key).toString());
                        }
                        dataList.add(StringUtils.join(dataValList.toArray(),","));
                    }
                    sb.append("/").append(dataMpcTask.getTaskIdName()).append(".csv");
                    dataMpcTask.setResultFilePath(sb.toString());
                    FileUtil.writeFile(filePath,dataMpcTask.getResultFilePath(),dataList);
                }
            }
            dataMpcTask.setTaskStatus(1);
        } catch (Exception e) {
            dataMpcTask.setResultFilePath(null);
            dataMpcTask.setLogData(e.getMessage());
            dataMpcTask.setTaskStatus(3);
        }
        this.dataMpcPrRepository.updateDataMpcTask(dataMpcTask);
    }

    public void batchDataFusionResource(String paramStr){
//        log.info(paramStr);
        SysOrganFusion sysOrganFusion = JSONObject.parseObject(paramStr, SysOrganFusion.class);
        Long maxId=dataResourceRepository.findMaxDataResource();
        if (maxId==null)
            return;
        DataFusionCopyTask task = new DataFusionCopyTask(1,1L,maxId, DataFusionCopyEnum.RESOURCE.getTableName(), sysOrganFusion.getServerAddress());
        dataCopyPrimarydbRepository.saveCopyInfo(task);
        dataCopyService.handleFusionCopyTask(task);
    }

    public void singleDataFusionResource(String paramStr){
//        log.info(paramStr);
        DataResource dataResource = JSONObject.parseObject(paramStr, DataResource.class);
        Iterator<Map.Entry<String, SysOrganFusion>> iterator = organConfiguration.getSysLocalOrganInfo().getFusionMap().entrySet().iterator();
        while (iterator.hasNext()){
            SysOrganFusion sysOrganFusion = iterator.next().getValue();
            if (sysOrganFusion.isRegistered()){
                DataFusionCopyTask task = new DataFusionCopyTask(2,-1L,dataResource.getResourceId(), DataFusionCopyEnum.RESOURCE.getTableName(), sysOrganFusion.getServerAddress());
                dataCopyPrimarydbRepository.saveCopyInfo(task);
                dataCopyService.handleFusionCopyTask(task);
            }
        }
    }

    public void compareAndFixLocalOrganName(String paramStr){
        List<DataResourceVisibilityAuth> list=JSONObject.parseArray(paramStr, DataResourceVisibilityAuth.class);
        Map<String,List<DataResourceVisibilityAuth>> groupMap=list.stream().collect(Collectors.groupingBy(DataResourceVisibilityAuth::getOrganServerAddress,Collectors.toList()));
        Iterator<String> it=groupMap.keySet().iterator();
        SysLocalOrganInfo sysLocalOrganInfo=organConfiguration.getSysLocalOrganInfo();
        while(it.hasNext()){
            String key=it.next();
            List<DataResourceVisibilityAuth> currentList=groupMap.get(key);
            List<String> organGlobalIdList=currentList.stream().map(item->item.getOrganGlobalId()).collect(Collectors.toList());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
            map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
            map.put("globalIdArray", organGlobalIdList);
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            BaseResultEntity<Map<String,Object>> resultEntity=restTemplate.postForObject(key+"/fusion/findOrganByGlobalId",request, BaseResultEntity.class);
            if(resultEntity!=null&&resultEntity.getCode()== BaseResultEnum.SUCCESS.getReturnCode()&&resultEntity.getResult()!=null){
                Map resultMap=resultEntity.getResult();
                if(resultMap.get("organList")!=null) {
                    List<Map<String,String>> organDtoList = (List<Map<String,String>>)  resultMap.get("organList");
                    if(organDtoList.size()!=0) {
                        Map<String, String> organDtoMap = organDtoList.stream().collect(Collectors.toMap(item->item.get("globalId"), item->item.get("globalName")));
                        for (DataResourceVisibilityAuth auth : currentList) {
                            String currentName=organDtoMap.get(auth.getOrganGlobalId());
                            if(currentName!=null&&!currentName.equals(auth.getOrganName())){
                                dataResourcePrRepository.updateVisibilityAuthName(currentName, auth.getOrganGlobalId());
                            }
                        }
                    }
                }
            }
        }
    }

    public void spreadProjectData(String paramStr){
        String sysLocalOrganId = organConfiguration.getSysLocalOrganId();
        log.info(paramStr);
        ShareProjectVo shareProjectVo = JSONObject.parseObject(paramStr, ShareProjectVo.class);
        shareProjectVo.supplement();
        shareProjectVo.getProjectOrgans().addAll(dataProjectRepository.selectDataProjcetOrganByProjectId(shareProjectVo.getProjectId()));
        if (shareProjectVo.getProjectResources().size()==0)
            shareProjectVo.getProjectResources().addAll(dataProjectRepository.selectProjectResourceByProjectId(shareProjectVo.getProjectId()));
        if(StringUtils.isNotBlank(shareProjectVo.getServerAddress())){
            List<DataProjectOrgan> dataProjectOrgans = shareProjectVo.getProjectOrgans();
            if (dataProjectOrgans.size()==0)
                return;
            List<String> organIds = dataProjectOrgans.stream().map(DataProjectOrgan::getOrganId).collect(Collectors.toList());
            Map<String, Map> organListMap = dataProjectService.getOrganListMap(organIds, shareProjectVo.getServerAddress());
            List<String> organNames = new ArrayList<>();
            for (DataProjectOrgan dataProjectOrgan : dataProjectOrgans) {
                if (!sysLocalOrganId.equals(dataProjectOrgan.getOrganId())){
                    if (organListMap.containsKey(dataProjectOrgan.getOrganId())){
                        Map map = organListMap.get(dataProjectOrgan.getOrganId());
                        organNames.add(map.get("globalName").toString());
                        Object gatewayAddress = map==null?null:map.get("gatewayAddress");
                        if (gatewayAddress==null&&StringUtils.isBlank(gatewayAddress.toString())){
                            log.info("projectId:{} - OrganId:{} gatewayAddress null",dataProjectOrgan.getProjectId(),dataProjectOrgan.getOrganId());
                            return;
                        }
                        log.info("projectId:{} - OrganId:{} gatewayAddress api start:{}",dataProjectOrgan.getProjectId(),dataProjectOrgan.getOrganId(),System.currentTimeMillis());
                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_JSON);
                        HttpEntity<HashMap<String, Object>> request = new HttpEntity(shareProjectVo, headers);
                        log.info(CommonConstant.PROJECT_SYNC_API_URL.replace("<address>", gatewayAddress.toString()));
                        try {
                            BaseResultEntity baseResultEntity = restTemplate.postForObject(CommonConstant.PROJECT_SYNC_API_URL.replace("<address>", gatewayAddress.toString()), request, BaseResultEntity.class);
                            log.info("baseResultEntity code:{} msg:{}",baseResultEntity.getCode(),baseResultEntity.getMsg());
                        }catch (Exception e){
                            log.info("projectId:{} - OrganId:{} gatewayAddress api Exception:{}",dataProjectOrgan.getProjectId(),dataProjectOrgan.getOrganId(),e.getMessage());
                        }
                        log.info("projectId:{} - OrganId:{} gatewayAddress api end:{}",dataProjectOrgan.getProjectId(),dataProjectOrgan.getOrganId(),System.currentTimeMillis());
                    }
                }
            }
            log.info("开始向调度方进行项目同步");
            spreadDispatchlData(CommonConstant.PROJECT_SYNC_API_URL,shareProjectVo);
            log.info("结束向调度方进行项目同步");
            DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(null, shareProjectVo.getProjectId());
            dataProject.setResourceNum(dataProjectRepository.selectProjectResourceByProjectId(shareProjectVo.getProjectId()).size());
//            dataProject.setProviderOrganNames(StringUtils.join(organNames,","));
            dataProjectPrRepository.updateDataProject(dataProject);
        }
    }

    public void spreadModelData(String paramStr){
        log.info(paramStr);
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
            Map<String, Map> organListMap = dataProjectService.getOrganListMap(shareModelVo.getShareOrganId(), shareModelVo.getServerAddress());
            for (String organId : shareModelVo.getShareOrganId()) {
                if (!organId.equals(organConfiguration.getSysLocalOrganId())&&organListMap.containsKey(organId)){
                    Map map = organListMap.get(organId);
                    Object gatewayAddress = map==null?null:map.get("gatewayAddress");
                    if (gatewayAddress==null&&StringUtils.isBlank(gatewayAddress.toString())){
                        log.info("OrganId:{} gatewayAddress null",organId);
                        return;
                    }
                    log.info("OrganId:{} gatewayAddress api start:{}",organId,System.currentTimeMillis());
                    shareModelVo.supplement();
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);
                    HttpEntity<HashMap<String, Object>> request = new HttpEntity(shareModelVo, headers);
                    log.info(CommonConstant.MODEL_SYNC_API_URL.replace("<address>", gatewayAddress.toString()));
                    try {
                        BaseResultEntity baseResultEntity = restTemplate.postForObject(CommonConstant.MODEL_SYNC_API_URL.replace("<address>", gatewayAddress.toString()), request, BaseResultEntity.class);
                        log.info("baseResultEntity code:{} msg:{}",baseResultEntity.getCode(),baseResultEntity.getMsg());
                    }catch (Exception e){
                        log.info("modelUUID:{} - OrganId:{} gatewayAddress api Exception:{}",shareModelVo.getDataModel().getModelUUID(),organId,e.getMessage());
                    }
                    log.info("modelUUID:{} - OrganId:{} gatewayAddress api end:{}",shareModelVo.getDataModel().getModelUUID(),organId,System.currentTimeMillis());
                }
            }
            spreadDispatchlData(CommonConstant.MODEL_SYNC_API_URL,shareModelVo);
        }
    }

    private void spreadDispatchlData(String url,Object shareVo){
        if (StringUtils.isBlank(baseConfiguration.getDispatchUrl()))
            return;
        String gatewayAddress = baseConfiguration.getDispatchUrl();
        log.info("DispatchUrl{}",gatewayAddress);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<HashMap<String, Object>> request = new HttpEntity(shareVo, headers);
        log.info(CommonConstant.MODEL_SYNC_API_URL.replace("<address>", gatewayAddress.toString()));
        try {
            BaseResultEntity baseResultEntity = restTemplate.postForObject(url.replace("<address>", gatewayAddress.toString()), request, BaseResultEntity.class);
            log.info("baseResultEntity code:{} msg:{}",baseResultEntity.getCode(),baseResultEntity.getMsg());
        }catch (Exception e){
            log.info("Dispatch gatewayAddress api Exception:{}",e.getMessage());
        }
        log.info("出去");
    }

    public BaseResultEntity getModelTaskList(Long modelId,PageReq req) {
        Map<String,Object> map = new HashMap<>();
        map.put("modelId",modelId);
        map.put("offset",req.getOffset());
        map.put("pageSize",req.getPageSize());
        List<DataModelTask> dataModelTasks = dataModelRepository.queryModelTaskByModelId(map);
        if (dataModelTasks.size()==0)
            return BaseResultEntity.success(new PageDataEntity(0, req.getPageSize(), req.getPageNo(),new ArrayList()));
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
        if (taskId!=null&&taskId!=0L)
            return dataTaskRepository.selectDataTaskByTaskId(taskId);
        return null;
    }

    public BaseResultEntity getTaskData(Long taskId) {
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (dataTask==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"为查询到任务信息");
        return BaseResultEntity.success(DataTaskConvert.dataTaskPoConvertDataModelTaskList(dataTask));
    }

    public BaseResultEntity deleteTaskData(Long taskId) {
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (dataTask==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"无任务信息");
        if (dataTask.getTaskState()==2)
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"任务运行中无法删除");
        if (dataTask.getTaskType().equals(TaskTypeEnum.MODEL.getTaskType())){
            dataAsyncService.deleteModelTask(dataTask);
            dataTask.setTaskState(TaskStateEnum.DELETE.getStateType());
            dataTaskPrRepository.updateDataTask(dataTask);
        }else {
            dataTaskPrRepository.deleteDataTask(taskId);
        }
        return BaseResultEntity.success();
    }


    public BaseResultEntity cancelTask(Long taskId) {
        DataTask rawDataTask = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (rawDataTask==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"无任务信息");
        if (!rawDataTask.getTaskState().equals(TaskStateEnum.IN_OPERATION.getStateType()))
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"无法取消,任务状态不是运行中");
        rawDataTask.setTaskState(TaskStateEnum.CANCEL.getStateType());
        dataTaskPrRepository.updateDataTask(rawDataTask);
        return BaseResultEntity.success(taskId);
    }

    public BaseResultEntity getTaskLogInfo(Long taskId) {
        LokiConfig lokiConfig = baseConfiguration.getLokiConfig();
        if (lokiConfig == null || StringUtils.isBlank(lokiConfig.getAddress())
                || StringUtils.isBlank(lokiConfig.getJob()) || StringUtils.isBlank(lokiConfig.getContainer()))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"请检查loki配置信息");
        DataTask rawDataTask = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (rawDataTask==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"无任务信息");
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
            if (!runFile.exists())
                runFile.mkdirs();
            generateLogFile(file,dataTask);
        }
        return file;
    }

    public void generateLogFile(File file,DataTask dataTask){
        try {
            List<String[]> lokiLogList = getLokiLogList(dataTask.getTaskIdName(), dataTask.getTaskStartTime()/1000);
            if (lokiLogList==null || lokiLogList.isEmpty())
                return;
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
        ||StringUtils.isBlank(lokiConfig.getContainer()))
            return null;
        String query = "query={job =\""+lokiConfig.getJob()+"\", container=\""+lokiConfig.getContainer()+"\"} |= \""+taskId+"\"";
        String url = "http://"+lokiConfig.getAddress()+"/loki/api/v1/query_range?start="+start+"&direction=forward&"+query;
        log.info(url);
        LokiDto lokiDto = restTemplate.getForObject(url, LokiDto.class);
        if (lokiDto==null || lokiDto.getData()==null || lokiDto.getData().getResult()==null || lokiDto.getData().getResult().size()==0)
            return null;
        List<String[]> logArray = new ArrayList<>();
        List<LokiResultContentDto> result = lokiDto.getData().getResult();
        for (LokiResultContentDto lokiResultContentDto : result) {
            logArray.addAll(lokiResultContentDto.getValues());
        }
        return logArray;
    }


    public BaseResultEntity getTaskList(DataTaskReq req) {
        List<DataTaskVo> dataTaskVos = dataTaskRepository.selectDataTaskList(req);
        if (dataTaskVos.size()==0)
            return BaseResultEntity.success(new PageDataEntity(0, req.getPageSize(), req.getPageNo(),new ArrayList()));
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

    public BaseResultEntity saveDataTask(DataPirTaskSyncReq req) {
        DataTask dt = dataTaskRepository.selectDataTaskByTaskIdName(req.getDataTask().getTaskIdName());
        if (dt==null){
            req.getDataTask().setTaskId(null);
            dataTaskPrRepository.saveDataTask(req.getDataTask());
            req.getDataPirTask().setTaskId(req.getDataTask().getTaskId());
            dataTaskPrRepository.saveDataPirTask(req.getDataPirTask());
        }else {
            req.getDataTask().setTaskId(dt.getTaskId());
            dataTaskPrRepository.updateDataTask(req.getDataTask());
        }

        return BaseResultEntity.success();
    }
}

