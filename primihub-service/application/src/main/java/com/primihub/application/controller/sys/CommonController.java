package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.service.sys.SysCommonService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Api(value = "共用接口",tags = "共用接口")
@RequestMapping("common")
@RestController
public class CommonController {

    @Autowired
    private SysCommonService sysCommonService;
    @Value("${trackingID:null}")
    private String trackingID;

    @GetMapping("getValidatePublicKey")
    public BaseResultEntity getValidatePublicKey(){
        return sysCommonService.getValidatePublicKey();
    }
    @GetMapping("getTrackingID")
    public BaseResultEntity getTrackingID(){
        return BaseResultEntity.success(trackingID);
    }

    @GetMapping("getCollectList")
    public BaseResultEntity getCollectList(){
        return sysCommonService.getCollectList();
    }

}
