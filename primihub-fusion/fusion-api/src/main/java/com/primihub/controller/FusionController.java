package com.primihub.controller;

import com.primihub.entity.base.BaseResultEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("fusion")
@RestController
public class FusionController {

    @RequestMapping("healthConnection")
    public BaseResultEntity healthConnection(){
        return BaseResultEntity.success(System.currentTimeMillis());
    }


}
