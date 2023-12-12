package com.primihub.biz.service.feign;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.vo.DataResourceCopyVo;
import com.primihub.biz.entity.fusion.param.OrganResourceParam;
import com.primihub.biz.entity.fusion.param.ResourceParam;
import com.primihub.biz.entity.sys.po.DataSet;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;
import java.util.Set;

//@FeignClient(name = "fusion",contextId = "resource",url = "http://192.168.99.13:32132")
@FeignClient(name = "fusion",contextId = "resource")
public interface FusionResourceService {
    @RequestMapping("/fusionResource/getResourceList")
    BaseResultEntity getResourceList(@RequestBody ResourceParam resourceParam);

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
    BaseResultEntity saveResource(@RequestParam("globalId") String globalId,@RequestBody List<DataResourceCopyVo> copyResourceDtoList);

    @RequestMapping("/fusionResource/getCopyResource")
    BaseResultEntity getCopyResource(@RequestParam("resourceIds")Set<String> resourceIds);

    @RequestMapping("/fusionResource/getTestDataSet")
    BaseResultEntity getTestDataSet(@RequestParam("id")String id);

    @RequestMapping("/fusionResource/getDataSets")
    BaseResultEntity getDataSets(@RequestBody Set<String> id);

    @PostMapping("/fusionResource/batchSaveTestDataSet")
    BaseResultEntity batchSaveTestDataSet(@RequestBody List<DataSet> dataSets);
}
