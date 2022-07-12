package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.param.FindUserPageParam;
import com.primihub.biz.entity.sys.param.LoginParam;
import com.primihub.biz.entity.sys.param.SaveOrUpdateUserParam;
import com.primihub.biz.service.sys.SysUserService;
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
        if(findUserPageParam.getRoleId()!=null&&findUserPageParam.getRoleId()<0)
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"roleId");
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

    @RequestMapping("findUserByAccount")
    public BaseResultEntity findUserByAccount(String userAccount){
        if(userAccount==null||userAccount.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userAccount");
        return sysUserService.findUserByAccount(userAccount);
    }

}
