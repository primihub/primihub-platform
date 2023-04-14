package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.enumeration.AuthTypeEnum;
import com.primihub.biz.entity.sys.param.AlterAuthNodeStatusParam;
import com.primihub.biz.entity.sys.param.CreateAuthNodeParam;
import com.primihub.biz.service.sys.SysAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("auth")
@RestController
public class AuthController {

    @Autowired
    private SysAuthService sysAuthService;

    @RequestMapping("createAuthNode")
    public BaseResultEntity createAuthNode(CreateAuthNodeParam createAuthNodeParam){
        if(createAuthNodeParam.getAuthName()==null|| "".equals(createAuthNodeParam.getAuthName().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"authName");
        }
        if(createAuthNodeParam.getAuthCode()==null|| "".equals(createAuthNodeParam.getAuthCode().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"authCode");
        }
        if(createAuthNodeParam.getPAuthId()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"pAuthId");
        }
        if(createAuthNodeParam.getPAuthId()<0) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"pAuthId");
        }
        if(createAuthNodeParam.getAuthIndex()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"authIndex");
        }
        if(createAuthNodeParam.getAuthType()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"authType");
        }
        if(AuthTypeEnum.findByCode(createAuthNodeParam.getAuthType())==null) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"authType");
        }
        if(createAuthNodeParam.getIsShow()==null) {
            createAuthNodeParam.setIsShow(1);
        }
        if(createAuthNodeParam.getDataAuthCode()==null|| "".equals(createAuthNodeParam.getDataAuthCode().trim())) {
            createAuthNodeParam.setDataAuthCode("own");
        }
        return sysAuthService.createAuthNode(createAuthNodeParam);
    }

    @RequestMapping("alterAuthNodeStatus")
    public BaseResultEntity alterAuthNodeStatus(AlterAuthNodeStatusParam alterAuthNodeStatusParam){
        if(alterAuthNodeStatusParam.getAuthId()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"authId");
        }
        if((alterAuthNodeStatusParam.getAuthName()==null|| "".equals(alterAuthNodeStatusParam.getAuthName().trim()))
                &&(alterAuthNodeStatusParam.getAuthCode()==null|| "".equals(alterAuthNodeStatusParam.getAuthCode().trim()))
                &&(alterAuthNodeStatusParam.getDataAuthCode()==null|| "".equals(alterAuthNodeStatusParam.getDataAuthCode().trim()))
                && alterAuthNodeStatusParam.getAuthType()==null
                && alterAuthNodeStatusParam.getIsShow()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"至少需要一个修改的字段");
        }
        return sysAuthService.alterAuthNodeStatus(alterAuthNodeStatusParam);
    }

    @RequestMapping("deleteAuthNode")
    public BaseResultEntity deleteAuthNode(Long authId){
        return sysAuthService.deleteAuthNode(authId);
    }

    @RequestMapping("getAuthTree")
    public BaseResultEntity getAuthTree(){
        return sysAuthService.getAuthTree();
    }

    @RequestMapping("generateAllAuth")
    public BaseResultEntity generateAllAuth(){
        return sysAuthService.generateAllAuth();
    }

}
