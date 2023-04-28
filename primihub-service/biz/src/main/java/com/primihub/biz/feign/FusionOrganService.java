package com.primihub.biz.feign;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.fusion.param.FusionConnectionParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "fusion",contextId = "organ")
public interface FusionOrganService {

    @RequestMapping("/fusion/registerConnection")
    BaseResultEntity registerConnection(FusionConnectionParam fusionConnectionParam);

    @RequestMapping("/fusion/changeConnection")
    BaseResultEntity changeConnection(FusionConnectionParam fusionConnectionParam);

    @RequestMapping("/fusion/findOrganByGlobalId")
    BaseResultEntity findOrganByGlobalId(String[] globalIdArray);

    @RequestMapping("/fusion/getOrganAllList")
    BaseResultEntity getOrganAllList();
}
