package com.primihub.controller;

import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.entity.copy.dto.CopyResourceDto;
import com.primihub.entity.resource.param.ResourceParam;
import com.primihub.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RequestMapping("fusionResource")
@RestController
public class FusionResourceController {

    @Autowired
    private ResourceService resourceService;

    @RequestMapping("getResourceList")
    public BaseResultEntity getResourceList(ResourceParam resourceParam){
        return resourceService.getResourceList(resourceParam);
    }

    @RequestMapping("getResourceListById")
    public BaseResultEntity getResourceListById(String[] resourceIdArray,String globalId){
        if(resourceIdArray==null||resourceIdArray.length==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceIdArray");
        }
        return resourceService.getResourceListById(resourceIdArray,globalId);
    }
    @RequestMapping("getCopyResource")
    public BaseResultEntity getCopyResource(Set<String> resourceIds){
        if(resourceIds==null||resourceIds.size()==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceIds");
        }
        return resourceService.getCopyResource(resourceIds);
    }

    @RequestMapping("getResourceTagList")
    public BaseResultEntity getResourceTagList(){
        return resourceService.getResourceTagList();
    }

    @RequestMapping("getDataResource")
    public BaseResultEntity getDataResource(String resourceId,String globalId){
        if (StringUtils.isEmpty(resourceId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        return resourceService.getDataResource(resourceId,globalId);
    }
    @PostMapping("saveResource")
    public BaseResultEntity saveResource(String globalId, List<CopyResourceDto> copyResourceDtoList){
        if (StringUtils.isEmpty(globalId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"globalId");
        }
        if (copyResourceDtoList==null || copyResourceDtoList.size()==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"copyResourceDtoList");
        }
        return resourceService.batchSaveResource(globalId,copyResourceDtoList);
    }

}
