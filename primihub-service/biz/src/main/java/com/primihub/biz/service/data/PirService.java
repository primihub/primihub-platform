package com.primihub.biz.service.data;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.thread.ThreadPoolConfig;
import com.primihub.biz.constant.RemoteConstant;
import com.primihub.biz.convert.DataTaskConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.base.DataPirKeyQuery;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DataPirCopyReq;
import com.primihub.biz.entity.data.req.DataPirReq;
import com.primihub.biz.entity.data.req.DataPirTaskReq;
import com.primihub.biz.entity.data.vo.DataPirTaskDetailVo;
import com.primihub.biz.entity.data.vo.DataPirTaskVo;
import com.primihub.biz.entity.data.vo.RemoteRespVo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.repository.primarydb.data.DataCorePrimarydbRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.primarydb.data.RecordPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataCoreRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import com.primihub.biz.repository.secondarydb.data.RecordRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
    @Qualifier("recordRepository")
    @Autowired
    private RecordRepository recordRepository;
    @Autowired
    private DataPsiService dataPsiRepository;
    @Autowired
    private SysOrganSecondarydbRepository organSecondaryDbRepository;
    @Autowired
    private ThreadPoolConfig threadPoolConfig;
    @Autowired
    private RemoteClient remoteClient;
    @Qualifier("dataCoreRepository")
    @Autowired
    private DataCoreRepository dataCoreRepository;
    @Qualifier("dataCorePrimarydbRepository")
    @Autowired
    private DataCorePrimarydbRepository dataCorePrimarydbRepository;
    @Autowired
    private ExamService examService;
    @Autowired
    private RecordPrRepository recordPrRepository;

    public String getResultFilePath(String taskId, String taskDate) {
        return new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(taskDate).append("/").append(taskId).append(".csv").toString();
    }

    public BaseResultEntity pirSubmitTask(DataPirReq req, String pirParam) {
        BaseResultEntity dataResource = otherBusinessesService.getDataResource(req.getResourceId());
        if (dataResource.getCode() != 0) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "资源查询失败");
        }
        Map<String, Object> pirDataResource = (LinkedHashMap) dataResource.getResult();
        int available = Integer.parseInt(pirDataResource.getOrDefault("available", "1").toString());
        if (available == 1) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "资源不可用");
        }
        // dataResource columnName list
        String resourceColumnNames = pirDataResource.getOrDefault("resourceColumnNameList", "").toString();
        if (StringUtils.isBlank(resourceColumnNames)) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "获取资源字段列表失败");
        }
        String[] resourceColumnNameArray = resourceColumnNames.split(",");
        if (resourceColumnNameArray.length == 0) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "获取资源字段列表为空");
        }

        String[] queryColumnNames = {resourceColumnNameArray[0]};
        // convert pirparam to query array
        List<DataPirKeyQuery> dataPirKeyQueries = convertPirParamToQueryArray(pirParam, queryColumnNames);

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
        dataAsyncService.pirGrpcTask(dataTask, dataPirTask, resourceColumnNames, dataPirKeyQueries);
        Map<String, Object> map = new HashMap<>();
        map.put("taskId", dataTask.getTaskId());
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

    private static List<DataPirKeyQuery> convertPirParamToQueryArray(String[] pirParamArray, String[] resourceColumnNameArray) {
        DataPirKeyQuery dataPirKeyQuery = new DataPirKeyQuery();
        dataPirKeyQuery.setKey(resourceColumnNameArray);
        String[] array = pirParamArray;
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
            return BaseResultEntity.success(new PageDataEntity(0, req.getPageSize(), req.getPageNo(), new ArrayList()));
        }
        Integer tolal = dataTaskRepository.selectDataPirTaskCount(req);
        Map<String, LinkedHashMap<String, Object>> resourceMap = new HashMap<>();
        List<String> ids = dataPirTaskVos.stream().map(DataPirTaskVo::getResourceId).collect(Collectors.toList());
        BaseResultEntity baseResult = otherBusinessesService.getResourceListById(ids);
        if (baseResult.getCode() == 0) {
            List<LinkedHashMap<String, Object>> voList = (List<LinkedHashMap<String, Object>>) baseResult.getResult();
            if (voList != null && voList.size() != 0) {
                resourceMap.putAll(voList.stream().collect(Collectors.toMap(data -> data.get("resourceId").toString(), Function.identity())));
            }
        }
        for (DataPirTaskVo dataPirTaskVo : dataPirTaskVos) {
            if (resourceMap.containsKey(dataPirTaskVo.getResourceId())) {
                DataTaskConvert.dataPirTaskPoConvertDataPirTaskVo(dataPirTaskVo, resourceMap.get(dataPirTaskVo.getResourceId()));
            }
        }
        return BaseResultEntity.success(new PageDataEntity(tolal, req.getPageSize(), req.getPageNo(), dataPirTaskVos));
    }

    public BaseResultEntity getPirTaskDetail(Long taskId) {
        DataPirTask task = dataTaskRepository.selectPirTaskById(taskId);
        if (task == null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "未查询到任务信息");
        }
        DataTask dataTask = dataTaskRepository.selectDataTaskByTaskId(taskId);
        if (dataTask == null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "未查询到任务详情");
        }
        DataPirTaskDetailVo vo = new DataPirTaskDetailVo();
        List<LinkedHashMap<String, Object>> list = null;
        if (StringUtils.isNotEmpty(dataTask.getTaskResultPath())) {
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


    /**
     * pir phase1 #1
     *
     * @param req
     * @return
     */
    public BaseResultEntity submitPirPhase1(DataPirCopyReq req) {
        PsiRecord psiRecord = recordRepository.selectPsiRecordByRecordId(req.getPsiRecordId());
        Long psiTaskId = psiRecord.getPsiTaskId();

        // 获取目标值
        DataPsiTask task = dataPsiRepository.selectPsiTaskById(psiTaskId);
        List<LinkedHashMap<String, Object>> list = null;
        if (org.apache.commons.lang.StringUtils.isNotEmpty(task.getFilePath())) {
            list = FileUtil.getAllCsvData(task.getFilePath());
            Set<String> phoneSet = list.stream().map(map -> String.valueOf(map.getOrDefault("phone", "")))
                    .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
            req.setTargetValueSet(phoneSet);
        } else {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "psi结果为空");
        }

        String targetOrganId = UUID.randomUUID().toString();
        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(targetOrganId);
        if (CollectionUtils.isEmpty(sysOrgans)) {
            log.info("查询机构ID: [{}] 失败，未查询到结果", targetOrganId);
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "organ");
        }
        for (SysOrgan organ : sysOrgans) {
            return otherBusinessesService.syncGatewayApiData(req, organ.getOrganGateway() + "/share/shareData/processPirPhase1", organ.getPublicKey());
        }
        return null;
    }

    /**
     * 协作方
     * pir phase1 #2
     *
     * @param req
     * @return
     */
    public BaseResultEntity processPirPhase1(DataPirCopyReq req) {
        // idNum
        Set<String> targetValueSet = req.getTargetValueSet();
        Set<DataCore> dataCores = dataCoreRepository.selectDataCoreFromIdNum(targetValueSet);
        Map<String, DataCore> idNumDataCoreMap = dataCores.stream().collect(Collectors.toMap(DataCore::getIdNum, Function.identity()));
        String scoreModelType = req.getScoreModelType();

        for (Map.Entry<String, DataCore> entry : idNumDataCoreMap.entrySet()) {
            if (entry.getValue().getScore1() != null) {
                continue;
            }
            RemoteRespVo respVo = remoteClient.queryFromRemote(entry.getValue().getPhoneNum());
            if (respVo != null && ("Y").equals(respVo.getHead().getResult())) {
                entry.getValue().setScore1(Double.valueOf(respVo.getRespBody().getTruth_score()));
                dataCorePrimarydbRepository.saveDataCore(entry.getValue());
            }
        }

        // 成功后开始生成文件
        String jsonArrayStr = JSON.toJSONString(dataCores);
        List<Map> maps = JSONObject.parseArray(jsonArrayStr, Map.class);
        // 生成数据源
        DataResource dataResource = examService.generateTargetResource(maps);

        req.setTargetResourceId(dataResource.getResourceFusionId());

        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(req.getOriginOrganId());
        if (CollectionUtils.isEmpty(sysOrgans)) {
            log.info("查询机构ID: [{}] 失败，未查询到结果", req.getOriginOrganId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "organ");
        }
        for (SysOrgan organ : sysOrgans) {
            return otherBusinessesService.syncGatewayApiData(req, organ.getOrganGateway() + "/share/shareData/submitPirPhase2", organ.getPublicKey());
        }
        return null;
    }

    /**
     * pir phase2 #3
     *
     * @param req
     * @return
     */
    public BaseResultEntity submitPirPhase2(DataPirReq param, DataPirCopyReq req) {
        BaseResultEntity dataResource = otherBusinessesService.getDataResource(param.getResourceId());
        if (dataResource.getCode() != 0) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "资源查询失败");
        }
        Map<String, Object> pirDataResource = (LinkedHashMap) dataResource.getResult();
        int available = Integer.parseInt(pirDataResource.getOrDefault("available", "1").toString());
        if (available == 1) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "资源不可用");
        }
        // dataResource columnName list
        String resourceColumnNames = pirDataResource.getOrDefault("resourceColumnNameList", "").toString();
        if (StringUtils.isBlank(resourceColumnNames)) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "获取资源字段列表失败");
        }
        String[] resourceColumnNameArray = resourceColumnNames.split(",");
        if (resourceColumnNameArray.length == 0) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "获取资源字段列表为空");
        }
        boolean containedTargetFieldFlag = Arrays.asList(resourceColumnNameArray).contains("idNum");
        if (!containedTargetFieldFlag) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "获取资源字段列表不包含目标字段");
        }

        String[] targetValueArray = req.getTargetValueSet().toArray(new String[0]);
        String[] queryColumnNames = {RemoteConstant.INPUT_FIELD_NAME};
        List<DataPirKeyQuery> dataPirKeyQueries = convertPirParamToQueryArray(targetValueArray, queryColumnNames);

        DataTask dataTask = new DataTask();
        dataTask.setTaskIdName(Long.toString(SnowflakeId.getInstance().nextId()));
        dataTask.setTaskName(req.getTaskName());
        dataTask.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        dataTask.setTaskType(TaskTypeEnum.PIR.getTaskType());
        dataTask.setTaskStartTime(System.currentTimeMillis());
        dataTaskPrRepository.saveDataTask(dataTask);
        DataPirTask dataPirTask = new DataPirTask();
        dataPirTask.setTaskId(dataTask.getTaskId());
        // retrievalId will rent in web ,need to be readable
        dataPirTask.setRetrievalId(String.join(",", req.getTargetValueSet()));
        dataPirTask.setProviderOrganName(pirDataResource.get("organName").toString());
        dataPirTask.setResourceName(pirDataResource.get("resourceName").toString());
        dataPirTask.setResourceId(param.getResourceId());
        dataTaskPrRepository.saveDataPirTask(dataPirTask);
        dataAsyncService.pirGrpcTask(dataTask, dataPirTask, resourceColumnNames, dataPirKeyQueries);

        PirRecord pirRecord = new PirRecord();
        pirRecord.setPirName(req.getTaskName());
        pirRecord.setPirTaskId(dataTask.getTaskId());
        pirRecord.setTaskState(dataTask.getTaskState());
        pirRecord.setStartTime(new Date());
        pirRecord.setCommitRowsNum(req.getTargetValueSet().size());

        List<LinkedHashMap<String, Object>> list = new ArrayList<>();
        if (org.apache.commons.lang.StringUtils.isNotEmpty(dataTask.getTaskResultPath())) {
            list = FileUtil.getAllCsvData(dataTask.getTaskResultPath());
        }
        pirRecord.setResultRowsNum(list.size());
        recordPrRepository.savePirRecord(pirRecord);


        Map<String, Object> map = new HashMap<>();
        map.put("taskId", dataTask.getTaskId());
        return BaseResultEntity.success(map);
    }
}
