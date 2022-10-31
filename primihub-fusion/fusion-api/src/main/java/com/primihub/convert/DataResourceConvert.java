package com.primihub.convert;

import com.primihub.entity.copy.dto.CopyResourceDto;
import com.primihub.entity.copy.dto.CopyResourceFieldDto;
import com.primihub.entity.resource.enumeration.AuthTypeEnum;
import com.primihub.entity.resource.po.FusionResource;
import com.primihub.entity.resource.po.FusionResourceField;
import com.primihub.entity.resource.vo.FusionResourceVo;
import org.springframework.util.StringUtils;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 资源实体转换类
 */
public class DataResourceConvert {

    public static FusionResource CopyResourceDtoConvertPo(CopyResourceDto dto){
        FusionResource fusionResource = new FusionResource();
        fusionResource.setResourceId(dto.getResourceId());
        fusionResource.setResourceName(dto.getResourceName());
        fusionResource.setResourceDesc(dto.getResourceDesc());
        fusionResource.setResourceType(dto.getResourceType());
        fusionResource.setResourceAuthType(dto.getResourceAuthType());
        fusionResource.setResourceRowsCount(dto.getResourceRowsCount());
        fusionResource.setResourceColumnCount(dto.getResourceColumnCount());
        fusionResource.setResourceColumnNameList(dto.getResourceColumnNameList().getBytes(Charset.forName("UTF-8")));
        fusionResource.setResourceContainsY(dto.getResourceContainsY());
        fusionResource.setResourceYRowsCount(dto.getResourceYRowsCount());
        fusionResource.setResourceYRatio(dto.getResourceYRatio());
        fusionResource.setResourceTag(String.valueOf(dto.getResourceTag()));
        fusionResource.setOrganId(dto.getOrganId());
        fusionResource.setIsDel(dto.getIsDel());
        fusionResource.setResourceState(dto.getResourceState());
        fusionResource.setResourceHashCode(dto.getResourceHashCode());
        return fusionResource;

    }


    public static FusionResourceVo fusionResourcePoConvertVo(FusionResource fusionResource, String organName, List<FusionResourceField> fieldList, Set<String> groupInOrganIds,String globalId){
        FusionResourceVo fusionResourceVo = new FusionResourceVo();
        fusionResourceVo.setResourceId(fusionResource.getResourceId());
        fusionResourceVo.setResourceName(fusionResource.getResourceName());
        fusionResourceVo.setResourceDesc(fusionResource.getResourceDesc());
        fusionResourceVo.setResourceType(fusionResource.getResourceType());
        fusionResourceVo.setResourceAuthType(fusionResource.getResourceAuthType());
        fusionResourceVo.setResourceRowsCount(fusionResource.getResourceRowsCount());
        fusionResourceVo.setResourceColumnCount(fusionResource.getResourceColumnCount());
        if (fusionResource.getResourceColumnNameList()!=null&&fusionResource.getResourceColumnNameList().length>=0)
            fusionResourceVo.setResourceColumnNameList(new String(fusionResource.getResourceColumnNameList(),Charset.forName("UTF-8")));
        fusionResourceVo.setFieldList(fieldList);
        fusionResourceVo.setResourceContainsY(fusionResource.getResourceContainsY());
        fusionResourceVo.setResourceYRowsCount(fusionResource.getResourceYRowsCount());
        fusionResourceVo.setResourceYRatio(fusionResource.getResourceYRatio());
        fusionResourceVo.setResourceTag(Arrays.asList(fusionResource.getResourceTag().split(",")));
        fusionResourceVo.setOrganId(fusionResource.getOrganId());
        fusionResourceVo.setOrganName(organName);
        fusionResourceVo.setCreateDate(fusionResource.getCTime());
        if (fieldList!=null&&fieldList.size()!=0){
            fusionResourceVo.setOpenColumnNameList(fieldList.stream().map(field-> StringUtils.isEmpty(field.getFieldAs())?field.getFieldName():field.getFieldAs()).collect(Collectors.joining(",")));
        }
        fusionResourceVo.setAvailable(1);
        if (fusionResource.getIsDel() == 0 && fusionResource.getResourceState() == 0){
            if (fusionResource.getResourceAuthType().equals(AuthTypeEnum.PRIVATE.getAuthType()) && fusionResource.getOrganId().equals(globalId)){
                fusionResourceVo.setAvailable(0);
            }
            if (fusionResource.getResourceAuthType().equals(AuthTypeEnum.VISIBILITY.getAuthType())){
                if (!StringUtils.isEmpty(fusionResource.getAuthOrgans()) && !StringUtils.isEmpty(globalId)){
                    Set<String> authOrgansSet = Arrays.stream(fusionResource.getAuthOrgans().split(",")).collect(Collectors.toSet());
                    authOrgansSet.add(fusionResource.getOrganId());
                    if (authOrgansSet.contains(globalId))
                        fusionResourceVo.setAvailable(0);
                }
            }
            if(fusionResource.getResourceAuthType().equals(AuthTypeEnum.PUBLIC.getAuthType()) && groupInOrganIds!=null && groupInOrganIds.contains(fusionResource.getOrganId())) {
                fusionResourceVo.setAvailable(0);
            }
        }
        fusionResourceVo.setResourceState(fusionResource.getResourceState());
        fusionResourceVo.setResourceHashCode(fusionResource.getResourceHashCode());
        return fusionResourceVo;
    }

    public static FusionResourceField copyResourceFieldDtoConvertPo(CopyResourceFieldDto dto,Long resourceId,Long fieldId){
        FusionResourceField fusionResourceField = new FusionResourceField();
        fusionResourceField.setFieldId(fieldId);
        fusionResourceField.setResourceId(resourceId);
        fusionResourceField.setFieldName(dto.getFieldName());
        fusionResourceField.setFieldAs(dto.getFieldAs());
        fusionResourceField.setFieldType(dto.getFieldType());
        fusionResourceField.setFieldDesc(dto.getFieldDesc());
        fusionResourceField.setRelevance(dto.getRelevance());
        fusionResourceField.setGrouping(dto.getGrouping());
        fusionResourceField.setProtectionStatus(dto.getProtectionStatus());
        return fusionResourceField;
    }
}
