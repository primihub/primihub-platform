package com.primihub.biz.feign;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.fusion.param.FusionConnectionParam;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "fusion",contextId = "organ")
public interface FusionOrganService {

    @RequestMapping("/fusion/healthConnection")
    BaseResultEntity healthConnection();
}
