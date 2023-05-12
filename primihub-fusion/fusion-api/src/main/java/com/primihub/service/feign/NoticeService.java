package com.primihub.service.feign;

import com.primihub.entity.base.BaseResultEntity;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "platform",contextId = "resource")
public interface NoticeService {

    @PostMapping("/resource/noticeResource")
    BaseResultEntity noticeResource(String resourceId);
}
