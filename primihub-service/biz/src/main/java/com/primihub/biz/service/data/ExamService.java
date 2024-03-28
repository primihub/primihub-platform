package com.primihub.biz.service.data;


import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.convert.DataExamConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.DataExamTask;
import com.primihub.biz.entity.data.po.DataFileField;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.req.DataExamTaskReq;
import com.primihub.biz.entity.data.vo.DataExamTaskVo;
import com.primihub.biz.entity.data.vo.DataPirTaskDetailVo;
import com.primihub.biz.entity.data.vo.DataPirTaskVo;
import com.primihub.biz.entity.data.vo.DataResourceCsvVo;
import com.primihub.biz.entity.sys.po.SysFile;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import com.primihub.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.feign.FusionResourceService;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public BaseResultEntity<PageDataEntity<DataPirTaskVo>> getExamTaskList(DataExamTaskReq req) {
        List<DataExamTaskVo> dataExamTaskVos = dataTaskRepository.selectDataExamTaskPage(req);
        if (dataExamTaskVos.isEmpty()) {
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),Collections.emptyList()));
        }
        Integer total = dataTaskRepository.selectDataExamTaskCount(req);
        return BaseResultEntity.success(new PageDataEntity(total,req.getPageSize(),req.getPageNo(),dataExamTaskVos));
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
        SysFile sysFile = fileRepository.selectSysFileByFileId(Optional.ofNullable(dataResource.getFileId()).orElse(0L));
        if (sysFile == null) {
            log.info("预处理的资源查询为空 sysFileId: [{}]", dataResource.getFileId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL, "sysFile");
        }
        BaseResultEntity<Set<String>> result = getDataResourceCsvTargetFieldList(sysFile);
        if (!BaseResultEntity.isSuccess(result)) {
            log.info("文件解析失败 sysFileId: [{}]", dataResource.getFileId());
            return result;
        }
        Set<String> targetFieldValueSet = result.getResult();

        DataExamReq req = DataExamConvert.convertPoToReq(po);
        req.setFieldValueSet(targetFieldValueSet);
        // 发送给对方机构
        return sendExamTask(param);
    }

    private  BaseResultEntity<Set<String>> getDataResourceCsvTargetFieldList(SysFile sysFile) {
        try {
            List<String> fileContent = FileUtil.getFileContent(sysFile.getFileUrl(), 1);
            if (fileContent==null|| fileContent.isEmpty()) {
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
            if (!Arrays.asList(headers).contains(DataConstant.INPUT_FIELD_NAME)) {
                log.info("该资源字段不包括目的字段: [{}]", DataConstant.INPUT_FIELD_NAME);
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL, DataConstant.INPUT_FIELD_NAME);
            }
            List<LinkedHashMap<String, Object>> csvData = FileUtil.getCsvData(sysFile.getFileUrl(), Math.toIntExact(sysFile.getFileSize()));
            // stream.filter 结果为ture的元素留下
            Set<String> targetFieldValueSet = csvData.stream().map(stringObjectLinkedHashMap -> stringObjectLinkedHashMap.getOrDefault(DataConstant.INPUT_FIELD_NAME, StringUtils.EMPTY)).map(String::valueOf).filter(StringUtils::isNotBlank).collect(Collectors.toSet());
            return BaseResultEntity.success(targetFieldValueSet);
        }catch (Exception e){
            log.info("fileUrl:[{}] Exception Message : {}",sysFile.getFileUrl(),e);
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL,"请检查文件编码格式");
        }
    }

    private BaseResultEntity sendExamTask(DataExamReq param) {
        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(param.getTargetOrganId());
        if (CollectionUtils.isEmpty(sysOrgans)) {
            log.info("查询机构ID: [{}] 失败，未查询到结果", param.getTargetOrganId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"organ");
        }

        for (SysOrgan organ : sysOrgans) {
            return otherBusinessesService.syncGatewayApiData(param, organ.getOrganGateway() + "/data/exam/processExamTask", organ.getPublicKey());
        }
        return null;
    }

    private DataExamTask saveExamTaskReq(DataExamReq param) {
        DataExamTask task = DataExamConvert.convertReqToPo(param, organConfiguration.getSysLocalOrganInfo());
        dataTaskPrRepository.saveDataExamTask(task);
        return task;
    }

    public BaseResultEntity processExamTask(DataExamReq req) {
        req.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        sendEndExamTask(req);

        // 检查源数据集是否存在
        BaseResultEntity fusionResult = fusionResourceService.getDataResource(req.getResourceId(), req.getOriginOrganId());
        if (fusionResult.getCode() != 0 || fusionResult.getResult() == null ) {
            log.info("未找到预处理源数据 resourceId: [{}]", req.getResourceId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"resourceId");
        }

        // 生成任务实体记录
        DataExamTask task = new DataExamTask();
        task.setTaskId(req.getTaskId());
        task.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        task.setTaskName(req.getTaskName());
        task.setTargetOrganId(req.getTargetOrganId());
        task.setOriginResourceId(req.getResourceId());
        task.setOriginOrganId(req.getOriginOrganId());
        dataTaskPrRepository.saveDataExamTask(task);
        req.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());

        sendEndExamTask(req);

        Map<String, Object> otherDataResource = (LinkedHashMap) fusionResult.getResult();
        // futureTask
        startFutureExamTask(otherDataResource, req);

        return BaseResultEntity.success();
    }

    private void startFutureExamTask(Map<String, Object> otherDataResource, DataExamReq req) {
        FutureTask<Object> task = new FutureTask<Object>(() -> {
            System.out.println("执行任务");

            // 先返回执行
            // 生成新的数据集
            // todo 预处理流程
            String targetResourceId = mockExam(otherDataResource);


            // 结束任务
            sendEndExamTask(req);

            return new Object();
        });
        primaryThreadPool.submit(task);
    }

    private BaseResultEntity sendEndExamTask(DataExamReq req) {
        List<SysOrgan> sysOrgans = organSecondaryDbRepository.selectOrganByOrganId(req.getOriginOrganId());
        if (CollectionUtils.isEmpty(sysOrgans)) {
            log.info("查询机构ID: [{}] 失败，未查询到结果", req.getOriginOrganId());
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"organ");
        }

        for (SysOrgan organ : sysOrgans) {
            return otherBusinessesService.syncGatewayApiData(req, organ.getOrganGateway() + "/data/exam/finishExamTask", organ.getPublicKey());
        }
        return null;
    }

    private String mockExam(Map<String, Object> otherDataResource) {
        return "mockResourceId";
    }

    private BaseResultEntity getTargetResource(String resourceId, String organId) {
        BaseResultEntity fusionResult = fusionResourceService.getDataResource(resourceId, organId);
        if (fusionResult.getCode() != 0 || fusionResult.getResult() == null ) {
            log.info("未找到预处理源数据 resourceId: [{}]", resourceId);
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"resourceId");
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
        if (task==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到任务信息");
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
        if (!fieldNameSet.contains(DataConstant.INPUT_FIELD_NAME)) {
            log.info("该数据资源缺乏目的字段, [{}]", DataConstant.INPUT_FIELD_NAME);
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, DataConstant.INPUT_FIELD_NAME);
        }

        req.setTaskId(String.valueOf(SnowflakeId.getInstance().nextId()));
        DataExamTask po = DataExamConvert.convertReqToPo(req, organConfiguration.getSysLocalOrganInfo());
        po.setTaskState(TaskStateEnum.INIT.getStateType());
        dataTaskPrRepository.saveDataExamTask(po);
        return BaseResultEntity.success();
    }

    private void checkCSVContainIdNum(DataExamReq req) {




    }
}
