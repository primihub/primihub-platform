package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.param.ChangeLocalOrganInfoParam;
import com.primihub.biz.service.sys.SysOrganService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 机构
 */
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

    @RequestMapping("changeHomepage")
    public BaseResultEntity changeHomepage(@RequestBody Map<String,Object> homeMap){
        homeMap.remove("token");
        homeMap.remove("timestamp");
        homeMap.remove("nonce");
        if (homeMap.size()==0) {
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"无参数");
        }
        return sysOrganService.changeHomepage(homeMap);
    }

    @RequestMapping("getHomepage")
    public BaseResultEntity getHomepage(){
        return sysOrganService.getHomepage();
    }
}
