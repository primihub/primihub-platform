package com.primihub.biz.service.feign;

import com.primihub.biz.entity.base.BaseResultEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = "fusion",contextId = "organ")
public interface FusionOrganService {

    @RequestMapping("/fusion/healthConnection")
    BaseResultEntity healthConnection();
}
