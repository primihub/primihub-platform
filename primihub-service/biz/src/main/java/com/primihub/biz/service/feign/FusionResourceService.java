package com.primihub.biz.service.feign;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.fusion.param.OrganResourceParam;
import com.primihub.biz.entity.fusion.param.ResourceParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@FeignClient(name = "fusion",contextId = "resource")
public interface FusionResourceService {
    @GetMapping("/fusionResource/getResourceList")
    BaseResultEntity getResourceList(ResourceParam resourceParam);

    @GetMapping("/fusionResource/getResourceListById")
    BaseResultEntity getResourceListById(@RequestParam("resourceIdArray") List<String> resourceIdArray, @RequestParam("globalId") String globalId);

    @GetMapping("/fusionResource/getResourceTagList")
    BaseResultEntity getResourceTagList();

    @GetMapping("/fusionResource/getDataResource")
    BaseResultEntity getDataResource(@RequestParam("resourceId") String resourceId,@RequestParam("globalId") String globalId);

    @PostMapping("/fusionResource/saveOrganResourceAuth")
    BaseResultEntity saveOrganResourceAuth(@RequestParam("organId") String organId,@RequestParam("resourceId") String resourceId,@RequestParam("projectId") String projectId,@RequestParam("auditStatus") Integer auditStatus);

    @GetMapping("/fusionResource/getOrganResourceList")
    BaseResultEntity getOrganResourceList(OrganResourceParam param);

    @PostMapping("/copy/batchSave")
    BaseResultEntity batchSave(@RequestParam("globalId")String globalId,@RequestParam("copyPart")String copyPart);

    @PostMapping("/fusionResource/saveResource")
    BaseResultEntity saveResource(@RequestParam("globalId") String globalId,@RequestParam("copyResourceDtoList") String copyResourceDtoList);

    @GetMapping("/fusionResource/getCopyResource")
    BaseResultEntity getCopyResource(@RequestParam("resourceIds")Set<String> resourceIds);

    @GetMapping("/fusionResource/getTestDataSet")
    BaseResultEntity getTestDataSet();

    @PostMapping("/fusionResource/batchSaveTestDataSet")
    BaseResultEntity batchSaveTestDataSet(@RequestBody Map<String,String> dataSets);
}
