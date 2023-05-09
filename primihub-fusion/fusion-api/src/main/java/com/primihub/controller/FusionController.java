package com.primihub.controller;

import com.primihub.entity.base.BaseResultEntity;
import com.primihub.service.OrganService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("fusion")
@RestController
public class FusionController {
    @Autowired
    private OrganService organService;

    @RequestMapping("healthConnection")
    public BaseResultEntity healthConnection(){
        return BaseResultEntity.success(System.currentTimeMillis());
    }

    @RequestMapping("organData")
    public BaseResultEntity organData(String organId, String organName){
        return organService.organData(organId,organName);
    }
}
