package com.primihub.controller;

import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.entity.resource.param.OrganResourceParam;
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
    public BaseResultEntity getResourceListById(String[] resourceIdArray,String globalId){
        if(resourceIdArray==null||resourceIdArray.length==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceIdArray");
        }
        return resourceService.getResourceListById(resourceIdArray,globalId);
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
    @RequestMapping("saveOrganResourceAuth")
    public BaseResultEntity saveOrganResourceAuth(String organId,String resourceId,String projectId,Integer auditStatus){
        if (StringUtils.isEmpty(organId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        }
        if (StringUtils.isEmpty(resourceId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        if (StringUtils.isEmpty(projectId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"projectId");
        }
        if (auditStatus==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"auditStatus");
        }
        return resourceService.saveOrganResourceAuth(organId,resourceId,projectId,auditStatus);
    }

    @RequestMapping("getOrganResourceList")
    public BaseResultEntity getOrganResourceList(OrganResourceParam param){
        if (StringUtils.isEmpty(param.getOrganId())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        }
        return resourceService.getOrganResourceList(param);
    }


}
