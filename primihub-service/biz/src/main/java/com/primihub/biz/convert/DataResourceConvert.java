package com.primihub.biz.convert;

import com.primihub.biz.entity.data.dataenum.FieldTypeEnum;
import com.primihub.biz.entity.data.po.DataFileField;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.po.DataResourceTag;
import com.primihub.biz.entity.data.req.DataResourceFieldReq;
import com.primihub.biz.entity.data.req.DataResourceReq;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.entity.sys.po.SysFile;
import org.apache.commons.lang.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class DataResourceConvert {

    public static ResourceTagVo dataResourceTagPoConvertVo(DataResourceTag tag){
        ResourceTagVo vo = new ResourceTagVo();
        vo.setTagId(tag.getTagId());
        vo.setTagName(tag.getTagName());
        return vo;
    }
    public static ResourceTagListVo dataResourceTagPoConvertListVo(DataResourceTag tag){
        ResourceTagListVo vo = new ResourceTagListVo();
        vo.setTagId(tag.getTagId());
        vo.setTagName(tag.getTagName());
        return vo;
    }

    public static DataResource dataResourceReqConvertPo(DataResourceReq req, Long userId, Long organId, SysFile sysFile){
        DataResource po = new DataResource();
        po.setResourceId(req.getResourceId());
        po.setResourceName(req.getResourceName());
        po.setResourceDesc(req.getResourceDesc());
        po.setResourceAuthType(req.getResourceAuthType());
        po.setResourceSource(req.getResourceSource());
        po.setUserId(userId);
        po.setOrganId(organId);
        //TODO 先默认文件信息 liweihua
        po.setFileId(sysFile.getFileId());
        po.setFileSize(sysFile.getFileSize().intValue());
        po.setFileSuffix(sysFile.getFileSuffix());
        po.setFileColumns(0);
        po.setFileRows(0);
        po.setFileHandleStatus(0);
        po.setResourceNum(0);
        po.setDbId(0L);
        po.setUrl(sysFile.getFileUrl());
        po.setPublicOrganId(req.getPublicOrganId());
        return po;
    }

    public static void editResourceReqConvertPo(DataResourceReq req, DataResource po){
        if (req.getResourceName()!=null && !req.getResourceName().trim().equals("")){
            po.setResourceName(req.getResourceName());
        }
        if (req.getResourceDesc()!=null && !req.getResourceDesc().trim().equals("")){
            po.setResourceDesc(req.getResourceDesc());
        }
        if (req.getResourceAuthType()!=null && req.getResourceAuthType()!=0){
            po.setResourceAuthType(req.getResourceAuthType());
        }
        if (req.getResourceSource()!=null && req.getResourceSource()!=0){
            po.setResourceSource(req.getResourceSource());
        }
    }

    public static DataResourceVo dataResourcePoConvertVo(DataResource po){
        DataResourceVo vo = new DataResourceVo();
        vo.setCreateDate(po.getCreateDate());
        vo.setDbId(po.getDbId());
        vo.setFileId(po.getFileId());
        vo.setFileSize(po.getFileSize());
        vo.setFileSuffix(po.getFileSuffix());
        vo.setFileColumns(po.getFileColumns());
        vo.setFileRows(po.getFileRows());
        vo.setFileHandleStatus(po.getFileHandleStatus());
        vo.setFileContainsY(po.getFileContainsY());
        vo.setFileYRows(po.getFileYRows());
        vo.setFileYRatio(po.getFileYRatio());
        vo.setOrganId(po.getOrganId());
        vo.setResourceAuthType(po.getResourceAuthType());
        vo.setResourceSource(po.getResourceSource());
        vo.setResourceDesc(po.getResourceDesc());
        vo.setResourceName(po.getResourceName());
        vo.setResourceId(po.getResourceId());
        vo.setResourceNum(po.getResourceNum());
        vo.setUserId(po.getUserId());
        vo.setUrl(po.getUrl());
        vo.setFileHandleField(StringUtils.isBlank(po.getFileHandleField())?new String[]{}:po.getFileHandleField().split(","));
        return vo;
    }
    public static DataFileFieldVo DataFileFieldPoConvertVo(DataFileField fileField){
        DataFileFieldVo dataFileFieldVo = new DataFileFieldVo();
        dataFileFieldVo.setFieldId(fileField.getFieldId());
        dataFileFieldVo.setFieldName(fileField.getFieldName());
        dataFileFieldVo.setFieldAs(fileField.getFieldAs());
        dataFileFieldVo.setFieldType(FieldTypeEnum.getTypeName(fileField.getFieldType()));
        dataFileFieldVo.setFieldDesc(fileField.getFieldDesc());
        dataFileFieldVo.setRelevance(fileField.getRelevance() != 0);
        dataFileFieldVo.setGrouping(fileField.getGrouping() != 0);
        dataFileFieldVo.setProtectionStatus(fileField.getProtectionStatus() != 0);
        dataFileFieldVo.setCreateDate(fileField.getCreateDate());
        return dataFileFieldVo;
    }

    public static DataFileField DataFileFieldReqConvertPo(DataResourceFieldReq req, Long fileId, Long resourceId){
        DataFileField dataFileField = DataFileFieldReqConvertPo(req, FieldTypeEnum.getEnumByTypeName(req.getFieldType()));
        dataFileField.setFileId(fileId);
        dataFileField.setResourceId(resourceId);
        return dataFileField;
    }

    public static DataFileField DataFileFieldReqConvertPo(DataResourceFieldReq req,FieldTypeEnum fieldTypeEnum){
        DataFileField dataFileField = new DataFileField();
        dataFileField.setFieldName(req.getFieldName());
        dataFileField.setFieldId(req.getFieldId());
        dataFileField.setFieldAs(req.getFieldAs());
        if (fieldTypeEnum!=null)
            dataFileField.setFieldType(fieldTypeEnum.getCode());
        dataFileField.setFieldDesc(req.getFieldDesc());
        dataFileField.setRelevance(req.getRelevance());
        dataFileField.setGrouping(req.getGrouping());
        dataFileField.setProtectionStatus(req.getProtectionStatus());
        return dataFileField;
    }

    public static DataPsiResourceAllocationVo DataResourcePoConvertAllocationVo(DataResource dataResource, List<DataFileField> fileFieldList, String organId){
        DataPsiResourceAllocationVo vo = new DataPsiResourceAllocationVo();
        vo.setOrganId(organId);
//        if (StringUtils.isNotBlank(dataResource.getResourceFusionId())){
//            vo.setResourceId(dataResource.getResourceFusionId());
//        }else {
            vo.setResourceId(dataResource.getResourceId().toString());
//        }
        vo.setResourceName(dataResource.getResourceName());
        if (fileFieldList!=null&&fileFieldList.size()>0){
            for (DataFileField dataFileField : fileFieldList) {
                vo.getKeywordList().add(dataFileField.getFieldName());
            }
        }
        return vo;
    }

    public static DataResourceCopyVo dataResourcePoConvertCopyVo(DataResource dataResource, String organId, String tags, List<String> authOrganList){
        DataResourceCopyVo dataResourceCopyVo = new DataResourceCopyVo();
        dataResourceCopyVo.setResourceId(dataResource.getResourceFusionId());
        dataResourceCopyVo.setResourceName(dataResource.getResourceName());
        dataResourceCopyVo.setResourceDesc(dataResource.getResourceDesc());
        dataResourceCopyVo.setResourceAuthType(dataResource.getResourceAuthType());
        dataResourceCopyVo.setResourceType(dataResource.getResourceSource());
        dataResourceCopyVo.setResourceRowsCount(dataResource.getFileRows());
        dataResourceCopyVo.setResourceColumnCount(dataResource.getFileColumns());
        dataResourceCopyVo.setResourceColumnNameList(dataResource.getFileHandleField());
        dataResourceCopyVo.setResourceContainsY(dataResource.getFileContainsY());
        dataResourceCopyVo.setResourceYRowsCount(dataResource.getFileYRows());
        dataResourceCopyVo.setResourceYRatio(dataResource.getFileYRatio());
        dataResourceCopyVo.setOrganId(organId);
        dataResourceCopyVo.setResourceTag(tags);
        dataResourceCopyVo.setAuthOrganList(authOrganList);
        return dataResourceCopyVo;
    }

    public static DataPsiResourceAllocationVo fusionResourceConvertAllocationVo(Map<String,Object> fr, String organId) {
        DataPsiResourceAllocationVo vo = new DataPsiResourceAllocationVo();
        if (fr==null)
            return vo;
        vo.setResourceId(fr.getOrDefault("resourceId","").toString());
        vo.setResourceName(fr.getOrDefault("resourceName","").toString());
        vo.setOrganId(fr.getOrDefault("organId","").toString());
//        System.out.println(vo.toString());
        Object resourceColumnNameList = fr.get("resourceColumnNameList");
        if (resourceColumnNameList!=null&&StringUtils.isNotBlank(resourceColumnNameList.toString())){
            vo.getKeywordList().addAll(Arrays.asList(resourceColumnNameList.toString().split(",")));
        }
        return vo;
    }
}
