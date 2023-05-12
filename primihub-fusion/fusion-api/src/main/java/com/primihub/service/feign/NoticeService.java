package com.primihub.service.feign;

import com.primihub.entity.base.BaseResultEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "platform",contextId = "resource")
public interface NoticeService {

    @PostMapping("/resource/noticeResource")
    BaseResultEntity noticeResource(@RequestParam("resourceId") String resourceId);
    @GetMapping("/testDataSet")
    BaseResultEntity testDataSet(@RequestParam("id") String id);
}
