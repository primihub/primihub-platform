package com.primihub.biz.feign;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.fusion.param.OrganResourceParam;
import com.primihub.biz.entity.fusion.param.ResourceParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@FeignClient(name = "fusion",contextId = "resource")
public interface FusionResourceService {
    @RequestMapping("/fusionResource/getResourceList")
    BaseResultEntity getResourceList(ResourceParam resourceParam);

    @RequestMapping("/fusionResource/getResourceListById")
    BaseResultEntity getResourceListById(List<String> resourceIdArray, String globalId);

    @RequestMapping("/fusionResource/getResourceTagList")
    BaseResultEntity getResourceTagList();

    @RequestMapping("/fusionResource/getDataResource")
    BaseResultEntity getDataResource(String resourceId,String globalId);

    @RequestMapping("/fusionResource/saveOrganResourceAuth")
    BaseResultEntity saveOrganResourceAuth(String organId,String resourceId,String projectId,Integer auditStatus);

    @RequestMapping("/fusionResource/getOrganResourceList")
    BaseResultEntity getOrganResourceList(OrganResourceParam param);
}
