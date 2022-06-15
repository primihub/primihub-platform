package com.yyds.biz.service.data;

import com.alibaba.fastjson.JSON;
import com.yyds.biz.config.mq.SingleTaskChannel;
import com.yyds.biz.constant.DataConstant;
import com.yyds.biz.convert.DataResourceConvert;
import com.yyds.biz.entity.base.*;
import com.yyds.biz.entity.data.dataenum.FieldTypeEnum;
import com.yyds.biz.entity.data.po.DataFileField;
import com.yyds.biz.entity.data.po.DataResource;
import com.yyds.biz.entity.data.po.DataResourceAuthRecord;
import com.yyds.biz.entity.data.po.DataResourceTag;
import com.yyds.biz.entity.data.req.DataResourceFieldReq;
import com.yyds.biz.entity.data.req.DataResourceReq;
import com.yyds.biz.entity.data.req.PageReq;
import com.yyds.biz.entity.data.vo.DataResourceAuthRecordVo;
import com.yyds.biz.entity.data.vo.DataResourceVo;
import com.yyds.biz.entity.data.vo.ResourceTagListVo;
import com.yyds.biz.entity.sys.po.SysFile;
import com.yyds.biz.entity.sys.po.SysOrgan;
import com.yyds.biz.entity.sys.po.SysUser;
import com.yyds.biz.repository.primarydb.data.DataProjectPrRepository;
import com.yyds.biz.repository.primarydb.data.DataResourcePrRepository;
import com.yyds.biz.repository.secondarydb.data.DataProjectRepository;
import com.yyds.biz.repository.secondarydb.data.DataResourceRepository;
import com.yyds.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
import com.yyds.biz.service.sys.SysOrganService;
import com.yyds.biz.service.sys.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private DataProjectPrRepository dataProjectPrRepository;
    @Autowired
    private SysFileSecondarydbRepository sysFileSecondarydbRepository;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysOrganService sysOrganService;
    @Autowired
    private SingleTaskChannel singleTaskChannel;

    public BaseResultEntity getDataResourceList(DataResourceReq req,Long userId,Long organId,boolean isPsi){
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userId",userId);
        paramMap.put("organId",organId);
        paramMap.put("offset",req.getOffset());
        paramMap.put("pageSize",req.getPageSize());
        paramMap.put("resourceSortType",req.getResourceSortType());
        paramMap.put("resourceAuthType",req.getResourceAuthType());
        paramMap.put("resourceName",req.getResourceName());
        if (isPsi){
            paramMap.put("isPsi","true");
        }
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
        // 标签信息
        Map<Long, List<ResourceTagListVo>> resourceTagMap = dataResourceRepository.queryDataResourceListTags(resourceIds).stream().collect(Collectors.groupingBy(ResourceTagListVo::getResourceId));
        // 用户信息
        Map<Long, SysUser> sysUserMap = sysUserService.getSysUserMap(userIds);
        Map<Long, SysOrgan> sysOrganMap = sysOrganService.getSysOrganMap(organIds);
        for (DataResourceVo dataResourceVo : voList) {
            // TODO 查询用户信息 liweihua -- 完成
            SysUser sysUser = sysUserMap.get(dataResourceVo.getUserId());
            dataResourceVo.setUserName(sysUser==null?"":sysUser.getUserName());
            SysOrgan sysOrgan = sysOrganMap.get(dataResourceVo.getOrganId());
            dataResourceVo.setOrganName(sysOrgan==null?"":sysOrgan.getOrganName());
            dataResourceVo.setTags(resourceTagMap.get(dataResourceVo.getResourceId()));
            dataResourceVo.setUrl("");
        }
        return BaseResultEntity.success(new PageDataEntity(count,req.getPageSize(),req.getPageNo(),voList));
    }



    public BaseResultEntity saveDataResource(DataResourceReq req,Long userId,Long organId){
        SysFile sysFile = sysFileSecondarydbRepository.selectSysFileByFileId(req.getFileId());
        if (sysFile==null){
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"file");
        }
        DataResource dataResource = DataResourceConvert.dataResourceReqConvertPo(req,userId,organId,sysFile);
        try {
            dataResourcePrRepository.saveResource(dataResource);
            // 处理标签保存
            List<String> tags = req.getTags();
            for (String tagName : tags) {
                DataResourceTag dataResourceTag = new DataResourceTag(tagName);
                dataResourcePrRepository.saveResourceTag(dataResourceTag);
                dataResourcePrRepository.saveResourceTagRelation(dataResourceTag.getTagId(),dataResource.getResourceId());
            }
            singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.DATA_RESOURCE_FILE_TASK.getHandleType(),dataResource))).build());
        }catch (Exception e){
            log.info("save DataResource Exception：{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.FAILURE);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("resourceId",dataResource.getResourceId());
        map.put("resourceName",dataResource.getResourceName());
        map.put("resourceDesc",dataResource.getResourceDesc());
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity editDataResource(DataResourceReq req, Long userId, Long organId) {
        DataResource dataResource = dataResourceRepository.queryDataResourceById(req.getResourceId());
        if (dataResource==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"找不到资源信息");
        }
        DataResourceConvert.editResourceReqConvertPo(req,dataResource);
        try {
            // TODO 此处是否有编辑权限验证 liweihua
            dataResourcePrRepository.editResource(dataResource);
            //TODO 处理标签 先删除再增加 后期改造 liweihua
            handleResourceTag(req.getTags(),dataResource.getResourceId());
        }catch (Exception e){
            log.info("edit DataResource Exception: {}",e.getMessage());
        }
        Map<String,Object> map = new HashMap<>();
        map.put("resourceId",dataResource.getResourceId());
        map.put("resourceName",dataResource.getResourceName());
        map.put("resourceDesc",dataResource.getResourceDesc());
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getDataResource(Long resourceId) {
        // 查询资源信息
        DataResource dataResource = dataResourceRepository.queryDataResourceById(resourceId);
        if (dataResource==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        }
        List<DataResourceTag> dataResourceTags = dataResourceRepository.queryTagsByResourceId(resourceId);
        DataResourceVo dataResourceVo = DataResourceConvert.dataResourcePoConvertVo(dataResource);
        // TODO 补齐用户名 liweihua --完成
        SysUser sysUser = sysUserService.getSysUserById(dataResourceVo.getUserId());
        dataResourceVo.setUserName(sysUser == null?"":sysUser.getUserName());
        dataResourceVo.setTags(dataResourceTags.stream().map(DataResourceConvert::dataResourceTagPoConvertListVo).collect(Collectors.toList()));
        Map<String,Object> map = new HashMap<>();
        map.put("resource",dataResourceVo);
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
        Integer count = dataResourceRepository.queryResourceProjectRelationCount(resourceId);
        if (count>0){
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"该资源已关联项目");
        }
        dataResourcePrRepository.deleteResource(resourceId);
        return BaseResultEntity.success("删除资源成功");
    }

    public BaseResultEntity getAuthorizationList(PageReq req, Long userId,Integer status) {
        List<DataResourceAuthRecordVo> dataResourceAuthRecordVos = dataResourceRepository.queryResourceAuthRecord(req.getOffset(), req.getPageSize(), userId,status);
        if (dataResourceAuthRecordVos==null||dataResourceAuthRecordVos.size()==0){
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        //TODO dataResourceAuthRecordVos 补充机构名称 liweihua --完成
        Set<Long> organids = dataResourceAuthRecordVos.stream().map(DataResourceAuthRecordVo::getOrganId).collect(Collectors.toSet());
        Map<Long, SysOrgan> sysOrganMap = sysOrganService.getSysOrganMap(organids);
        dataResourceAuthRecordVos.forEach(vo->{
            SysOrgan sysOrgan = sysOrganMap.get(vo.getOrganId());
            vo.setOrganName(sysOrgan==null?"":sysOrgan.getOrganName());
        });
        Long count = dataResourceRepository.queryResourceAuthRecordCount(userId,status);
        return BaseResultEntity.success(new PageDataEntity(count.intValue(),req.getPageSize(),req.getPageNo(),dataResourceAuthRecordVos));
    }

    public BaseResultEntity approvalauthorization(Long recordId,Long userId,Integer status) {
        // 查询授权信息
        DataResourceAuthRecord dataResourceAuthRecord = dataResourceRepository.queryDataResourceAuthRecordById(recordId);
        if (dataResourceAuthRecord==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        }
        if (dataResourceAuthRecord.getRecordStatus()==1){
            return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"已授权");
        }
        if (dataResourceAuthRecord.getRecordStatus()==2){
            return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"已拒绝");
        }
        // TODO 补充审批用户名称 liweihua -- 完成
        SysUser sysUser = this.sysUserService.getSysUserById(userId);
        if (sysUser==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"未查询到审批人信息");
        }
        // 审核状态
        dataResourcePrRepository.updateAuthRecordStatus(status,recordId,userId,sysUser.getUserName());
        // 项目资源关系表更改
        dataProjectPrRepository.updateProjectResourceStatus(status,dataResourceAuthRecord.getProjectId(),dataResourceAuthRecord.getResourceId());
        if (status==1){
            // 更新项目表已授权资源数 +1
            dataProjectPrRepository.updateProjectAuthNum(dataResourceAuthRecord.getProjectId());
        }
        Map<String,Object> map = new HashMap<>();
        map.put("recordId",recordId);
        map.put("status",status);
        return BaseResultEntity.success(map);
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

    public BaseResultEntity updateDataResourceField(DataResourceFieldReq req,FieldTypeEnum fieldTypeEnum) {
        if (StringUtils.isNotBlank(req.getFieldAs()))
            if (!req.getFieldAs().substring(0,1).matches(DataConstant.MATCHES))
                return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"字段别名格式错误");
        DataFileField dataFileField = DataResourceConvert.DataFileFieldReqConvertPo(req, fieldTypeEnum);
        dataResourcePrRepository.updateResourceFileField(dataFileField);
        return BaseResultEntity.success();
    }
}
