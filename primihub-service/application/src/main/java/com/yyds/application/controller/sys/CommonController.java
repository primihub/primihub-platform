package com.yyds.application.controller.sys;

import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.service.sys.SysCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("common")
@RestController
public class CommonController {

    @Autowired
    private SysCommonService sysCommonService;

    @RequestMapping("getValidatePublicKey")
    public BaseResultEntity getValidatePublicKey(){
        return sysCommonService.getValidatePublicKey();
    }

}
