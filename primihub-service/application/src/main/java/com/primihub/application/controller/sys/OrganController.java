package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.sys.param.ChangeLocalOrganInfoParam;
import com.primihub.biz.service.sys.SysOrganService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("organ")
@RestController
public class OrganController {

    @Autowired
    private SysOrganService sysOrganService;

    @RequestMapping("getLocalOrganInfo")
    public BaseResultEntity getLocalOrganInfo(){
        return sysOrganService.getLocalOrganInfo();
    }

    @RequestMapping("changeLocalOrganInfo")
    public BaseResultEntity changeLocalOrganInfo(ChangeLocalOrganInfoParam changeLocalOrganInfoParam){
        return sysOrganService.changeLocalOrganInfo(changeLocalOrganInfoParam);
    }
}
