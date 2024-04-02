package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.enumeration.AuthTypeEnum;
import com.primihub.biz.entity.sys.param.AlterAuthNodeStatusParam;
import com.primihub.biz.entity.sys.param.CreateAuthNodeParam;
import com.primihub.biz.service.sys.SysAuthService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "权限接口",tags = "权限接口")
@RequestMapping("auth")
@RestController
public class AuthController {

    @Autowired
    private SysAuthService sysAuthService;

    @GetMapping("createAuthNode")
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

    @GetMapping("alterAuthNodeStatus")
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

    @GetMapping("deleteAuthNode")
    public BaseResultEntity deleteAuthNode(Long authId){
        return sysAuthService.deleteAuthNode(authId);
    }

    @GetMapping("getAuthTree")
    public BaseResultEntity getAuthTree(){
        return sysAuthService.getAuthTree();
    }

    @GetMapping("generateAllAuth")
    public BaseResultEntity generateAllAuth(){
        return sysAuthService.generateAllAuth();
    }

}
