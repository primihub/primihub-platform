package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.convert.DataResourceConvert;
import com.primihub.biz.convert.DataSourceConvert;
import com.primihub.biz.entity.base.*;
import com.primihub.biz.entity.data.base.ResourceFileData;
import com.primihub.biz.entity.data.dataenum.DataResourceAuthType;
import com.primihub.biz.entity.data.dataenum.SourceEnum;
import com.primihub.biz.entity.data.dto.DataFusionCopyDto;
import com.primihub.biz.entity.data.dto.ModelDerivationDto;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.entity.sys.po.SysFile;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysUser;
import com.primihub.biz.repository.primarydb.data.DataResourcePrRepository;
import com.primihub.biz.repository.secondarydb.data.DataModelRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
import com.primihub.biz.service.feign.FusionResourceService;
import com.primihub.biz.service.sys.SysUserService;
import com.primihub.biz.util.DataUtil;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.crypt.SignUtil;
import com.primihub.sdk.task.TaskHelper;
import com.primihub.sdk.task.dataenum.FieldTypeEnum;
import com.primihub.sdk.task.param.TaskDataSetParam;
import com.primihub.sdk.task.param.TaskParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataResourceService {

    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private DataResourcePrRepository dataResourcePrRepository;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private SysFileSecondarydbRepository sysFileSecondarydbRepository;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private SingleTaskChannel singleTaskChannel;
    @Autowired
    private DataSourceService dataSourceService;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private DataModelRepository dataModelRepository;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private TaskHelper taskHelper;

    public BaseResultEntity getDataResourceList(DataResourceReq req, Long userId){
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userId",userId);
        paramMap.put("offset",req.getOffset());
        paramMap.put("pageSize",req.getPageSize());
        paramMap.put("resourceId",req.getResourceId());
        paramMap.put("resourceAuthType",req.getResourceAuthType());
        paramMap.put("resourceSource",req.getResourceSource());
        paramMap.put("resourceName",req.getResourceName());
        paramMap.put("tag",req.getTag());
        paramMap.put("selectTag",req.getSelectTag());
        paramMap.put("userName",req.getUserName());
        paramMap.put("derivation",req.getDerivation());
        paramMap.put("fileContainsY",req.getFileContainsY());
        List<DataResource> dataResources = dataResourceRepository.queryDataResource(paramMap);
        if (dataResources.size()==0){
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Integer count = dataResourceRepository.queryDataResourceCount(paramMap);
        List<Long> resourceIds = new ArrayList<>();
        Set<Long> userIds = new HashSet<>();
        List<DataResourceVo> voList = dataResources.stream().map(vo->{
            resourceIds.add(vo.getResourceId());
            userIds.add(vo.getUserId());
            return DataResourceConvert.dataResourcePoConvertVo(vo);
        }).collect(Collectors.toList());
        Map<Long, List<ResourceTagListVo>> resourceTagMap = dataResourceRepository.queryDataResourceListTags(resourceIds)
                .stream()
                .collect(Collectors.groupingBy(ResourceTagListVo::getResourceId));
        Map<Long, SysUser> sysUserMap = sysUserService.getSysUserMap(userIds);
        for (DataResourceVo dataResourceVo : voList) {
            SysUser sysUser = sysUserMap.get(dataResourceVo.getUserId());
            dataResourceVo.setUserName(sysUser==null?"":sysUser.getUserName());
            dataResourceVo.setTags(resourceTagMap.get(dataResourceVo.getResourceId()));
            dataResourceVo.setUrl("");
        }
        return BaseResultEntity.success(new PageDataEntity(count,req.getPageSize(),req.getPageNo(),voList));
    }

    public BaseResultEntity saveDataResource(DataResourceReq req,Long userId){
        Map<String,Object> map = new HashMap<>();
        try {
            DataResource dataResource = DataResourceConvert.dataResourceReqConvertPo(req,userId,null);
            List<DataResourceFieldReq> fieldList = req.getFieldList();
            BaseResultEntity handleDataResourceFileResult = null;
            DataSource dataSource = null;
            if (req.getResourceSource() == 1){
                SysFile sysFile = sysFileSecondarydbRepository.selectSysFileByFileId(req.getFileId());
                if (sysFile==null){
                    return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"file");
                }
                dataResource = DataResourceConvert.dataResourceReqConvertPo(req,userId,null,sysFile);
                handleDataResourceFileResult = handleDataResourceFile(dataResource, sysFile.getFileUrl());
            }else if (req.getResourceSource() == 2){
                dataSource  = DataSourceConvert.DataSourceReqConvertPo(req.getDataSource());
                handleDataResourceFileResult = handleDataResourceSource(dataResource,fieldList,dataSource);
                dataResource.setUrl(dataSource.getDbUrl());
            }
            if (handleDataResourceFileResult.getCode()!=0) {
                return handleDataResourceFileResult;
            }

            SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
            if (sysLocalOrganInfo!=null&&sysLocalOrganInfo.getOrganId()!=null&&!"".equals(sysLocalOrganInfo.getOrganId().trim())){
                dataResource.setResourceFusionId(organConfiguration.generateUniqueCode());
            }
            List<DataFileField> dataFileFieldList = new ArrayList<>();
            for (DataResourceFieldReq field : fieldList) {
                dataFileFieldList.add(DataResourceConvert.DataFileFieldReqConvertPo(field, 0L, dataResource.getResourceId()));
            }
            TaskParam taskParam = resourceSynGRPCDataSet(dataSource, dataResource, dataFileFieldList);
            if (!taskParam.getSuccess()){
                return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"无法将资源注册到数据集中:"+taskParam.getError());
            }
            if (dataSource!=null){
                dataResourcePrRepository.saveSource(dataSource);
                dataResource.setDbId(dataSource.getId());
            }
            dataResourcePrRepository.saveResource(dataResource);
            for (DataFileField field : dataFileFieldList) {
                field.setResourceId(dataResource.getResourceId());
            }
            dataResourcePrRepository.saveResourceFileFieldBatch(dataFileFieldList);
            List<String> tags = req.getTags();
            for (String tagName : tags) {
                DataResourceTag dataResourceTag = new DataResourceTag(tagName);
                dataResourcePrRepository.saveResourceTag(dataResourceTag);
                dataResourcePrRepository.saveResourceTagRelation(dataResourceTag.getTagId(),dataResource.getResourceId());
            }
            if(req.getResourceAuthType().equals(DataResourceAuthType.ASSIGN.getAuthType())&&req.getFusionOrganList()!=null&&req.getFusionOrganList().size()!=0){
                List<DataResourceVisibilityAuth> authList=new ArrayList<>();
                for(DataSourceOrganReq organ:req.getFusionOrganList()){
                    DataResourceVisibilityAuth dataResourceVisibilityAuth=new DataResourceVisibilityAuth(dataResource.getResourceId(),organ.getOrganGlobalId(),organ.getOrganName());
                    authList.add(dataResourceVisibilityAuth);
                }
                dataResourcePrRepository.saveVisibilityAuth(authList);
            }
            fusionResourceService.saveResource(organConfiguration.getSysLocalOrganId(),findCopyResourceList(dataResource.getResourceId(), dataResource.getResourceId()));
            singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SINGLE_DATA_FUSION_RESOURCE_TASK.getHandleType(),dataResource))).build());
            map.put("resourceId",dataResource.getResourceId());
            map.put("resourceFusionId",dataResource.getResourceFusionId());
            map.put("resourceName",dataResource.getResourceName());
            map.put("resourceDesc",dataResource.getResourceDesc());
        }catch (Exception e){
            log.info("save DataResource Exception：{}",e.getMessage());
            e.printStackTrace();
            return BaseResultEntity.failure(BaseResultEnum.FAILURE);
        }

        return BaseResultEntity.success(map);
    }

    public BaseResultEntity editDataResource(DataResourceReq req, Long userId) {
        DataResource dataResource = dataResourceRepository.queryDataResourceById(req.getResourceId());
        if (dataResource==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"找不到资源信息");
        }
        DataResourceConvert.editResourceReqConvertPo(req,dataResource);
        try {
            dataResourcePrRepository.editResource(dataResource);
            handleResourceTag(req.getTags(),dataResource.getResourceId());
        }catch (Exception e){
            log.info("edit DataResource Exception: {}",e.getMessage());
        }

        if(req.getResourceAuthType()!=null){
            dataResourcePrRepository.deleteVisibilityAuthByResourceId(req.getResourceId());
            if(req.getResourceAuthType().equals(DataResourceAuthType.ASSIGN.getAuthType())&&req.getFusionOrganList()!=null&&req.getFusionOrganList().size()!=0){
                List<DataResourceVisibilityAuth> authList=new ArrayList<>();
                for(DataSourceOrganReq organ:req.getFusionOrganList()){
                    DataResourceVisibilityAuth dataResourceVisibilityAuth=new DataResourceVisibilityAuth(dataResource.getResourceId(),organ.getOrganGlobalId(),organ.getOrganName());
                    authList.add(dataResourceVisibilityAuth);
                }
                dataResourcePrRepository.saveVisibilityAuth(authList);
            }
        }
        DataSource dataSource = null;
        if(dataResource.getDbId()!=null && dataResource.getDbId()!=0L){
            Long dbId = dataResource.getDbId();
            dataSource = dataResourceRepository.queryDataSourceById(dbId);
            log.info("{}-{}",dbId,JSONObject.toJSONString(dataSource));
        }
        TaskParam taskParam = resourceSynGRPCDataSet(dataSource, dataResource, dataResourceRepository.queryDataFileFieldByFileId(dataResource.getResourceId()));
        if (!taskParam.getSuccess()){
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"无法将资源注册到数据集中:"+taskParam.getError());
        }
        fusionResourceService.saveResource(organConfiguration.getSysLocalOrganId(),findCopyResourceList(dataResource.getResourceId(), dataResource.getResourceId()));
        singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SINGLE_DATA_FUSION_RESOURCE_TASK.getHandleType(),dataResource))).build());
        Map<String,Object> map = new HashMap<>();
        map.put("resourceId",dataResource.getResourceId());
        map.put("resourceName",dataResource.getResourceName());
        map.put("resourceDesc",dataResource.getResourceDesc());
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getDataResource(Long resourceId) {
        DataResource dataResource = dataResourceRepository.queryDataResourceById(resourceId);
        if (dataResource == null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        }
        List<DataResourceTag> dataResourceTags = dataResourceRepository.queryTagsByResourceId(resourceId);
        DataResourceVo dataResourceVo = DataResourceConvert.dataResourcePoConvertVo(dataResource);
        SysUser sysUser = sysUserService.getSysUserById(dataResourceVo.getUserId());
        dataResourceVo.setUserName(sysUser == null?"":sysUser.getUserName());
        dataResourceVo.setTags(dataResourceTags.stream().map(DataResourceConvert::dataResourceTagPoConvertListVo).collect(Collectors.toList()));
        List<DataFileFieldVo> dataFileFieldList = dataResourceRepository.queryDataFileFieldByFileId(dataResource.getResourceId())
                .stream().map(DataResourceConvert::DataFileFieldPoConvertVo)
                .collect(Collectors.toList());
        Map<String,Object> map = new HashMap<>();
        try {
            if (dataResource.getResourceSource() == 2){
                DataSource dataSource = dataResourceRepository.queryDataSourceById(dataResource.getDbId());
                log.info("{}-{}",dataResource.getDbId(),JSONObject.toJSONString(dataSource));
                if (dataSource!=null){
                    BaseResultEntity baseResultEntity = dataSourceService.dataSourceTableDetails(dataSource);
                    if (baseResultEntity.getCode()==0){
                        map.putAll((Map<String,Object>)baseResultEntity.getResult());
                    }
                }
            }else {
                List<LinkedHashMap<String, Object>> csvData = FileUtil.getCsvData(dataResource.getUrl(), DataConstant.READ_DATA_ROW);
                map.put("dataList",csvData);
            }
        }catch (Exception e){
            log.info("资源id:{}-文件读取失败：{}",dataResource.getResourceId(),e.getMessage());
            map.put("dataList",new ArrayList());
        }
        map.put("resource",dataResourceVo);
        map.put("fusionOrganList",dataResourceRepository.findAuthOrganByResourceId(new ArrayList(){{add(dataResource.getResourceId());}}));
        map.put("fieldList",dataFileFieldList);
        return BaseResultEntity.success(map);
    }

    public void handleResourceTag(List<String> tags,Long resourceId){
        if (tags==null||tags.size()==0){
            return;
        }
        List<Long> tagIds = dataResourceRepository.queryResourceTagRelation(resourceId);
        dataResourcePrRepository.deleteRelationResourceTag(tagIds,resourceId);
        dataResourcePrRepository.deleteResourceTag(tagIds);
        for (String tagName : tags) {
            DataResourceTag dataResourceTag = new DataResourceTag(tagName);
            dataResourcePrRepository.saveResourceTag(dataResourceTag);
            dataResourcePrRepository.saveResourceTagRelation(dataResourceTag.getTagId(),resourceId);
        }
    }

    public BaseResultEntity deleteDataResource(Long resourceId) {
        DataResource dataResource = dataResourceRepository.queryDataResourceById(resourceId);
        if (dataResource == null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"未查询到资源信息");
        }
        Integer count = dataResourceRepository.queryResourceProjectRelationCount(dataResource.getResourceFusionId());
        if (count>0){
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"该资源已关联项目");
        }
        dataResourcePrRepository.deleteResource(resourceId);
        dataResource.setIsDel(1);
        singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SINGLE_DATA_FUSION_RESOURCE_TASK.getHandleType(),dataResource))).build());
        return BaseResultEntity.success("删除资源成功");
    }


    public BaseResultEntity getDataResourceFieldPage(Long resourceId,PageReq req,Long userId) {
        Map<String,Object> map = new HashMap<>();
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("resourceId",resourceId);
        paramMap.put("offset",req.getOffset());
        paramMap.put("pageSize",req.getPageSize());
        paramMap.put("isPage",1);
        List<DataFileField> dataFileFieldList = dataResourceRepository.queryDataFileField(paramMap);
        if (dataFileFieldList.size()==0){
            map.put("isUpdate",false);
            map.put("pageData",new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }else {
            Integer count = dataResourceRepository.queryDataFileFieldCount(paramMap);
            boolean isUpdate = false;
            DataResource dataResource = dataResourceRepository.queryDataResourceById(resourceId);
            if (dataResource!=null&&dataResource.getUserId().equals(userId)) {
                isUpdate = true;
            }
            map.put("isUpdate",isUpdate);
            map.put("pageData",new PageDataEntity(count,req.getPageSize(),req.getPageNo(),dataFileFieldList.stream().map(DataResourceConvert::DataFileFieldPoConvertVo).collect(Collectors.toList())));
        }
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity updateDataResourceField(DataResourceFieldReq req, FieldTypeEnum fieldTypeEnum) {
        if (StringUtils.isNotBlank(req.getFieldAs())) {
            if (!req.getFieldAs().substring(0,1).matches(DataConstant.MATCHES)){
                return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"字段别名格式错误");
            }
        }
        DataFileField dataFileField = DataResourceConvert.DataFileFieldReqConvertPo(req, fieldTypeEnum);
        dataResourcePrRepository.updateResourceFileField(dataFileField);
        return BaseResultEntity.success();
    }

    public BaseResultEntity resourceFilePreview(Long fileId,String resourceId) {
        if(StringUtils.isNotBlank(resourceId)){
            DataResource dataResource = dataResourceRepository.queryDataResourceByResourceFusionId(resourceId);
            if (dataResource==null) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
            }
            if (dataResource.getResourceSource() == 2){
                DataSource dataSource = dataResourceRepository.queryDataSourceById(dataResource.getDbId());
                return dataSourceService.dataSourceTableDetails(dataSource);

            }
            fileId = dataResource.getFileId();
        }

        SysFile sysFile = sysFileSecondarydbRepository.selectSysFileByFileId(fileId);
        if (sysFile==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        }
        return getDataResourceCsvVo(sysFile);
    }

    public BaseResultEntity getDataResourceCsvVo(SysFile sysFile) {
        DataResourceCsvVo csvVo = null;
        try {
            List<String> fileContent = FileUtil.getFileContent(sysFile.getFileUrl(), 1);
            if (fileContent==null||fileContent.size()==0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL);
            }
            String headersStr = fileContent.get(0);
            if (StringUtils.isBlank(headersStr)) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL);
            }
            csvVo =  new DataResourceCsvVo();
            String[] headers = headersStr.split(",");
            if (headers[0].startsWith(DataConstant.UTF8_BOM)) {
                headers[0] = headers[0].substring(1);
            }
            List<LinkedHashMap<String, Object>> csvData = FileUtil.getCsvData(sysFile.getFileUrl(),DataConstant.READ_DATA_ROW);
            csvVo.setDataList(csvData);
            List<DataFileField> dataFileFieldList = batchInsertDataFileField(sysFile, headers, csvData.get(0));
            csvVo.setFieldDataList(dataFileFieldList);
            csvVo.setFileId(sysFile.getFileId());
        }catch (Exception e){
            log.info("fileUrl:【{}】Exception Message : {}",sysFile.getFileUrl(),e);
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL,"请检查文件编码格式");
        }
        return BaseResultEntity.success(csvVo);
    }

    public List<DataFileField> batchInsertDataFileField(SysFile sysFile,String[] headers,LinkedHashMap<String,Object> dataMap) {
        List<DataFileField> fileFieldList = new ArrayList<>();
        int i = 1;
        for (String fieldName : headers) {
            if (StringUtils.isNotBlank(fieldName)) {
                FieldTypeEnum fieldType = getFieldType(dataMap.get(fieldName).toString());
                if (fieldName.substring(0, 1).matches(DataConstant.MATCHES)) {
                    fileFieldList.add(new DataFileField(sysFile.getFileId(), fieldName,fieldType.getCode(), null));
                } else {
                    fileFieldList.add(new DataFileField(sysFile.getFileId(), fieldName,fieldType.getCode(), DataConstant.FIELD_NAME_AS + i));
                    i++;
                }
            } else {
                fileFieldList.add(new DataFileField(sysFile.getFileId(), fieldName,FieldTypeEnum.STRING.getCode(), DataConstant.FIELD_NAME_AS + i));
                i++;
            }
        }
        return fileFieldList;
    }

    public List<DataFileField> batchInsertDataDataSourceField(List<String> headers,Map<String,Object> dataMap) {
        List<DataFileField> fileFieldList = new ArrayList<>();
        int i = 1;
        Iterator<String> iterator = headers.iterator();
        while (iterator.hasNext()){
            String fieldName = iterator.next();
            if (StringUtils.isNotBlank(fieldName)) {
                FieldTypeEnum fieldType = dataMap.containsKey(fieldName)&&dataMap.get(fieldName)!=null?getFieldType(dataMap.get(fieldName).toString()):FieldTypeEnum.STRING;
                if (fieldName.substring(0, 1).matches(DataConstant.MATCHES)) {
                    fileFieldList.add(new DataFileField(null, fieldName,fieldType.getCode(), null));
                } else {
                    fileFieldList.add(new DataFileField(null, fieldName,fieldType.getCode(), DataConstant.FIELD_NAME_AS + i));
                    i++;
                }
            } else {
                fileFieldList.add(new DataFileField(null, fieldName,FieldTypeEnum.STRING.getCode(), DataConstant.FIELD_NAME_AS + i));
                i++;
            }
        }
        return fileFieldList;
    }


    public FieldTypeEnum getFieldType(String fieldVal) {
        if (StringUtils.isBlank(fieldVal)) {
            return FieldTypeEnum.STRING;
        }
        if (DataConstant.RESOURCE_PATTERN_SCIENTIFIC_NOTATION.matcher(fieldVal).find()) {
            return FieldTypeEnum.DOUBLE;
        }
        if (DataConstant.RESOURCE_PATTERN_DOUBLE.matcher(fieldVal).find()) {
            return FieldTypeEnum.DOUBLE;
        }
        if (DataConstant.RESOURCE_PATTERN_LONG.matcher(fieldVal).find()) {
            return FieldTypeEnum.LONG;
        }
        if (DataConstant.RESOURCE_PATTERN_INTEGER.matcher(fieldVal).find()) {
            return FieldTypeEnum.INTEGER;
        }
        return FieldTypeEnum.STRING;
    }

    public BaseResultEntity getResourceTags() {
        return BaseResultEntity.success(dataResourceRepository.queryAllResourceTag());
    }

    public Object findFusionCopyResourceList(Long startOffset, Long endOffset){
        List<DataResource> resourceList=dataResourceRepository.findCopyResourceList(startOffset,endOffset);
        Set<String> ids = resourceList.stream().map(DataResource::getResourceFusionId).collect(Collectors.toSet());
        BaseResultEntity result = fusionResourceService.getCopyResource(ids);
        log.info(JSONObject.toJSONString(result));
        return result.getResult();
    }

    public List<DataResourceCopyVo> findCopyResourceList(Long startOffset, Long endOffset){
        String localOrganShortCode = organConfiguration.getLocalOrganShortCode();
        List<DataResource> resourceList=dataResourceRepository.findCopyResourceList(startOffset,endOffset);
        if (resourceList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> resourceIds = resourceList.stream().map(DataResource::getResourceId).collect(Collectors.toList());
        Map<Long, List<ResourceTagListVo>> resourceTagMap = dataResourceRepository.queryDataResourceListTags(resourceIds).stream().collect(Collectors.groupingBy(ResourceTagListVo::getResourceId));
        List<Long> filterIdList=resourceList.stream().filter(item->item.getResourceAuthType().equals(DataResourceAuthType.ASSIGN.getAuthType())).map(DataResource::getResourceId).collect(Collectors.toList());
        Map<Long, List<DataResourceVisibilityAuth>> resourceAuthMap=new HashMap<>();
        if(filterIdList!=null&&filterIdList.size()!=0) {
            List<DataResourceVisibilityAuth> authList=dataResourceRepository.findAuthOrganByResourceId(filterIdList);
            resourceAuthMap=authList.stream().collect(Collectors.groupingBy(DataResourceVisibilityAuth::getResourceId));
        }
        List<DataFileField> fileFieldList = dataResourceRepository.queryDataFileField(new HashMap() {{
            put("resourceIds", resourceIds);
        }});
        Map<Long, List<DataFileField>> fileFieldListMap = fileFieldList.stream().collect(Collectors.groupingBy(DataFileField::getResourceId));
        Set<Long> userids = resourceList.stream().map(DataResource::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> sysUserMap = sysUserService.getSysUserMap(userids);
        List<DataResourceCopyVo> copyVolist = new ArrayList();
        for (DataResource dataResource : resourceList) {
//            if (dataResource.getResourceSource() == 3)
//                continue;
            if (dataResource.getResourceFusionId()==null|| "".equals(dataResource.getResourceFusionId().trim())){
                dataResource.setResourceFusionId(organConfiguration.generateUniqueCode());
                dataResourcePrRepository.editResource(dataResource);
            }else {
                String organShortCode = dataResource.getResourceFusionId().substring(0, 12);
                if (!localOrganShortCode.equals(organShortCode)){
                    dataResource.setResourceFusionId(organConfiguration.generateUniqueCode());
                    dataResourcePrRepository.editResource(dataResource);
                }
            }
            List<String> tags = Optional.ofNullable(resourceTagMap.get(dataResource.getResourceId())).map(list -> list.stream().map(ResourceTagListVo::getTagName).collect(Collectors.toList())).orElse(null);
            List<String> authOrganList = Optional.ofNullable(resourceAuthMap.get(dataResource.getResourceId())).map(list -> list.stream().map(DataResourceVisibilityAuth::getOrganGlobalId).collect(Collectors.toList())).orElse(null);
            copyVolist.add(DataResourceConvert.dataResourcePoConvertCopyVo(dataResource,organConfiguration.getSysLocalOrganId(),StringUtils.join(tags,","),authOrganList,fileFieldListMap.get(dataResource.getResourceId()),sysUserMap.get(dataResource.getUserId())));
        }
        return copyVolist;
    }

    public BaseResultEntity handleDataResourceFile(DataResource dataResource,String url) throws Exception {
        File file = new File(url);
        if (file.exists()) {
            ResourceFileData resourceFileData = getResourceFileData(url);
            dataResource.setFileRows(resourceFileData.getFileContentSize());
            if (dataResource.getFileRows() > 0) {
                dataResource.setFileHandleField(resourceFileData.getField());
                dataResource.setFileColumns(resourceFileData.getFieldSize());
            } else {
                dataResource.setFileHandleField("");
                dataResource.setFileColumns(0);
            }
            if (resourceFileData.getResourceFileYRow()>0){
                dataResource.setFileContainsY(1);
                dataResource.setFileYRows(resourceFileData.getResourceFileYRow());
                BigDecimal yRowRatio = new BigDecimal(resourceFileData.getResourceFileYRow()).divide(new BigDecimal(dataResource.getFileRows()),6, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                dataResource.setFileYRatio(yRowRatio);
            }
            try {
                String md5hash = FileUtil.md5HashCode(file);
                dataResource.setResourceHashCode(md5hash);
            }catch (Exception e){
                log.info("resource_id:{} - url:{} - e:{}",dataResource.getResourceId(),url,e.getMessage());
                return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"文件hash出错");
            }
        }else {
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"无文件信息");
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity handleDataResourceSource(DataResource dataResource, List<DataResourceFieldReq> fieldList, DataSource dataSource) {
        List<String> fieldNames = fieldList.stream().map(DataResourceFieldReq::getFieldName).collect(Collectors.toList());
        BaseResultEntity baseResultEntity = dataSourceService.tableDataStatistics(dataSource, fieldNames.contains("y") || fieldNames.contains("Y"));
        if (!baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())) {
            return baseResultEntity;
        }
        Map<String,Object> map = (Map<String, Object>) baseResultEntity.getResult();
        Object total = map.get("total");
        if (total!=null){
            dataResource.setFileRows(Integer.parseInt(total.toString()));
            dataResource.setFileHandleField(String.join(",", fieldNames));
            dataResource.setFileColumns(fieldNames.size());
        }else {
            dataResource.setFileHandleField("");
            dataResource.setFileColumns(0);
        }
        Object ytotal = map.get("ytotal");
        if (ytotal!=null){
            dataResource.setFileContainsY(1);
            dataResource.setFileYRows(Integer.parseInt(ytotal.toString()));
            BigDecimal yRowRatio = new BigDecimal(dataResource.getFileYRows()).divide(new BigDecimal(dataResource.getFileRows()),6, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
            dataResource.setFileYRatio(yRowRatio);
        }
        try {
            String md5hash = SignUtil.getMD5ValueLowerCaseByDefaultEncode(dataSource.getDbUrl());
            dataResource.setResourceHashCode(md5hash);
        }catch (Exception e){
            log.info("resource_id:{} - url:{} - e:{}",dataResource.getResourceId(),dataSource.getDbUrl(),e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"数据库url hash出错");
        }
        return BaseResultEntity.success();
    }

    public ResourceFileData getResourceFileData(String filePath) throws Exception {
        ResourceFileData resourceFileData = new ResourceFileData();
        File file = new File(filePath);
        if (!file.exists()){
            log.info("{}-不存在",filePath);
            return resourceFileData;
        }
        boolean header = true;
        int yIndex = 0;
        String charsetName = FileUtil.charset(file);
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath),charsetName));
        String str;
        while((str = bufferedReader.readLine())!=null) {
            if (header){
                if (str.startsWith(DataConstant.UTF8_BOM)) {
                    str = str.substring(1);
                }
                resourceFileData.setField(str);
                header = false;
                yIndex = resourceFileData.getYIndex();
                continue;
            }
            if (resourceFileData.isFileContainsY()){
                if (StringUtils.isNotBlank(str)){
                    String[] rowValSplit = str.split(",");
                    if (resourceFileData.getFieldSize() == rowValSplit.length){
                        if (StringUtils.isNotBlank(rowValSplit[yIndex])&&!"0".equals(rowValSplit[yIndex])){
                            resourceFileData.addFileYRowSzie();
                        }
                    }
                }
            }else {
                resourceFileData.setFileContentSize( FileUtil.getFileLineNumber(filePath)-1);
                break;
            }
            resourceFileData.addContentSzie();
        }
        bufferedReader.close();
        return resourceFileData;
    }


    public Integer getResourceFileYRow(List<String> fileContent){
        if (fileContent.size()<=0) {
            return -1;
        }
        String field = fileContent.get(0);
        if (StringUtils.isBlank(field)) {
            return -1;
        }
        List<String> fieldList = Arrays.asList(field.toLowerCase().split(","));
        int yIndex = fieldList.indexOf("y");
        if (yIndex<0) {
            return -1;
        }
        int yRow = 0;
        Integer fieldListSize = fieldList.size();
        for (int i = 1; i < fileContent.size(); i++) {
            String rowVal = fileContent.get(i);
            if (StringUtils.isNotBlank(rowVal)){
                String[] rowValSplit = rowVal.split(",");
                if (fieldListSize == rowValSplit.length){
                    if (StringUtils.isNotBlank(rowValSplit[yIndex])&&!"0".equals(rowValSplit[yIndex])){
                        yRow++;
                    }
                }
            }
        }
        return yRow;
    }

    public TaskParam resourceSynGRPCDataSet(DataSource dataSource,DataResource dataResource,List<DataFileField> fieldList){
        if (dataResource.getResourceSource() !=2 ){
            return resourceSynGRPCDataSet(dataResource.getFileSuffix(),dataResource.getResourceFusionId(),dataResource.getUrl(),fieldList);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("dbType",dataSource.getDbType());
        map.put("dbUrl",dataSource.getDbUrl());
        map.put("tableName", dataSource.getDbTableName());
        if (SourceEnum.sqlite.getSourceType().equals(dataSource.getDbType())){
            map.put("db_path",dataSource.getDbUrl());
        }else {
            map.put("username",dataSource.getDbUsername());
            map.put("password", dataSource.getDbPassword());
            map.put("dbName", dataSource.getDbName());
            map.putAll(DataUtil.getJDBCData(dataSource.getDbUrl()));
        }
        return resourceSynGRPCDataSet(SourceEnum.SOURCE_MAP.get(dataSource.getDbType()).getSourceName(),dataResource.getResourceFusionId(), JSONObject.toJSONString(map),fieldList);
    }

    public TaskParam resourceSynGRPCDataSet(String suffix, String id, String url, List<DataFileField> fieldList){
        TaskParam<TaskDataSetParam> taskParam=new TaskParam();
        TaskDataSetParam param = new TaskDataSetParam();
        param.setFieldTypes(new ArrayList<>());
        param.setId(id);
        param.setAccessInfo(url);
        param.setDriver(suffix);
        for (DataFileField field : fieldList) {
            param.getFieldTypes().add(new TaskDataSetParam.FieldType(field.getFieldName(), FieldTypeEnum.FIELD_TYPE_MAP.get(field.getFieldType())));
        }
        taskParam.setTaskContentParam(param);
        taskHelper.submit(taskParam);
        return taskParam;
    }


    public BaseResultEntity resourceStatusChange(Long resourceId, Integer resourceState) {
        DataResource dataResource = dataResourceRepository.queryDataResourceById(resourceId);
        if (dataResource==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"找不到资源信息");
        }
        if(!dataResource.getResourceState().equals(resourceState)){
            dataResource.setResourceState(resourceState);
            dataResourcePrRepository.editResource(dataResource);
            fusionResourceService.saveResource(organConfiguration.getSysLocalOrganId(),findCopyResourceList(dataResource.getResourceId(), dataResource.getResourceId()));
            singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SINGLE_DATA_FUSION_RESOURCE_TASK.getHandleType(),dataResource))).build());
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity displayDatabaseSourceType() {
        return BaseResultEntity.success(baseConfiguration.isDisplayDatabaseSourceType());
    }

    public BaseResultEntity saveDerivationResource(List<ModelDerivationDto> derivationList,Long userId) {
        try {
            Map<String, List<ModelDerivationDto>> map = derivationList.stream().collect(Collectors.groupingBy(ModelDerivationDto::getOriginalResourceId));
            Set<String> resourceIds = map.keySet();
            DataResource dataResource = null;
            for (String resourceId : resourceIds) {
                dataResource = dataResourceRepository.queryDataResourceByResourceFusionId(resourceId);
                if (dataResource!=null) {
                    break;
                }
            }
            if (dataResource == null) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"衍生原始资源数据查询失败");
            }
            BaseResultEntity result = otherBusinessesService.getResourceListById(new ArrayList<>(resourceIds));
            if (result.getCode()!=0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"查询中心节点数据失败:"+result.getMsg());
            }
            List<LinkedHashMap<String,Object>> fusionResourceMap = (List<LinkedHashMap<String,Object>>)result.getResult();
            StringBuilder resourceNames = new StringBuilder(dataResource.getResourceName());
            for (LinkedHashMap<String, Object> resourceMap : fusionResourceMap) {
                String resourceId = resourceMap.get("resourceId").toString();
                if (!dataResource.getResourceFusionId().equals(resourceId)){
                    resourceNames.append(resourceMap.get("resourceName").toString());
                }
            }
            List<ModelDerivationDto> modelDerivationDtos = map.get(dataResource.getResourceFusionId());
            for (ModelDerivationDto modelDerivationDto : modelDerivationDtos) {
                String url = dataResource.getUrl();
                if (StringUtils.isNotBlank(modelDerivationDto.getPath())){
                    url = modelDerivationDto.getPath();
                }else {
                    if (url.contains(".csv")) {
                        url = url.replace(".csv","_"+modelDerivationDto.getType()+".csv");
                    }
                    if (url.contains(".db")){
                        String[] split = url.split("\\.");
                        url = url.replace("."+split[1],"_"+modelDerivationDto.getType()+".csv");
                    }
                }
                log.info(url);
                long start = System.currentTimeMillis();
                File file = new File(url);
//                while ((System.currentTimeMillis() - start)< 300000){
//                    if (file.exists()){
//                        break;
//                    }
//                }
                if (!file.exists()) {
                    continue;
//                    return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"衍生数据文件不存在");
                }
                DataResource derivationDataResource = new DataResource();
                derivationDataResource.setUrl(url);
                derivationDataResource.setResourceName(resourceNames.toString() + modelDerivationDto.getDerivationType());
                derivationDataResource.setResourceAuthType(2);
                derivationDataResource.setResourceSource(3);
                if (userId==null || userId==0L){
                    derivationDataResource.setUserId(dataResource.getUserId());
                }else {
                    derivationDataResource.setUserId(userId);
                }
                derivationDataResource.setOrganId(0L);
                derivationDataResource.setFileId(0L);
                derivationDataResource.setFileSize(Integer.parseInt(String.valueOf(file.length())));
                derivationDataResource.setFileSuffix("csv");
                derivationDataResource.setFileColumns(0);
                derivationDataResource.setFileRows(0);
                derivationDataResource.setFileHandleStatus(0);
                derivationDataResource.setResourceNum(0);
                derivationDataResource.setDbId(0L);
                derivationDataResource.setResourceState(0);
                BaseResultEntity baseResultEntity = handleDataResourceFile(derivationDataResource, url);
                if (!baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())) {
                    return baseResultEntity;
                }
                derivationDataResource.setResourceFusionId(modelDerivationDto.getNewResourceId());
                dataResourcePrRepository.saveResource(derivationDataResource);
                List<DataFileField> dataFileFields = dataResourceRepository.queryDataFileFieldByFileId(dataResource.getResourceId());
                for (DataFileField dataFileField : dataFileFields) {
                    dataFileField.setFileId(null);
                    dataFileField.setResourceId(derivationDataResource.getResourceId());
                }
                dataResourcePrRepository.saveResourceFileFieldBatch(dataFileFields);
                DataResourceTag dataResourceTag = new DataResourceTag(modelDerivationDto.getDerivationType());
                dataResourcePrRepository.saveResourceTag(dataResourceTag);
                dataResourcePrRepository.saveResourceTagRelation(dataResourceTag.getTagId(),derivationDataResource.getResourceId());
                fusionResourceService.saveResource(organConfiguration.getSysLocalOrganId(),findCopyResourceList(derivationDataResource.getResourceId(), derivationDataResource.getResourceId()));
//                singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SINGLE_DATA_FUSION_RESOURCE_TASK.getHandleType(),derivationDataResource))).build());
            }
            return BaseResultEntity.success(modelDerivationDtos.stream().map(ModelDerivationDto::getNewResourceId).collect(Collectors.toList()));
        }catch (Exception e){
            log.info("衍生数据异常:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"衍生数据异常:"+e.getMessage());
        }
    }

    public BaseResultEntity getDerivationResourceList(DerivationResourceReq req) {
        List<DataDerivationResourceVo> dataDerivationResourceVos = dataResourceRepository.queryDerivationResourceList(req);
        if (dataDerivationResourceVos.size()==0){
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Set<Long> userIds = dataDerivationResourceVos.stream().map(DataDerivationResourceVo::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> sysUserMap = sysUserService.getSysUserMap(userIds);
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        for (DataDerivationResourceVo dataDerivationResourceVo : dataDerivationResourceVos) {
            SysUser sysUser = sysUserMap.get(dataDerivationResourceVo.getUserId());
            dataDerivationResourceVo.setUserName(sysUser==null?"":sysUser.getUserName());
            dataDerivationResourceVo.setOrganId(sysLocalOrganInfo.getOrganId());
            dataDerivationResourceVo.setOrganName(sysLocalOrganInfo.getOrganName());
        }
        Integer count = dataResourceRepository.queryDerivationResourceListCount(req);

        return BaseResultEntity.success(new PageDataEntity(count,req.getPageSize(),req.getPageNo(),dataDerivationResourceVos));
    }

    public DataResource getDataResourceUrl(Long resourceId) {
        return dataResourceRepository.queryDataResourceById(resourceId);
    }

    public BaseResultEntity getDerivationResourceData(DerivationResourceReq req) {
        List<DataDerivationResourceVo> dataDerivationResourceVos = dataResourceRepository.queryDerivationResourceList(req);
        if (dataDerivationResourceVos.size()==0){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"无数据信息");
        }
        DataDerivationResourceVo dataDerivationResourceVo = dataDerivationResourceVos.get(0);
        DataDerivationResourceDataVo dataVo= DataResourceConvert.dataDerivationResourcePoConvertDataVo(dataDerivationResourceVo);
        DataModelTask modelTask = dataModelRepository.queryModelTaskById(dataVo.getTaskId());
        if (modelTask==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        }
        if (StringUtils.isBlank(modelTask.getComponentJson())){
            return BaseResultEntity.success();
        }
        List<DataComponent> dataComponents = JSONObject.parseArray(modelTask.getComponentJson(),DataComponent.class);
        dataComponents = dataComponents.stream().filter(d->d.getComponentName().equals(dataVo.getTag())|| "dataSet".equals(d.getComponentCode())).collect(Collectors.toList());
        for (DataComponent dataComponent : dataComponents) {
            Map<String, String> map = JSONArray.parseArray(dataComponent.getDataJson(), DataComponentValue.class).stream().collect(Collectors.toMap(DataComponentValue::getKey, DataComponentValue::getVal));
            if (dataComponent.getComponentName().equals(dataVo.getTag())){
                dataVo.setTotalTime(dataComponent.getEndTime() - dataComponent.getStartTime());
                if (map.containsKey("selectFeatures")){
                    dataVo.setAlignFeature(map.get("selectFeatures"));
                }else if (map.containsKey("MultipleSelected")){
                    dataVo.setAlignFeature(map.get("MultipleSelected"));
                }
            }
            if ("dataSet".equals(dataComponent.getComponentCode())){
                log.info(dataComponent.getDataJson());
                List<ModelProjectResourceVo> modelProjectResourceVos = JSONArray.parseArray(map.get("selectData"), ModelProjectResourceVo.class);
                for (ModelProjectResourceVo modelProjectResourceVo : modelProjectResourceVos) {
                    if (modelProjectResourceVo.getParticipationIdentity() == 1){
                        dataVo.setInitiateOrganResource(modelProjectResourceVo.getResourceId());
                    }else if (modelProjectResourceVo.getParticipationIdentity() == 2){
                        dataVo.setProviderOrganResource(modelProjectResourceVo.getResourceId());
                    }
                }
            }

        }
        SysUser sysUser = sysUserService.getSysUserById(dataVo.getUserId());
        if (sysUser!=null) {
            dataVo.setUserName(sysUser.getUserName());
        }
        return BaseResultEntity.success(dataVo);
    }

    public BaseResultEntity saveFusionResource(DataFusionCopyDto dto) {
        try {
            log.info(dto.getCopyPart());
            List<DataResourceCopyVo> dataResourceCopyVos = JSONArray.parseArray(dto.getCopyPart(), DataResourceCopyVo.class);
            BaseResultEntity resultEntity = fusionResourceService.saveResource(dto.getOrganId(), dataResourceCopyVos);
            log.info(JSONObject.toJSONString(resultEntity));
        }catch (Exception e){
            e.printStackTrace();
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity noticeResource(String resourceId) {
        log.info(resourceId);
        DataResource dataResource = dataResourceRepository.queryDataResourceByResourceFusionId(resourceId);
        log.info(JSONObject.toJSONString(dataResource));
        if (dataResource==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"找不到资源信息");
        }
        singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SINGLE_DATA_FUSION_RESOURCE_TASK.getHandleType(),dataResource))).build());
        return BaseResultEntity.success();
    }
}

