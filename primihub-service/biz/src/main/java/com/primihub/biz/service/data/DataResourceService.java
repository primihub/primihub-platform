package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSON;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.convert.DataResourceConvert;
import com.primihub.biz.entity.base.*;
import com.primihub.biz.entity.data.dataenum.DataResourceAuthType;
import com.primihub.biz.entity.data.dataenum.FieldTypeEnum;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DataResourceFieldReq;
import com.primihub.biz.entity.data.req.DataResourceReq;
import com.primihub.biz.entity.data.req.DataSourceOrganReq;
import com.primihub.biz.entity.data.req.PageReq;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.entity.sys.po.SysFile;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysUser;
import com.primihub.biz.grpc.client.DataServiceGrpcClient;
import com.primihub.biz.repository.primarydb.data.DataProjectPrRepository;
import com.primihub.biz.repository.primarydb.data.DataResourcePrRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
import com.primihub.biz.service.sys.SysOrganService;
import com.primihub.biz.service.sys.SysUserService;
import com.primihub.biz.util.FileUtil;
import java_data_service.NewDatasetRequest;
import java_data_service.NewDatasetResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.File;
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
    private SysFileSecondarydbRepository sysFileSecondarydbRepository;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private SingleTaskChannel singleTaskChannel;
    @Autowired
    private DataServiceGrpcClient dataServiceGrpcClient;

    public BaseResultEntity getDataResourceList(DataResourceReq req, Long userId){
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userId",userId);
        paramMap.put("offset",req.getOffset());
        paramMap.put("pageSize",req.getPageSize());
        paramMap.put("resourceId",req.getResourceId());
        paramMap.put("resourceAuthType",req.getResourceAuthType());
        paramMap.put("resourceName",req.getResourceName());
        paramMap.put("tag",req.getTag());
        paramMap.put("selectTag",req.getSelectTag());
        paramMap.put("userName",req.getUserName());
        List<DataResource> dataResources = dataResourceRepository.queryDataResource(paramMap);
        if (dataResources.size()==0){
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Integer count = dataResourceRepository.queryDataResourceCount(paramMap);
        List<Long> resourceIds = new ArrayList<>();
        Set<Long> userIds = new HashSet<>();
        Set<Long> organIds = new HashSet<>();
        List<DataResourceVo> voList = dataResources.stream().map(vo->{
            resourceIds.add(vo.getResourceId());
            userIds.add(vo.getUserId());
            organIds.add(vo.getOrganId());
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
        SysFile sysFile = sysFileSecondarydbRepository.selectSysFileByFileId(req.getFileId());
        if (sysFile==null){
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"file");
        }
        DataResource dataResource = DataResourceConvert.dataResourceReqConvertPo(req,userId,null,sysFile);
        try {
            List<DataResourceFieldReq> fieldList = req.getFieldList();
            if (fieldList==null||fieldList.size()==0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"字段信息不能为空");
            handleDataResourceFile(dataResource,sysFile);
            SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
            if (sysLocalOrganInfo!=null&&sysLocalOrganInfo.getOrganId()!=null&&!sysLocalOrganInfo.getOrganId().trim().equals("")){
                dataResource.setResourceFusionId(organConfiguration.generateUniqueCode());
            }
            if (!resourceSynGRPCDataSet(dataResource.getFileSuffix(),StringUtils.isNotBlank(dataResource.getResourceFusionId())?dataResource.getResourceFusionId():dataResource.getFileId().toString(),dataResource.getUrl())){
                return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"无法将资源注册到数据集中");
            }
            dataResourcePrRepository.saveResource(dataResource);
            List<DataFileField> dataFileFieldList = fieldList.stream().map(field -> DataResourceConvert.DataFileFieldReqConvertPo(field, sysFile.getFileId(), dataResource.getResourceId())).collect(Collectors.toList());
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
                    DataResourceVisibilityAuth dataResourceVisibilityAuth=new DataResourceVisibilityAuth(dataResource.getResourceId(),organ.getOrganGlobalId(),organ.getOrganName(),organ.getOrganServerAddress());
                    authList.add(dataResourceVisibilityAuth);
                }
                dataResourcePrRepository.saveVisibilityAuth(authList);
            }

            if (sysLocalOrganInfo!=null&&sysLocalOrganInfo.getFusionMap()!=null&&!sysLocalOrganInfo.getFusionMap().isEmpty()){
                singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SINGLE_DATA_FUSION_RESOURCE_TASK.getHandleType(),dataResource))).build());
            }
        }catch (Exception e){
            log.info("save DataResource Exception：{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.FAILURE);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("resourceId",dataResource.getResourceId());
        map.put("resourceFusionId",dataResource.getResourceFusionId());
        map.put("resourceName",dataResource.getResourceName());
        map.put("resourceDesc",dataResource.getResourceDesc());
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
                    DataResourceVisibilityAuth dataResourceVisibilityAuth=new DataResourceVisibilityAuth(dataResource.getResourceId(),organ.getOrganGlobalId(),organ.getOrganName(),organ.getOrganServerAddress());
                    authList.add(dataResourceVisibilityAuth);
                }
                dataResourcePrRepository.saveVisibilityAuth(authList);
            }
        }
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        if (sysLocalOrganInfo!=null&&sysLocalOrganInfo.getFusionMap()!=null&&!sysLocalOrganInfo.getFusionMap().isEmpty()){
            singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SINGLE_DATA_FUSION_RESOURCE_TASK.getHandleType(),dataResource))).build());
        }
        resourceSynGRPCDataSet(dataResource.getFileSuffix(),StringUtils.isNotBlank(dataResource.getResourceFusionId())?dataResource.getResourceFusionId():dataResource.getFileId().toString(),dataResource.getUrl());
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
        List<DataFileFieldVo> dataFileFieldList = dataResourceRepository.queryDataFileFieldByFileId(dataResource.getFileId(),dataResource.getResourceId())
                .stream().map(DataResourceConvert::DataFileFieldPoConvertVo)
                .collect(Collectors.toList());
        Map<String,Object> map = new HashMap<>();
        try {
            List<LinkedHashMap<String, Object>> csvData = FileUtil.getCsvData(dataResource.getUrl(), 0, DataConstant.READ_DATA_ROW);
            map.put("dataList",csvData);
        }catch (Exception e){
            log.info("资源id:{}-文件读取失败：{}",dataResource.getResourceId(),e.getMessage());
            map.put("dataList",new ArrayList());
        }
        if(dataResource.getResourceAuthType().equals(DataResourceAuthType.ASSIGN.getAuthType())){
            List<DataResourceVisibilityAuth> authOrganList=dataResourceRepository.findAuthOrganByResourceId(new ArrayList(){{add(resourceId);}});
            map.put("authOrganList",authOrganList);
            singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.COMPARE_AND_FIX_LOCAL_ORGAN_NAME_TASK.getHandleType(),authOrganList))).build());
        }
        map.put("resource",dataResourceVo);
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
        if (dataResource == null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"未查询到资源信息");
        Integer count = dataResourceRepository.queryResourceProjectRelationCount(dataResource.getResourceFusionId());
        if (count>0){
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"该资源已关联项目");
        }
        dataResourcePrRepository.deleteResource(resourceId);
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        if (sysLocalOrganInfo!=null&&sysLocalOrganInfo.getFusionMap()!=null&&!sysLocalOrganInfo.getFusionMap().isEmpty()){
            dataResource.setIsDel(1);
            singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SINGLE_DATA_FUSION_RESOURCE_TASK.getHandleType(),dataResource))).build());
        }
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
            if (dataResource!=null&&dataResource.getUserId().equals(userId))
                isUpdate = true;
            map.put("isUpdate",isUpdate);
            map.put("pageData",new PageDataEntity(count,req.getPageSize(),req.getPageNo(),dataFileFieldList.stream().map(DataResourceConvert::DataFileFieldPoConvertVo).collect(Collectors.toList())));
        }
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity updateDataResourceField(DataResourceFieldReq req, FieldTypeEnum fieldTypeEnum) {
        if (StringUtils.isNotBlank(req.getFieldAs()))
            if (!req.getFieldAs().substring(0,1).matches(DataConstant.MATCHES))
                return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"字段别名格式错误");
        DataFileField dataFileField = DataResourceConvert.DataFileFieldReqConvertPo(req, fieldTypeEnum);
        dataResourcePrRepository.updateResourceFileField(dataFileField);
        return BaseResultEntity.success();
    }

    public BaseResultEntity resourceFilePreview(Long fileId,String resourceId) {
        if(StringUtils.isNotBlank(resourceId)){
            DataResource dataResource = dataResourceRepository.queryDataResourceByResourceFusionId(resourceId);
            if (dataResource==null)
                return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
            fileId = dataResource.getFileId();
        }
        SysFile sysFile = sysFileSecondarydbRepository.selectSysFileByFileId(fileId);
        if (sysFile==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        return getDataResourceCsvVo(sysFile);
    }

    public BaseResultEntity getDataResourceCsvVo(SysFile sysFile) {
        DataResourceCsvVo csvVo = null;
        try {
            List<String> fileContent = FileUtil.getFileContent(sysFile.getFileUrl(), 1);
            if (fileContent==null||fileContent.size()==0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL);
            String headersStr = fileContent.get(0);
            if (StringUtils.isBlank(headersStr))
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_FILE_CHECK_FAIL);
            csvVo =  new DataResourceCsvVo();
            String[] headers = headersStr.split(",");
            List<LinkedHashMap<String, Object>> csvData = FileUtil.getCsvData(sysFile.getFileUrl(), 0, DataConstant.READ_DATA_ROW);
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


    private FieldTypeEnum getFieldType(String fieldVal) {
        if (StringUtils.isBlank(fieldVal)) {
            return FieldTypeEnum.STRING;
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

    public List<DataResourceCopyVo> findCopyResourceList(Long startOffset, Long endOffset, String organId, String fusionServerAddress){
        String localOrganShortCode = organConfiguration.getLocalOrganShortCode();
        List<DataResource> resourceList=dataResourceRepository.findCopyResourceList(startOffset,endOffset);
        if (resourceList.isEmpty())
            return new ArrayList<>();
        List<Long> resourceIds = resourceList.stream().map(DataResource::getResourceId).collect(Collectors.toList());
        Map<Long, List<ResourceTagListVo>> resourceTagMap = dataResourceRepository.queryDataResourceListTags(resourceIds).stream().collect(Collectors.groupingBy(ResourceTagListVo::getResourceId));
        List<Long> filterIdList=resourceList.stream().filter(item->item.getResourceAuthType().equals(DataResourceAuthType.ASSIGN.getAuthType())).map(DataResource::getResourceId).collect(Collectors.toList());
        Map<Long, List<DataResourceVisibilityAuth>> resourceAuthMap=new HashMap<>();
        if(filterIdList!=null&&filterIdList.size()!=0) {
            List<DataResourceVisibilityAuth> authList=dataResourceRepository.findAuthOrganByResourceId(filterIdList);
            resourceAuthMap=authList.stream().filter(item->item.getOrganServerAddress().equals(fusionServerAddress)).collect(Collectors.groupingBy(DataResourceVisibilityAuth::getResourceId));
        }
        List<DataFileField> fileFieldList = dataResourceRepository.queryDataFileField(new HashMap() {{
            put("resourceIds", resourceIds);
        }});
        Map<Long, List<DataFileField>> fileFieldListMap = fileFieldList.stream().collect(Collectors.groupingBy(DataFileField::getResourceId));
        List<DataResourceCopyVo> copyVolist = new ArrayList();
        for (DataResource dataResource : resourceList) {
            if (dataResource.getResourceFusionId()==null||dataResource.getResourceFusionId().trim().equals("")){
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
            resourceAuthMap.get(dataResource.getResourceId());
            List<String> authOrganList = Optional.ofNullable(resourceAuthMap.get(dataResource.getResourceId())).map(list -> list.stream().map(DataResourceVisibilityAuth::getOrganGlobalId).collect(Collectors.toList())).orElse(null);
            copyVolist.add(DataResourceConvert.dataResourcePoConvertCopyVo(dataResource,organId,StringUtils.join(tags,","),authOrganList,fileFieldListMap.get(dataResource.getResourceId())));
        }
        return copyVolist;
    }

    public void handleDataResourceFile(DataResource dataResource,SysFile sysFile){
        File file = new File(sysFile.getFileUrl());
        if (file.exists()) {
            dataResource.setFileRows(FileUtil.getFileLineNumber(sysFile.getFileUrl())-1);
            List<String> fileContent = FileUtil.getFileContent(sysFile.getFileUrl(), null);
            if (fileContent.size() > 0) {
                dataResource.setFileHandleField(fileContent.get(0));
                dataResource.setFileColumns(dataResource.getFileHandleField().split(",").length);
            } else {
                dataResource.setFileHandleField("");
                dataResource.setFileColumns(0);
            }
            Integer resourceFileYRow = getResourceFileYRow(fileContent);
            if (resourceFileYRow>0){
                dataResource.setFileContainsY(1);
                dataResource.setFileYRows(resourceFileYRow);
                BigDecimal yRowRatio = new BigDecimal(resourceFileYRow).divide(new BigDecimal(dataResource.getFileRows()),6, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));
                dataResource.setFileYRatio(yRowRatio);
            }
        }
    }
    public Integer getResourceFileYRow(List<String> fileContent){
        String field = fileContent.get(0);
        if (StringUtils.isBlank(field))
            return -1;
        List<String> fieldList = Arrays.asList(field.toLowerCase().split(","));
        int yIndex = fieldList.indexOf("y");
        if (yIndex<0)
            return -1;
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

    public Boolean resourceSynGRPCDataSet(String suffix,String id,String url){
        log.info("run dataServiceGrpc fileSuffix:{} - fileId:{} - fileUrl:{} - time:{}",suffix,id,url,System.currentTimeMillis());
        NewDatasetRequest request = NewDatasetRequest.newBuilder()
                .setDriver(suffix)
                .setFid(id)
                .setPath(url)
                .build();
        log.info("NewDatasetRequest:{}",request.toString());
        try {
            NewDatasetResponse response = dataServiceGrpcClient.run(o -> o.newDataset(request));
            log.info("dataServiceGrpc Response:{}",response.toString());
            int retCode = response.getRetCode();
            if (retCode==0){
                log.info("dataServiceGrpc success");
                return true;
            }
        }catch (Exception e){
            log.info("dataServiceGrpcException:{}",e.getMessage());
        }
        log.info("end dataServiceGrpc fileSuffix:{} - fileId:{} - fileUrl:{}  - time:{}",suffix,id,url,System.currentTimeMillis());
        return false;
    }


}

