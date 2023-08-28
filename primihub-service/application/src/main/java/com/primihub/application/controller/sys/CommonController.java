package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.service.sys.SysCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("common")
@RestController
public class CommonController {

    @Autowired
    private SysCommonService sysCommonService;
    @Value("${trackingID:null}")
    private String trackingID;

    @RequestMapping("getValidatePublicKey")
    public BaseResultEntity getValidatePublicKey(){
        return sysCommonService.getValidatePublicKey();
    }
    @RequestMapping("getTrackingID")
    public BaseResultEntity getTrackingID(){
        return BaseResultEntity.success(trackingID);
    }

    @RequestMapping("getCollectList")
    public BaseResultEntity getCollectList(){
        return sysCommonService.getCollectList();
    }

}
