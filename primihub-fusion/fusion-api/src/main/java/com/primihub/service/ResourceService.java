package com.primihub.service;

import com.primihub.convert.DataResourceConvert;
import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.entity.base.PageDataEntity;
import com.primihub.entity.copy.dto.CopyResourceDto;
import com.primihub.entity.copy.dto.CopyResourceFieldDto;
import com.primihub.entity.fusion.po.FusionOrgan;
import com.primihub.entity.resource.enumeration.AuthTypeEnum;
import com.primihub.entity.resource.param.OrganResourceParam;
import com.primihub.entity.resource.param.ResourceParam;
import com.primihub.entity.resource.po.FusionOrganResourceAuth;
import com.primihub.entity.resource.po.FusionResource;
import com.primihub.entity.resource.po.FusionResourceField;
import com.primihub.entity.resource.po.FusionResourceVisibilityAuth;
import com.primihub.repository.FusionRepository;
import com.primihub.repository.FusionResourceRepository;
import com.primihub.repository.GroupRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ResourceService {

    @Autowired
    private FusionResourceRepository resourceRepository;
    @Autowired
    private FusionRepository fusionRepository;
    @Autowired
    private GroupRepository groupRepository;

    public BaseResultEntity getResourceList(ResourceParam param) {
        List<Long> groupList = groupRepository.findOrganInGroup(param.getGlobalId());
        if(groupList==null|| groupList.size()==0)
            return BaseResultEntity.success(new PageDataEntity(0,param.getPageSize(),param.getPageNo(),new ArrayList()));
        param.setGroupList(groupList);
        List<FusionResource> fusionResources = resourceRepository.selectFusionResource(param);
        if (fusionResources.isEmpty())
            return BaseResultEntity.success(new PageDataEntity(0,param.getPageSize(),param.getPageNo(),new ArrayList()));
        Integer count = resourceRepository.selectFusionResourceCount(param);
        Set<String> organIds = fusionResources.stream().map(FusionResource::getOrganId).collect(Collectors.toSet());
        Map<String, String> organNameMap = fusionRepository.selectFusionOrganByGlobalIds(organIds).stream().collect(Collectors.toMap(FusionOrgan::getGlobalId, FusionOrgan::getGlobalName));
        Set<Long> resourceIds = fusionResources.stream().map(FusionResource::getId).collect(Collectors.toSet());
        Map<Long, List<FusionResourceField>> resourceFielMap = resourceRepository.selectFusionResourceFieldByIds(resourceIds).stream().collect(Collectors.groupingBy(FusionResourceField::getResourceId));
        return BaseResultEntity.success(new PageDataEntity(count,param.getPageSize(),param.getPageNo(),fusionResources.stream().map(re-> DataResourceConvert.fusionResourcePoConvertVo(re,organNameMap.get(re.getOrganId()),resourceFielMap.get(re.getResourceId()),null,param.getGlobalId())).collect(Collectors.toList())));
    }

    public BaseResultEntity getDataResource(String resourceId,String globalId) {
        FusionResource fusionResource = resourceRepository.selectFusionResourceByResourceId(resourceId);
        if (fusionResource==null)
            return BaseResultEntity.success();
        FusionOrgan fusionOrgan = fusionRepository.getFusionOrganByGlobalId(fusionResource.getOrganId());
        Set<String> groupInOrganIds = getGroupInOrganIds(globalId);
        List<FusionResourceField> fusionResourceFields = resourceRepository.selectFusionResourceFieldById(fusionResource.getId());
        return BaseResultEntity.success(DataResourceConvert.fusionResourcePoConvertVo(fusionResource,fusionOrgan==null?"":fusionOrgan.getGlobalName(),fusionResourceFields,groupInOrganIds,globalId));
    }


    public BaseResultEntity getResourceTagList() {
        return BaseResultEntity.success(resourceRepository.selectFusionResourceTag());
    }

    public BaseResultEntity batchSaveResource(String globalId,List<CopyResourceDto> copyResourceDtoList){
        if (StringUtils.isEmpty(globalId)){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"globalId");
        }
        String organShortCode = getOrganShortCode(globalId);
        List<CopyResourceDto> filterDtoList = copyResourceDtoList.stream().filter(dto -> dto.getResourceId() == null && StringUtils.isEmpty(dto.getResourceId()) && !dto.getResourceId().substring(0, 11).equals(organShortCode)).collect(Collectors.toList());
        if (filterDtoList!=null && filterDtoList.size()>0){
            StringBuilder sb = new StringBuilder("执行复制任务失败:").append("【条件检验未通过】\n");
            for (CopyResourceDto copyResourceDto : filterDtoList) {
                sb.append("resourceId:").append(copyResourceDto.getResourceId()).append("-").
                append("resourceName:").append(copyResourceDto.getResourceName()).append("-").
                append("organId:").append(copyResourceDto.getOrganId()).append("\n");
            }
            return BaseResultEntity.failure(BaseResultEnum.DATA_EXECUTE_TASK_FAIL,sb.toString());
        }
        log.info(globalId);
        Set<String> resourceIds = copyResourceDtoList.stream().map(CopyResourceDto::getResourceId).collect(Collectors.toSet());
        List<FusionResource> fusionResources = resourceRepository.selectFusionResourceById(resourceIds);
        Set<String> existenceResourceIds = fusionResources.stream().map(FusionResource::getResourceId).collect(Collectors.toSet());
        Map<String, FusionResource> fusionResourcesMap = fusionResources.stream().collect(Collectors.toMap(FusionResource::getResourceId, Function.identity()));
        Set<String> saveTags = new HashSet<>();
        Set<String> existenceTags = new HashSet<>();
        List<FusionResourceVisibilityAuth> authOrganList=new ArrayList<>();
        for (CopyResourceDto copyResourceDto : copyResourceDtoList) {
            FusionResource fusionResource = DataResourceConvert.CopyResourceDtoConvertPo(copyResourceDto);
            if (existenceResourceIds.contains(fusionResource.getResourceId())){
                FusionResource fr = fusionResourcesMap.get(fusionResource.getResourceId());
                fusionResource.setId(fr.getId());
                resourceRepository.updateFusionResource(fusionResource);
                existenceTags.addAll(Arrays.asList(fr.getResourceTag().split(",")));
                Map<String, FusionResourceField> resourceFieldMap = resourceRepository.selectFusionResourceFieldById(fr.getId()).stream().collect(Collectors.toMap(FusionResourceField::getFieldName, Function.identity()));
                if (copyResourceDto.getFieldList()!=null&&copyResourceDto.getFieldList().size()!=0){
                    for (CopyResourceFieldDto dto : copyResourceDto.getFieldList()) {
                        if (resourceFieldMap.get(dto.getFieldName())==null){
                            FusionResourceField fusionResourceField = DataResourceConvert.copyResourceFieldDtoConvertPo(dto, fr.getId(), null);
                            resourceRepository.saveResourceField(fusionResourceField);
                        }else {
                            FusionResourceField fusionResourceField = DataResourceConvert.copyResourceFieldDtoConvertPo(dto, fr.getId(), resourceFieldMap.get(dto.getFieldName()).getFieldId());
                            resourceRepository.updateResourceField(fusionResourceField);
                        }
                    }
                }
            }else {
                resourceRepository.saveFusionResource(fusionResource);
                if (copyResourceDto.getFieldList()!=null&&copyResourceDto.getFieldList().size()!=0){
                    List<FusionResourceField> resourceFields = copyResourceDto.getFieldList().stream().map(field -> DataResourceConvert.copyResourceFieldDtoConvertPo(field, fusionResource.getId(), null)).collect(Collectors.toList());
                    resourceRepository.saveBatchResourceField(resourceFields);
                }
            }
            if (fusionResource.getResourceAuthType().equals(AuthTypeEnum.VISIBILITY.getAuthType())){
                List<String> authStringList=copyResourceDto.getAuthOrganList();
                if(authStringList!=null) {
                    for (String authString : authStringList) {
                        authOrganList.add(new FusionResourceVisibilityAuth(copyResourceDto.getResourceId(), authString));
                    }
                }
                resourceRepository.deleteResourceAuthOrgan(copyResourceDto.getResourceId());
            }
            if (!StringUtils.isEmpty(copyResourceDto.getResourceTag()))
                saveTags.addAll(Arrays.asList(copyResourceDto.getResourceTag().split(",")));
        }
        saveTags.removeAll(existenceTags);
        if (!saveTags.isEmpty())
            resourceRepository.saveBatchResourceTag(saveTags);
        if(authOrganList.size()!=0)
            resourceRepository.saveBatchResourceAuthOrgan(authOrganList);
        return BaseResultEntity.success();
    }


    public String getOrganShortCode(String globalId){
        return globalId.substring(24,36);
    }

    public BaseResultEntity getResourceListById(String[] resourceIdArray,String globalId) {
        List<FusionResource> fusionResources = resourceRepository.selectFusionResourceByResourceIds(Arrays.stream(resourceIdArray).collect(Collectors.toSet()));
        if (fusionResources.size()==0)
            return BaseResultEntity.success();
        Set<String> organIds = fusionResources.stream().map(FusionResource::getOrganId).collect(Collectors.toSet());
        Map<String, String> organNameMap = fusionRepository.selectFusionOrganByGlobalIds(organIds).stream().collect(Collectors.toMap(FusionOrgan::getGlobalId, FusionOrgan::getGlobalName));
        Set<Long> resourceIds = fusionResources.stream().map(FusionResource::getId).collect(Collectors.toSet());
        Map<Long, List<FusionResourceField>> resourceFielMap = resourceRepository.selectFusionResourceFieldByIds(resourceIds).stream().collect(Collectors.groupingBy(FusionResourceField::getResourceId));
        Set<String> groupInOrganIds = getGroupInOrganIds(globalId);
        return BaseResultEntity.success(fusionResources.stream().map(re-> DataResourceConvert.fusionResourcePoConvertVo(re,organNameMap.get(re.getOrganId()),resourceFielMap.get(re.getId()),groupInOrganIds,globalId)).collect(Collectors.toList()));
    }

    public BaseResultEntity saveOrganResourceAuth(String organId, String resourceId, String projectId, Integer auditStatus) {
        FusionOrgan fusionOrgan = fusionRepository.getFusionOrganByGlobalId(organId);
        if (fusionOrgan==null)
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"无机构信息");
        FusionResource fusionResource = resourceRepository.selectFusionResourceByResourceId(resourceId);
        if (fusionResource==null)
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"无资源信息");
        FusionOrganResourceAuth auth = new FusionOrganResourceAuth();
        auth.setOrganId(fusionOrgan.getId());
        auth.setResourceId(fusionResource.getId());
        auth.setProjectId(projectId);
        auth.setAuditStatus(auditStatus);
        resourceRepository.saveFusionOrganResourceAuth(auth);
        return BaseResultEntity.success();
    }

    public BaseResultEntity getOrganResourceList(OrganResourceParam param) {
        List<FusionResource> fusionResources = resourceRepository.selectOrganResourcePage(param);
        if (fusionResources.isEmpty())
            return BaseResultEntity.success(new PageDataEntity(0,param.getPageSize(),param.getPageNo(),new ArrayList()));
        Integer count = resourceRepository.selectOrganResourceCount(param);
        Set<String> organIds = fusionResources.stream().map(FusionResource::getOrganId).collect(Collectors.toSet());
        Map<String, String> organNameMap = fusionRepository.selectFusionOrganByGlobalIds(organIds).stream().collect(Collectors.toMap(FusionOrgan::getGlobalId, FusionOrgan::getGlobalName));
        return BaseResultEntity.success(new PageDataEntity(count,param.getPageSize(),param.getPageNo(),fusionResources.stream().map(re-> DataResourceConvert.fusionResourcePoConvertVo(re,organNameMap.get(re.getOrganId()),null,null,null)).collect(Collectors.toList())));
    }

    public Set<String> getGroupInOrganIds(String globalId){
        List<Long> organInGroup = groupRepository.findOrganInGroup(globalId);
        List<String> organIds = groupRepository.findOrganGlobalIdByGroupIdList(organInGroup);
        organIds.add(globalId);
        return new HashSet<>(organIds);
    }
}
