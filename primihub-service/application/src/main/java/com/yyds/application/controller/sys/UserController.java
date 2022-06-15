package com.yyds.application.controller.sys;

import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.base.BaseResultEnum;
import com.yyds.biz.entity.sys.param.FindUserPageParam;
import com.yyds.biz.entity.sys.param.LoginParam;
import com.yyds.biz.entity.sys.param.SaveOrUpdateUserParam;
import com.yyds.biz.service.sys.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("user")
@RestController
public class UserController {

    @Autowired
    private SysUserService sysUserService;

    @RequestMapping("login")
    public BaseResultEntity login(LoginParam loginParam){
        if(loginParam.getUserAccount()==null||loginParam.getUserAccount().trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userAccount");
        if(loginParam.getUserPassword()==null||loginParam.getUserPassword().trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userPassword");
        if(loginParam.getValidateKeyName()==null||loginParam.getValidateKeyName().trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"validateKeyName");
        return sysUserService.login(loginParam);
    }

    @RequestMapping("logout")
    public BaseResultEntity logout(@RequestHeader(value = "token",defaultValue = "") String token,@RequestHeader(value = "userId",defaultValue = "-1") Long userId){
        return sysUserService.logout(token,userId);
    }

    @RequestMapping("saveOrUpdateUser")
    public BaseResultEntity saveOrUpdateUser(SaveOrUpdateUserParam saveOrUpdateUserParam){
        if(saveOrUpdateUserParam.getUserId()!=null){
            if(saveOrUpdateUserParam.getUserId()<0)
                return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"userId");
        }
        if((saveOrUpdateUserParam.getOrganIdList()==null&&saveOrUpdateUserParam.getROrganIdList()!=null)
                ||(saveOrUpdateUserParam.getOrganIdList()!=null&&saveOrUpdateUserParam.getROrganIdList()==null)
                ||(saveOrUpdateUserParam.getOrganIdList()!=null&&saveOrUpdateUserParam.getROrganIdList()!=null&&saveOrUpdateUserParam.getOrganIdList().length!=saveOrUpdateUserParam.getROrganIdList().length))
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"organIdList和rOrganIdList");
        return sysUserService.saveOrUpdateUser(saveOrUpdateUserParam);
    }

    @RequestMapping("deleteSysUser")
    public BaseResultEntity deleteSysUser(Long userId){
        if(userId==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        if(userId<0)
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"userId");
        return sysUserService.deleteSysUser(userId);
    }

    @RequestMapping("findUserPage")
    public BaseResultEntity findUserPage(FindUserPageParam findUserPageParam,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10")Integer pageSize){
        if(findUserPageParam.getOrganId()!=null&&findUserPageParam.getOrganId()<0)
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"organId");
        if(findUserPageParam.getRoleId()!=null&&findUserPageParam.getRoleId()<0)
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"roleId");
        if(findUserPageParam.getROrganId()!=null&&findUserPageParam.getROrganId()<0)
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"rOrganId");
        return sysUserService.findUserPage(findUserPageParam,pageNum,pageSize);
    }

    @RequestMapping("initPassword")
    public BaseResultEntity initPassword(Long userId){
        if(userId==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        if(userId<0)
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"userId");
        return sysUserService.initPassword(userId);
    }

    @RequestMapping("updatePassword")
    public BaseResultEntity updatePassword(@RequestHeader("userId") Long userId,
                                           String password,
                                           String validateKeyName){
        if(userId==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        if(userId<0)
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"userId");
        if(password==null||password.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userPassword");
        if(validateKeyName==null||validateKeyName.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"validateKeyName");
        return sysUserService.updatePassword(userId,password,validateKeyName);
    }

}
