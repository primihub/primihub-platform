package com.primihub.biz.service.feign;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.vo.DataResourceCopyVo;
import com.primihub.biz.entity.fusion.param.OrganResourceParam;
import com.primihub.biz.entity.fusion.param.ResourceParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@FeignClient(name = "fusion",contextId = "resource")
public interface FusionResourceService {
    @RequestMapping("/fusionResource/getResourceList")
    BaseResultEntity getResourceList(ResourceParam resourceParam);

    @RequestMapping("/fusionResource/getResourceListById")
    BaseResultEntity getResourceListById(@RequestParam("resourceIdArray") List<String> resourceIdArray, @RequestParam("globalId") String globalId);

    @RequestMapping("/fusionResource/getResourceTagList")
    BaseResultEntity getResourceTagList();

    @RequestMapping("/fusionResource/getDataResource")
    BaseResultEntity getDataResource(@RequestParam("resourceId") String resourceId,@RequestParam("globalId") String globalId);

    @RequestMapping("/fusionResource/saveOrganResourceAuth")
    BaseResultEntity saveOrganResourceAuth(@RequestParam("organId") String organId,@RequestParam("resourceId") String resourceId,@RequestParam("projectId") String projectId,@RequestParam("auditStatus") Integer auditStatus);

    @RequestMapping("/fusionResource/getOrganResourceList")
    BaseResultEntity getOrganResourceList(OrganResourceParam param);

    @PostMapping("/copy/batchSave")
    BaseResultEntity batchSave(@RequestParam("globalId")String globalId,@RequestParam("copyPart")String copyPart);

    @PostMapping("/fusionResource/saveResource")
    BaseResultEntity saveResource(@RequestParam("globalId") String globalId,@RequestParam("copyResourceDtoList") String copyResourceDtoList);

    @RequestMapping("/fusionResource/getCopyResource")
    BaseResultEntity getCopyResource(@RequestParam("resourceIds")Set<String> resourceIds);
}
