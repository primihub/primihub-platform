package com.primihub.biz.service.data.exam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.constant.RemoteConstant;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.convert.DataResourceConvert;
import com.primihub.biz.entity.base.BaseFunctionHandleEntity;
import com.primihub.biz.entity.base.BaseFunctionHandleEnum;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.DataFileField;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.po.DataResourceTag;
import com.primihub.biz.entity.data.po.DataSource;
import com.primihub.biz.entity.data.po.lpy.DataCore;
import com.primihub.biz.entity.data.po.lpy.DataMap;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.vo.DataFileFieldVo;
import com.primihub.biz.entity.data.vo.DataResourceCsvVo;
import com.primihub.biz.entity.data.vo.lpy.ExamResultVo;
import com.primihub.biz.entity.sys.po.SysFile;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.repository.primarydb.data.DataCorePrimarydbRepository;
import com.primihub.biz.repository.primarydb.data.DataMapPrimarydbRepository;
import com.primihub.biz.repository.primarydb.data.DataResourcePrRepository;
import com.primihub.biz.repository.primarydb.sys.SysFilePrimarydbRepository;
import com.primihub.biz.repository.secondarydb.data.DataCoreRepository;
import com.primihub.biz.repository.secondarydb.data.DataMapRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.PhoneClientService;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.data.OtherBusinessesService;
import com.primihub.biz.service.feign.FusionResourceService;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.crypt.DateUtil;
import com.primihub.sdk.task.param.TaskParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Service
@Slf4j
public class ExamExecuteIdNum implements ExamExecute {
    @Autowired
    private DataCoreRepository dataCoreRepository;
    @Autowired
    private PhoneClientService phoneClientService;
    @Autowired
    private DataCorePrimarydbRepository dataCorePrimarydbRepository;
    @Autowired
    private DataMapPrimarydbRepository dataMapPrimarydbRepository;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private SysFilePrimarydbRepository sysFilePrimarydbRepository;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private DataResourcePrRepository dataResourcePrRepository;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private SingleTaskChannel singleTaskChannel;
    @Autowired
    private SysOrganSecondarydbRepository organSecondaryDbRepository;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private DataMapRepository dataMapRepository;

    /*
    rawSet
    oldSet, noOldSet
    oldSet, phoneSet, noPhoneSet
    oldSet, phoneSet, waterSet, noSet
     */
    @Override
    public void processExam(DataExamReq req) {
        log.info("process exam future task : idNum");

        // rawSet
        Set<String> rawSet = req.getFieldValueSet();

        // 已经存在的数据，这个时候还没有模型分，所以用DataMap查询
        /*
        rawSet
        oldMapSet, noMapSet
        oldMapSet, newMapSet, stillNoMapSet
        oldMapSet, newMapSet, waterMapSet , lastNoMapSet
         */
        Set<DataMap> dataMapSet = dataMapRepository.selectDataMap(rawSet);
        Set<String> oldMapSet = dataMapSet.stream().map(DataMap::getIdNum).collect(Collectors.toSet());
        Collection<String> noMapSet = CollectionUtils.subtract(rawSet, oldMapSet);

        // 旧数据中不存在数据，查询api
        Map<String, String> newMapSet = phoneClientService.findSM3PhoneForSM3IdNum(new HashSet<>(noMapSet));
        // newMap保存一下
        Set<DataMap> newMapDataSet = newMapSet.entrySet().stream().map(entry -> {
            return new DataMap(entry.getKey(), entry.getValue());
        }).collect(Collectors.toSet());
        dataMapPrimarydbRepository.saveDataMapList(newMapDataSet);
        Set<String> newMapMapSet = newMapDataSet.stream().map(DataMap::getIdNum).collect(Collectors.toSet());

        // noMap过滤
        Collection<String> stillNoMapSet = CollectionUtils.subtract(noMapSet, newMapMapSet);

        List<String> noPhoneList = new ArrayList<>(stillNoMapSet);
        int halfSize = noPhoneList.size() / 2;
        Set<String> waterSet = new HashSet<>();

        // water
        Random random = new Random();
        for (int i = 0; i < halfSize; i++) {
            int randomIndex = random.nextInt(noPhoneList.size());
            String s = noPhoneList.get(randomIndex);
            waterSet.add(s);
            noPhoneList.remove(randomIndex);
        }

        Set<DataMap> waterMapSet = waterSet.stream().map(idNum -> {
            DataMap dataMap = new DataMap(idNum, RemoteConstant.UNDEFILED);
            return dataMap;
        }).collect(Collectors.toSet());
        dataMapPrimarydbRepository.saveDataMapList(waterMapSet);

        Set<ExamResultVo> allDataCoreSet = dataMapRepository.selectIdNumExamResultVo(rawSet);

        String jsonArrayStr = JSON.toJSONString(allDataCoreSet);
        List<Map> maps = JSONObject.parseArray(jsonArrayStr, Map.class);
        // 生成数据源
        String resourceName = new StringBuffer().append("预处理生成资源").append(SysConstant.HYPHEN_DELIMITER).append(req.getTaskId()).toString();
        DataResource dataResource = generateTargetResource(maps, resourceName);
        if (dataResource == null) {
            req.setTaskState(TaskStateEnum.FAIL.getStateType());
            sendEndExamTask(req);
            log.info("====================== FAIL");
        } else {
            req.setTaskState(TaskStateEnum.SUCCESS.getStateType());
            req.setTargetResourceId(dataResource.getResourceFusionId());
            sendEndExamTask(req);
            log.info("====================== SUCCESS");
        }
    }

    //    private DataResource generateTargetResource(Map returnMap) {
    public DataResource generateTargetResource(List<Map> metaData, String resourceName) {
        log.info("开始生成数据源===========================");

        SysFile sysFile = new SysFile();
        sysFile.setFileSource(1);
        sysFile.setFileSuffix("csv");
        sysFile.setFileName(UUID.randomUUID().toString());
        Date date = new Date();
        StringBuilder sb = new StringBuilder().append(baseConfiguration.getUploadUrlDirPrefix()).append(1).append("/").append(DateUtil.formatDate(date, DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat())).append("/");
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
            po.setResourceName(resourceName);
            po.setResourceDesc(resourceName);
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
            log.info("save DataResource Exception：{}", e.getMessage());
            log.error(e.getMessage(), e);
            return null;
        }
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
}
