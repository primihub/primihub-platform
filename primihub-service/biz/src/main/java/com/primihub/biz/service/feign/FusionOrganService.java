package com.primihub.biz.service.feign;

import com.primihub.biz.entity.base.BaseResultEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

//@FeignClient(name = "fusion",contextId = "organ",url = "http://192.168.99.13:32132")
@FeignClient(name = "fusion",contextId = "organ")
public interface FusionOrganService {

    @RequestMapping("/fusion/healthConnection")
    BaseResultEntity healthConnection();

    @RequestMapping("/fusion/organData")
    BaseResultEntity organData(@RequestParam("organId")String organId, @RequestParam("organName")String organName);
}
