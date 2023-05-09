package com.primihub.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONValidator;
import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.entity.copy.dto.CopyResourceDto;
import com.primihub.entity.resource.param.ResourceParam;
import com.primihub.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequestMapping("fusionResource")
@RestController
public class FusionResourceController {

    @Autowired
    private ResourceService resourceService;

    @GetMapping("getResourceList")
    public BaseResultEntity getResourceList(ResourceParam resourceParam){
        return resourceService.getResourceList(resourceParam);
    }

    @GetMapping("getResourceListById")
    public BaseResultEntity getResourceListById(String[] resourceIdArray,String globalId){
        if(resourceIdArray==null||resourceIdArray.length==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceIdArray");
        }
        return resourceService.getResourceListById(resourceIdArray,globalId);
    }
    @GetMapping("getCopyResource")
    public BaseResultEntity getCopyResource(String[] resourceIds){
        if(resourceIds==null||resourceIds.length==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceIds");
        }
        return resourceService.getCopyResource(resourceIds);
    }

    @GetMapping("getResourceTagList")
    public BaseResultEntity getResourceTagList(){
        return resourceService.getResourceTagList();
    }

    @GetMapping("getDataResource")
    public BaseResultEntity getDataResource(String resourceId,String globalId){
        if (StringUtils.isEmpty(resourceId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        return resourceService.getDataResource(resourceId,globalId);
    }
    @PostMapping("saveResource")
    public BaseResultEntity saveResource(String globalId, String copyResourceDtoList){
        if (StringUtils.isEmpty(globalId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"globalId");
        }
        if (StringUtils.isEmpty(copyResourceDtoList)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"copyResourceDtoList");
        }
        try {
            List<CopyResourceDto> list = JSONArray.parseArray(copyResourceDtoList,CopyResourceDto.class);
            if (list==null || list.size()==0) {
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"copyResourceDtoList");
            }
            return resourceService.batchSaveResource(globalId,list);
        }catch (Exception e){
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }
    }

    @GetMapping("getTestDataSet")
    public BaseResultEntity getTestDataSet(){
        return resourceService.getTestDataSet();
    }

    @PostMapping("batchSaveTestDataSet")
    public BaseResultEntity batchSaveTestDataSet(@RequestBody Map<String,String> dataSets){
        if (dataSets == null || !dataSets.containsKey("dataSets")) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"shareData - dataSets");
        }
        return resourceService.batchSaveTestDataSet(dataSets);
    }

}
