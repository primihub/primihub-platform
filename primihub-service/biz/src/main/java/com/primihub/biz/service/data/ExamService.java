package com.primihub.biz.service.data;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.constant.RemoteConstant;
import com.primihub.biz.convert.DataExamConvert;
import com.primihub.biz.convert.DataResourceConvert;
import com.primihub.biz.entity.base.*;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.req.DataExamTaskReq;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.entity.sys.po.SysFile;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.repository.primarydb.data.DataCorePrimarydbRepository;
import com.primihub.biz.repository.primarydb.data.DataResourcePrRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.primarydb.sys.SysFilePrimarydbRepository;
import com.primihub.biz.repository.secondarydb.data.DataCoreRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import com.primihub.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.PhoneClientService;
import com.primihub.biz.service.data.db.AbstractDataDBService;
import com.primihub.biz.service.feign.FusionResourceService;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.crypt.DateUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import com.primihub.sdk.task.param.TaskParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExamService {

    private final Lock lock = new ReentrantLock();

    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;
    @Autowired
    private DataTaskRepository dataTaskRepository;
    @Autowired
    private SysOrganSecondarydbRepository organSecondaryDbRepository;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private ThreadPoolTaskExecutor primaryThreadPool;
    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private SysFileSecondarydbRepository fileRepository;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private SysFilePrimarydbRepository sysFilePrimarydbRepository;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private DataResourcePrRepository dataResourcePrRepository;
    @Autowired
    private SingleTaskChannel singleTaskChannel;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private DataCoreService dataCoreService;
    @Autowired
    private PhoneClientService phoneClientService;
    @Autowired
    private DataCorePrimarydbRepository dataCorePrimarydbRepository;
    @Autowired
    private DataCoreRepository dataCoreRepository;

    public BaseResultEntity<PageDataEntity<DataPirTaskVo>> getExamTaskList(DataExamTaskReq req) {
        List<DataExamTaskVo> dataExamTaskVos = dataTaskRepository.selectDataExamTaskPage(req);
        if (dataExamTaskVos.isEmpty()) {
            return BaseResultEntity.success(new PageDataEntity(0, req.getPageSize(), req.getPageNo(), Collections.emptyList()));
        }
        Integer total = dataTaskRepository.selectDataExamTaskCount(req);
        return BaseResultEntity.success(new PageDataEntity(total, req.getPageSize(), req.getPageNo(), dataExamTaskVos));
    }

    public BaseResultEntity submitExamTask(DataExamReq param) {
        // getTargetData
        DataExamTask po = dataTaskRepository.selectDataExamByTaskId(param.getTaskId());
        String resourceId = po.getOriginResourceId();
        DataResource dataResource = dataResourceRepository.queryDataResourceByResourceFusionId(resourceId);
        if (dataResource == null) {
            log.info("预处理的资源查询为空 resourceId: [{}]", resourceId);
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "dataResource");
        }
        BaseResultEntity<Set<String>> result;
        // 文件类型
        if (dataResource.getResourceSource() == 1) {
            SysFile sysFile = fileRepository.selectSysFileByFileId(Optional.ofNullable(dataResource.getFileId()).orElse(0L));
            if (sysFile == null) {
                log.info("预处理的资源查询为空 sysFileId: [{}]", dataResource.getFileId());
                return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "sysFile");
            }
            result = getTargetFieldValueListFromSysFile(sysFile);
            if (!BaseResultEntity.isSuccess(result)) {
                log.info("文件解析失败 sysFileId: [{}]", dataResource.getFileId());
                return result;
            }
        }
        // db类型
        else if (dataResource.getResourceSource() == 2) {
            DataSource dataSource = dataResourceRepository.queryDataSourceById(dataResource.getDbId());
            result = getTargetFieldValueListFromDb(dataSource);
            if (!BaseResultEntity.isSuccess(result)) {
                log.info("数据库表解析失败 dbId: [{}]", dataResource.getDbId());
                return result;
            }
        } else {
            log.info("预处理的资源类型错误 resourceId: [{}]", resourceId);
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "dataResource");
        }

        Set<String> targetFieldValueSet = result.getResult();
        DataExamReq req = DataExamConvert.convertPoToReq(po);
        req.setFieldValueSet(targetFieldValueSet);
        // 发送给对方机构
        return sendExamTask(req);
    }

    private BaseResultEntity<Set<String>> getTargetFieldValueListFromDb(DataSource dataSource) {
        AbstractDataDBService abstractDataDBService = dataSourceService.getDBServiceImpl(dataSource.getDbType());

        if (abstractDataDBService != null) {
            BaseResultEntity result = abstractDataDBService.dataSourceTableDetails(dataSource);
            if (result == null || result.getCode() != 0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL, "解析数据库表失败");
            }
            Map<String, Object> resultMap = (Map<String, Object>) result.getResult();
            List<Map<String, Object>> rowMap = (List<Map<String, Object>>) resultMap.getOrDefault("dataList", Collections.emptyList());
            if (CollectionUtils.isEmpty(rowMap)) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL, "数据库表中没有记录");
            }
            Set<String> targetFieldValueSet = rowMap.stream()
                    .map(map -> (String) map.getOrDefault(RemoteConstant.INPUT_FIELD_NAME, StringUtils.EMPTY))
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.toSet());
            return BaseResultEntity.success(targetFieldValueSet);
        } else {
            return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL, "您选择的数据库暂不支持");
        }
    }

    private BaseResultEntity<Set<String>> getTargetFieldValueListFromSysFile(SysFile sysFile) {
        try {
            List<String> fileContent = FileUtil.getFileContent(sysFile.getFileUrl(), 1);
            if (fileContent == null || fileContent.isEmpty()) {
                log.info("csv文件解析失败");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL);
            }
            String headersStr = fileContent.get(0);
            if (StringUtils.isBlank(headersStr)) {
                log.info("csv文件解析失败: 文件字段为空");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL);
            }
            String[] headers = headersStr.split(",");
            if (headers[0].startsWith(DataConstant.UTF8_BOM)) {
                headers[0] = headers[0].substring(1);
            }
            if (!Arrays.asList(headers).contains(RemoteConstant.INPUT_FIELD_NAME)) {
                log.info("该资源字段不包括目的字段: [{}]", RemoteConstant.INPUT_FIELD_NAME);
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL, RemoteConstant.INPUT_FIELD_NAME);
            }
            List<LinkedHashMap<String, Object>> csvData = FileUtil.getCsvData(sysFile.getFileUrl(), Math.toIntExact(sysFile.getFileSize()));
            // stream.filter 结果为ture的元素留下
            Set<String> targetFieldValueSet = csvData.stream().map(stringObjectLinkedHashMap -> stringObjectLinkedHashMap.getOrDefault(RemoteConstant.INPUT_FIELD_NAME, StringUtils.EMPTY)).map(String::valueOf).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
            return BaseResultEntity.success(targetFieldValueSet);
        } catch (Exception e) {
            log.info("fileUrl:[{}] Exception Message : {}", sysFile.getFileUrl(), e);
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL, "请检查文件编码格式");
        }
    }

    private BaseResultEntity sendExamTask(DataExamReq param) {
        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(param.getTargetOrganId());
        if (CollectionUtils.isEmpty(sysOrgans)) {
            log.info("查询机构ID: [{}] 失败，未查询到结果", param.getTargetOrganId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "organ");
        }

        for (SysOrgan organ : sysOrgans) {
            return otherBusinessesService.syncGatewayApiData(param, organ.getOrganGateway() + "/share/shareData/processExamTask", organ.getPublicKey());
        }
        return null;
    }

    public BaseResultEntity processExamTask(DataExamReq req) {
        if (CollectionUtils.isEmpty(req.getFieldValueSet())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "fieldValue");
        }

        req.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        sendEndExamTask(req);

        // futureTask
        startFutureExamTask(req);

        return BaseResultEntity.success();
    }

    //    private DataResource generateTargetResource(Map returnMap) {
    public DataResource generateTargetResource(List<Map> metaData) {
        log.info("开始生成数据源===========================");

        SysFile sysFile = new SysFile();
        sysFile.setFileSource(1);
        sysFile.setFileSuffix("csv");
        sysFile.setFileName(UUID.randomUUID().toString());
        Date date = new Date();
        StringBuilder sb = new StringBuilder().append(baseConfiguration.getUploadUrlDirPrefix()).append(1)
                .append("/").append(DateUtil.formatDate(date, DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat())).append("/");
        sysFile.setFileArea("local");
        sysFile.setFileSize(0L);
        sysFile.setFileCurrentSize(0L);
        sysFile.setIsDel(0);

        try {
            File tempFile = new File(sb.toString());
            if (!tempFile.exists()) {
                tempFile.mkdirs();
            }
            FileUtil.convertToCsv(metaData, sb.append(sysFile.getFileName()).append(".").append(sysFile.getFileSuffix()).toString());
            log.info("写入csv文件===========================");
            sysFile.setFileUrl(sb.toString());
        } catch (IOException e) {
            log.error("upload", e);
            return null;
//            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"写硬盘失败");
        }

        log.info("sysFile: {}", JSON.toJSONString(sysFile));
        sysFilePrimarydbRepository.insertSysFile(sysFile);
        log.info("sysFile: {}", JSON.toJSONString(sysFile));

        // resourceFilePreview
        BaseResultEntity resultEntity = dataResourceService.getDataResourceCsvVo(sysFile);
        log.info("resultEntity: {}", JSON.toJSONString(resultEntity));
        DataResourceCsvVo csvVo = (DataResourceCsvVo) resultEntity.getResult();

        try {
            DataResource po = new DataResource();
            po.setResourceName("预处理生成资源");
            po.setResourceDesc("预处理生成资源");
            po.setResourceAuthType(1);  // 公开
            po.setResourceSource(1);    // 文件
//            po.setUserId();
//            po.setOrganId();
            po.setFileId(sysFile.getFileId());
            po.setFileSize(0);
            po.setFileSuffix("");
            po.setFileColumns(0);
            po.setFileRows(0);
            po.setFileHandleStatus(0);
            po.setResourceNum(0);
            po.setDbId(0L);
            po.setUrl("");
//            po.setPublicOrganId();    // 可见机构列表
            po.setResourceState(0);
            List<DataFileFieldVo> fieldList = csvVo.getFieldList();
            BaseResultEntity handleDataResourceFileResult = null;
            DataSource dataSource = null;
            po.setFileId(sysFile.getFileId());
            po.setFileSize(sysFile.getFileSize().intValue());
            po.setFileSuffix(sysFile.getFileSuffix());
            po.setFileColumns(0);
            po.setFileRows(0);
            po.setFileHandleStatus(0);
            po.setResourceNum(0);
            po.setDbId(0L);
            po.setUrl(sysFile.getFileUrl());
//            po.setPublicOrganId();
            po.setResourceState(0);

            handleDataResourceFileResult = dataResourceService.handleDataResourceFile(po, sysFile.getFileUrl());
            log.info("{}", JSON.toJSONString(handleDataResourceFileResult));
            if (handleDataResourceFileResult.getCode() != 0) {
                log.info("{}", JSON.toJSONString(handleDataResourceFileResult));
                // todo 错误处理
//                return handleDataResourceFileResult;
                return null;
            }

            SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
            if (sysLocalOrganInfo != null && sysLocalOrganInfo.getOrganId() != null && !"".equals(sysLocalOrganInfo.getOrganId().trim())) {
                po.setResourceFusionId(organConfiguration.generateUniqueCode());
            }
            List<DataFileField> dataFileFieldList = new ArrayList<>();
            for (DataFileFieldVo field : fieldList) {
                dataFileFieldList.add(DataResourceConvert.DataFileFieldVoConvertPo(field, 0L, po.getResourceId()));
            }
            TaskParam taskParam = dataResourceService.resourceSynGRPCDataSet(dataSource, po, dataFileFieldList);
            log.info("{}", JSON.toJSONString(taskParam));
            if (!taskParam.getSuccess()) {
                log.info("{}", JSON.toJSONString(taskParam));
                // todo 错误处理
//                return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"无法将资源注册到数据集中:"+taskParam.getError());
                return null;
            }
            if (dataSource != null) {
                dataResourcePrRepository.saveSource(dataSource);
                po.setDbId(dataSource.getId());
            }
            dataResourcePrRepository.saveResource(po);
            for (DataFileField field : dataFileFieldList) {
                field.setResourceId(po.getResourceId());
            }
            dataResourcePrRepository.saveResourceFileFieldBatch(dataFileFieldList);
            List<String> tags = new ArrayList<String>() {
                {
                    add("examine");
                }
            };
            for (String tagName : tags) {
                DataResourceTag dataResourceTag = new DataResourceTag(tagName);
                dataResourcePrRepository.saveResourceTag(dataResourceTag);
                dataResourcePrRepository.saveResourceTagRelation(dataResourceTag.getTagId(), po.getResourceId());
            }
            log.info("存入数据库成功======================");
            fusionResourceService.saveResource(organConfiguration.getSysLocalOrganId(), dataResourceService.findCopyResourceList(po.getResourceId(), po.getResourceId()));
            singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SINGLE_DATA_FUSION_RESOURCE_TASK.getHandleType(), po))).build());

            return po;
        } catch (Exception e) {
            // todo
            log.info("save DataResource Exception：{}", e.getMessage());
            e.printStackTrace();
//            return BaseResultEntity.failure(BaseResultEnum.FAILURE);
            return null;
        }
    }

  /*  private List<Map<String, Object>> getDataFromCMCCSource(String cmccScoreUrl, List<Map<String, Object>> resultList) {
        *//*HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<HashMap<String, Object>> request = new HttpEntity(new Object(), headers);
        ResponseEntity<Map> exchange = restTemplate.exchange(cmccScoreUrl, HttpMethod.POST, request, Map.class);
        return exchange.getBody();*//*
        List<String> targetPhoneNumList = resultList.stream().filter(map -> StringUtils.isNotBlank((String) map.get("phone"))).map(stringObjectMap -> (String) stringObjectMap.get("phone")).collect(Collectors.toList());

        try {
            InputStream inputStream = ResourceReader.class.getClassLoader().getResourceAsStream("mock/score.json");
            List<Map<String, Object>> list = objectMapper.readValue(inputStream, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
            });
            // 读取JSON文件内容为字符串
            // 将JSON字符串解析为List<Map>

            List<Map<String, Object>> result = list.stream().filter(map -> map.get("phone") != null && targetPhoneNumList.contains((String) (map.get("phone")))).collect(Collectors.toList());
            Map<String, Map<String, Object>> phoneScoreMap = result.stream().collect(Collectors.toMap(stringObjectMap -> (String) stringObjectMap.get("phone"), Function.identity()));
            List<String> existPhoneNumList = result.stream().filter(map -> StringUtils.isNotBlank((String) map.get("phone"))).map(stringObjectMap -> (String) stringObjectMap.get("phone")).collect(Collectors.toList());
            List<Map<String, Object>> collect = resultList.stream().filter(map -> map.get("phone") != null && existPhoneNumList.contains((String) (map.get("phone")))).collect(Collectors.toList());
            collect.forEach(map -> {
                map.put("score", phoneScoreMap.get(map.get("phone")).getOrDefault("score", 0));
            });
            return collect;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("数据处理出错");
    }*/

    /*private List<Map<String, Object>> getDataFromFirstSource(String firstUrl, Set<String> fieldValueSet) {
        *//**HttpHeaders headers = new HttpHeaders();
         //        headers.setContentType(MediaType.APPLICATION_JSON);
         //        HttpEntity<HashMap<String, Object>> request = new HttpEntity(new Object(), headers);
         //        ResponseEntity<Map> exchange = restTemplate.exchange(firstUrl, HttpMethod.POST, request, Map.class);
         //        return exchange.getBody();
         *//*

        try {
            // 读取JSON文件内容为字符串
            InputStream inputStream = ResourceReader.class.getClassLoader().getResourceAsStream("mock/data.json");
            List<Map<String, Object>> list = objectMapper.readValue(inputStream, new com.fasterxml.jackson.core.type.TypeReference<List<Map<String, Object>>>() {
            });
            List<Map<String, Object>> result = list.stream().filter(map -> map.get("code") != null && fieldValueSet.contains((String) (map.get("code")))).collect(Collectors.toList());
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("数据处理出错");
    }
*/
    private void startFutureExamTask(DataExamReq req) {
        // 进行预处理，使用异步
        FutureTask<Object> task = new FutureTask<>(() -> {
            log.info("====================== 预处理开始");
            Set<String> fieldValueSet = req.getFieldValueSet();

            // 已经存在的数据
            Set<DataCore> existentDataCoreSet = dataCoreRepository.selectExistentDataCore(fieldValueSet);
            Set<String> existentTargetValueSet = existentDataCoreSet.stream().map(DataCore::getIdNum).collect(Collectors.toSet());
            // 不存在的数据
            Set<String> nonexistentTargetValueSet = fieldValueSet.stream().filter(s -> !existentTargetValueSet.contains(s)).collect(Collectors.toSet());


            // 先过滤出存在手机号的数据
            Map<String, String> phoneMap = phoneClientService.findSM3PhoneForSM3IdNum(nonexistentTargetValueSet);
            Set<DataCore> nonexistentDataCoreSet = phoneMap.entrySet().stream().map(entry -> {
                DataCore dataCore = new DataCore();
                dataCore.setIdNum(entry.getKey());
                dataCore.setPhoneNum(entry.getValue());
                return dataCore;
            }).collect(Collectors.toSet());
            dataCorePrimarydbRepository.saveDataCoreSet(nonexistentDataCoreSet);

            Set<DataCore> allDataCoreSet = dataCoreRepository.selectExistentDataCore(fieldValueSet);
            String jsonArrayStr = JSON.toJSONString(allDataCoreSet);
            List<Map> maps = JSONObject.parseArray(jsonArrayStr, Map.class);
            // 生成数据源
            DataResource dataResource = generateTargetResource(maps);
            if (dataResource == null) {
                req.setTaskState(TaskStateEnum.FAIL.getStateType());
                sendEndExamTask(req);
                log.info("====================== FAIL");
            }
            log.info("====================== dataResource");

            req.setTaskState(TaskStateEnum.SUCCESS.getStateType());
            req.setTargetResourceId(dataResource.getResourceFusionId());
            sendEndExamTask(req);
            log.info("====================== SUCCESS");
            log.info("====================== 预处理结束");
            return null;
        });
        primaryThreadPool.submit(task);
    }

    private BaseResultEntity sendEndExamTask(DataExamReq req) {
        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(req.getOriginOrganId());
        if (CollectionUtils.isEmpty(sysOrgans)) {
            log.info("查询机构ID: [{}] 失败，未查询到结果", req.getOriginOrganId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "organ");
        }

        for (SysOrgan organ : sysOrgans) {
            return otherBusinessesService.syncGatewayApiData(req, organ.getOrganGateway() + "/share/shareData/finishExamTask", organ.getPublicKey());
        }
        return null;
    }

    private BaseResultEntity getTargetResource(String resourceId, String organId) {
        BaseResultEntity fusionResult = fusionResourceService.getDataResource(resourceId, organId);
        if (fusionResult.getCode() != 0 || fusionResult.getResult() == null) {
            log.info("未找到预处理源数据 resourceId: [{}]", resourceId);
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "resourceId");
        }
        return fusionResult;
    }

    @Transactional
    public BaseResultEntity finishExamTask(DataExamReq req) {
        try {
            lock.lock();
            updateExamTaskAfterSelect(req);
        } finally {
            lock.unlock();
        }
        return BaseResultEntity.success();
    }

    private void updateExamTaskAfterSelect(DataExamReq req) {
        DataExamTask task = dataTaskRepository.selectDataExamByTaskId(req.getTaskId());
        task.setTaskState(req.getTaskState());
        task.setTargetResourceId(req.getTargetResourceId());
        dataTaskPrRepository.updateDataExamTask(task);
    }

    public BaseResultEntity<DataPirTaskDetailVo> getExamTaskDetail(String taskId) {
        DataExamTask task = dataTaskRepository.selectDataExamByTaskId(taskId);
        if (task == null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "未查询到任务信息");
        }
        DataExamTaskVo vo = DataExamConvert.convertPoToVo(task);
        return BaseResultEntity.success(vo);
    }

    public BaseResultEntity saveExamTask(DataExamReq req) {
        // 检查是否有目标字段
        DataResource dataResourcePo = dataResourceRepository.queryDataResourceByResourceFusionId(req.getResourceId());
        if (dataResourcePo == null) {
            log.info("资源查询失败, resourceId: [{}]", req.getResourceId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "dataResource");
        }
        List<DataFileField> dataFileFields = dataResourceRepository.queryDataFileFieldByResourceId(dataResourcePo.getResourceId());
        if (CollectionUtils.isEmpty(dataFileFields)) {
            log.info("资源查询失败, resourceId: [{}]", dataResourcePo.getResourceId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "dataFileFields");
        }
        Set<String> fieldNameSet = dataFileFields.stream().map(DataFileField::getFieldName).collect(Collectors.toSet());
        if (!fieldNameSet.contains(RemoteConstant.INPUT_FIELD_NAME)) {
            log.info("该数据资源缺乏目的字段, [{}]", RemoteConstant.INPUT_FIELD_NAME);
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, RemoteConstant.INPUT_FIELD_NAME);
        }

        // 生成预处理的流水Id
        req.setTaskId(String.valueOf(SnowflakeId.getInstance().nextId()));
        // 这里没有定义 `containY`
        DataExamTask po = DataExamConvert.convertReqToPo(req, organConfiguration.getSysLocalOrganInfo(), dataResourcePo);
        po.setTaskState(TaskStateEnum.INIT.getStateType());
        dataTaskPrRepository.saveDataExamTask(po);
        return BaseResultEntity.success();
    }

    /**
     * @param req
     * @return
     */
    public BaseResultEntity examTaskList(DataExamTaskReq req) {
        List<DataExamTaskVo> dataExamTaskVos = dataTaskRepository.selectDataExamTaskList(req);
        return BaseResultEntity.success(dataExamTaskVos);
    }
}
