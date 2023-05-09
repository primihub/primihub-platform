package com.primihub.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.primihub.convert.DataResourceConvert;
import com.primihub.entity.DataSet;
import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.entity.base.PageDataEntity;
import com.primihub.entity.copy.dto.CopyResourceDto;
import com.primihub.entity.copy.dto.CopyResourceFieldDto;
import com.primihub.entity.fusion.FusionOrgan;
import com.primihub.entity.resource.enumeration.AuthTypeEnum;
import com.primihub.entity.resource.param.ResourceParam;
import com.primihub.entity.resource.po.FusionResource;
import com.primihub.entity.resource.po.FusionResourceField;
import com.primihub.entity.resource.po.FusionResourceVisibilityAuth;
import com.primihub.repository.DataSetRepository;
import com.primihub.repository.FusionRepository;
import com.primihub.repository.FusionResourceRepository;
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
    private GrpcDataSetService dataSetService;
    @Autowired
    private DataSetRepository dataSetRepository;
    @Autowired
    private FusionRepository fusionRepository;


    public BaseResultEntity getResourceList(ResourceParam param) {
//        log.info(JSONObject.toJSONString(param));
        List<FusionResource> fusionResources = resourceRepository.selectFusionResource(param);
        if (fusionResources.isEmpty()) {
            return BaseResultEntity.success(new PageDataEntity(0,param.getPageSize(),param.getPageNo(),new ArrayList()));
        }
        Integer count = resourceRepository.selectFusionResourceCount(param);
        Set<Long> resourceIds = fusionResources.stream().map(FusionResource::getId).collect(Collectors.toSet());
        Set<String> organIds = fusionResources.stream().map(FusionResource::getOrganId).collect(Collectors.toSet());
        Map<String, String> organNameMap = fusionRepository.selectFusionOrganByGlobalIds(organIds).stream().collect(Collectors.toMap(FusionOrgan::getGlobalId, FusionOrgan::getGlobalName));
        log.info(JSONObject.toJSONString(organNameMap));
        Map<Long, List<FusionResourceField>> resourceFielMap = resourceRepository.selectFusionResourceFieldByIds(resourceIds).stream().collect(Collectors.groupingBy(FusionResourceField::getResourceId));
        return BaseResultEntity.success(new PageDataEntity(count,param.getPageSize(),param.getPageNo(),fusionResources.stream().map(re-> DataResourceConvert.fusionResourcePoConvertVo(re,organNameMap.get(re.getOrganId()),resourceFielMap.get(re.getId()),param.getGlobalId())).collect(Collectors.toList())));
    }

    public BaseResultEntity getDataResource(String resourceId,String globalId) {
        FusionResource fusionResource = resourceRepository.selectFusionResourceByResourceId(resourceId);
        if (fusionResource==null) {
            return BaseResultEntity.success();
        }
        FusionOrgan fusionOrgan = fusionRepository.getFusionOrganByGlobalId(fusionResource.getOrganId());
        List<FusionResourceField> fusionResourceFields = resourceRepository.selectFusionResourceFieldById(fusionResource.getId());
        return BaseResultEntity.success(DataResourceConvert.fusionResourcePoConvertVo(fusionResource,fusionOrgan==null?"":fusionOrgan.getGlobalName(),fusionResourceFields,globalId));
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
                resourceRepository.deleteResourceFieldByResourceId(fr.getId());
            }else {
                resourceRepository.saveFusionResource(fusionResource);
            }
            if (copyResourceDto.getFieldList()!=null&&copyResourceDto.getFieldList().size()!=0){
                List<FusionResourceField> resourceFields = copyResourceDto.getFieldList().stream().map(field -> DataResourceConvert.copyResourceFieldDtoConvertPo(field, fusionResource.getId(), null)).collect(Collectors.toList());
                resourceRepository.saveBatchResourceField(resourceFields);
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
            if (!StringUtils.isEmpty(copyResourceDto.getResourceTag())) {
                saveTags.addAll(Arrays.asList(copyResourceDto.getResourceTag().split(",")));
            }
            DataSet dataSet = copyResourceDto.getDataSet();
            if (dataSet!=null && !StringUtils.isEmpty(dataSet.getId())){
                dataSet.setHolder(1);
                dataSetService.saveDataSet(dataSet);
            }
        }
        saveTags.removeAll(existenceTags);
        if (!saveTags.isEmpty()) {
            resourceRepository.saveBatchResourceTag(saveTags);
        }
        if(authOrganList.size()!=0) {
            resourceRepository.saveBatchResourceAuthOrgan(authOrganList);
        }
        return BaseResultEntity.success();
    }


    public String getOrganShortCode(String globalId){
        return globalId.substring(24,36);
    }

    public BaseResultEntity getResourceListById(String[] resourceIdArray,String globalId) {
        List<FusionResource> fusionResources = resourceRepository.selectFusionResourceByResourceIds(Arrays.stream(resourceIdArray).collect(Collectors.toSet()));
        if (fusionResources.size()==0) {
            return BaseResultEntity.success();
        }
        Set<Long> resourceIds = fusionResources.stream().map(FusionResource::getId).collect(Collectors.toSet());
        Set<String> organIds = fusionResources.stream().map(FusionResource::getOrganId).collect(Collectors.toSet());
        Map<String, String> organNameMap = fusionRepository.selectFusionOrganByGlobalIds(organIds).stream().collect(Collectors.toMap(FusionOrgan::getGlobalId, FusionOrgan::getGlobalName));
        Map<Long, List<FusionResourceField>> resourceFielMap = resourceRepository.selectFusionResourceFieldByIds(resourceIds).stream().collect(Collectors.groupingBy(FusionResourceField::getResourceId));
        return BaseResultEntity.success(fusionResources.stream().map(re-> DataResourceConvert.fusionResourcePoConvertVo(re,organNameMap.get(re.getOrganId()),resourceFielMap.get(re.getId()),globalId)).collect(Collectors.toList()));
    }

    public BaseResultEntity getCopyResource(String[] resourceIds){
        Set<String> resourceIdArray = Arrays.stream(resourceIds).collect(Collectors.toSet());
        List<FusionResource> fusionResources = resourceRepository.selectFusionResourceByResourceIds(resourceIdArray);
        Map<String, DataSet> dataSetMap = dataSetService.getByIds(resourceIdArray).stream().collect(Collectors.toMap(DataSet::getId, Function.identity()));
        Set<Long> ids = fusionResources.stream().map(FusionResource::getId).collect(Collectors.toSet());
        Map<Long, List<CopyResourceFieldDto>> fieldMap = resourceRepository.selectFusionResourceFieldByIds(ids).stream().map(DataResourceConvert::fusionResourceFieldConvertCopyResourceFieldDto).collect(Collectors.groupingBy(CopyResourceFieldDto::getResourceId));
        return BaseResultEntity.success(fusionResources.stream().map(d -> DataResourceConvert.FusionResourceConvertCopyResourceDto(d, fieldMap.get(d.getId()), dataSetMap.get(d.getResourceId()))).collect(Collectors.toList()));
    }

    public BaseResultEntity getTestDataSet(){
        return BaseResultEntity.success(dataSetRepository.getTestDataSet());
    }

    public BaseResultEntity batchSaveTestDataSet(Map<String,String> dataSets){
        try {
            List<DataSet> dataSetList = JSONArray.parseArray(dataSets.get("dataSets"), DataSet.class);
            for (DataSet dataSet : dataSetList) {
                dataSet.setHolder(1);
                dataSetService.saveDataSet(dataSet);
            }
            return BaseResultEntity.success();
        }catch (Exception e){
            e.printStackTrace();
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }

    }
}
