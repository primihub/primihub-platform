package com.primihub.controller;

import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.entity.resource.param.ResourceParam;
import com.primihub.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public BaseResultEntity getResourceListById(String[] resourceIdArray){
        if(resourceIdArray==null||resourceIdArray.length==0)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceIdArray");
        return resourceService.getResourceListById(resourceIdArray);
    }

    @RequestMapping("getResourceTagList")
    public BaseResultEntity getResourceTagList(){
        return resourceService.getResourceTagList();
    }

    @RequestMapping("getDataResource")
    public BaseResultEntity getDataResource(String resourceId){
        if (StringUtils.isEmpty(resourceId))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        return resourceService.getDataResource(resourceId);
    }

    public BaseResultEntity saveOrganResourceAuth(String organId,String resourceId,String projectId,Integer auditStatus){
        if (StringUtils.isEmpty(organId))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        if (StringUtils.isEmpty(resourceId))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        if (StringUtils.isEmpty(projectId))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"projectId");
        if (auditStatus==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"auditStatus");
        return resourceService.saveOrganResourceAuth(organId,resourceId,projectId,auditStatus);
    }


}
