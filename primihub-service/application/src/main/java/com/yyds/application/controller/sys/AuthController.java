package com.yyds.application.controller.sys;

import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.base.BaseResultEnum;
import com.yyds.biz.entity.sys.enumeration.AuthTypeEnum;
import com.yyds.biz.entity.sys.param.AlterAuthNodeStatusParam;
import com.yyds.biz.entity.sys.param.CreateAuthNodeParam;
import com.yyds.biz.service.sys.SysAuthService;
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
        if(createAuthNodeParam.getAuthName()==null||createAuthNodeParam.getAuthName().trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"authName");
        if(createAuthNodeParam.getAuthCode()==null||createAuthNodeParam.getAuthCode().trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"authCode");
        if(createAuthNodeParam.getPAuthId()==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"pAuthId");
        if(createAuthNodeParam.getPAuthId()<0)
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"pAuthId");
        if(createAuthNodeParam.getAuthIndex()==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"authIndex");
        if(createAuthNodeParam.getAuthType()==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"authType");
        if(AuthTypeEnum.findByCode(createAuthNodeParam.getAuthType())==null)
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"authType");
        if(createAuthNodeParam.getIsShow()==null)
            createAuthNodeParam.setIsShow(1);
        if(createAuthNodeParam.getDataAuthCode()==null||createAuthNodeParam.getDataAuthCode().trim().equals(""))
            createAuthNodeParam.setDataAuthCode("own");
        return sysAuthService.createAuthNode(createAuthNodeParam);
    }

    @RequestMapping("alterAuthNodeStatus")
    public BaseResultEntity alterAuthNodeStatus(AlterAuthNodeStatusParam alterAuthNodeStatusParam){
        if(alterAuthNodeStatusParam.getAuthId()==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"authId");
        if((alterAuthNodeStatusParam.getAuthName()==null|| alterAuthNodeStatusParam.getAuthName().trim().equals(""))
                &&(alterAuthNodeStatusParam.getAuthCode()==null|| alterAuthNodeStatusParam.getAuthCode().trim().equals(""))
                &&(alterAuthNodeStatusParam.getDataAuthCode()==null|| alterAuthNodeStatusParam.getDataAuthCode().trim().equals(""))
                && alterAuthNodeStatusParam.getAuthType()==null
                && alterAuthNodeStatusParam.getIsShow()==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"至少需要一个修改的字段");
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

}
