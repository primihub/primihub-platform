package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.enumeration.VerificationCodeEnum;
import com.primihub.biz.entity.sys.param.*;
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
        if(saveOrUpdateUserParam.getRegisterType()==null){
            saveOrUpdateUserParam.setRegisterType(1);
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

    @RequestMapping("register")
    public BaseResultEntity register(SaveOrUpdateUserParam saveOrUpdateUserParam){
        if(saveOrUpdateUserParam.getVerificationCode()==null||saveOrUpdateUserParam.getVerificationCode().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"verificationCode");
        if(saveOrUpdateUserParam.getPassword()==null||saveOrUpdateUserParam.getPassword().trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"password");
        if(saveOrUpdateUserParam.getPasswordAgain()==null||saveOrUpdateUserParam.getPasswordAgain().trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"passwordAgain");
        if(!saveOrUpdateUserParam.getPasswordAgain().equals(saveOrUpdateUserParam.getPassword()))
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"password not match");
        if(saveOrUpdateUserParam.getRegisterType()==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"registerType");
        saveOrUpdateUserParam.setUserId(null);

        saveOrUpdateUserParam.setRoleIdList(new Long[]{1000L});
        if(!sysUserService.validateVerificationCode(VerificationCodeEnum.REGISTER.getCode(),saveOrUpdateUserParam.getUserAccount(),saveOrUpdateUserParam.getVerificationCode()))
            return BaseResultEntity.failure(BaseResultEnum.VERIFICATION_CODE);

        return sysUserService.saveOrUpdateUser(saveOrUpdateUserParam);
    }

    @RequestMapping("sendVerificationCode")
    public BaseResultEntity sendVerificationCode(SendVerificationCodeParam sendVerificationCodeParam){
        if((sendVerificationCodeParam.getCellphone()==null||sendVerificationCodeParam.getCellphone().equals(""))&&(sendVerificationCodeParam.getEmail()==null||sendVerificationCodeParam.getEmail().equals("")))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"cellphone or email");
        if(sendVerificationCodeParam.getCodeType()==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"codeType");
        if(!VerificationCodeEnum.COPY_MAP.containsKey(sendVerificationCodeParam.getCodeType()))
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"codeType");
        return sysUserService.sendVerificationCode(sendVerificationCodeParam);
    }

    @RequestMapping("forgetPassword")
    public BaseResultEntity forgetPassword(ForgetPasswordParam forgetPasswordParam){
        if(forgetPasswordParam.getUserAccount()==null||forgetPasswordParam.getUserAccount().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userAccount");
        if(forgetPasswordParam.getVerificationCode()==null||forgetPasswordParam.getVerificationCode().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"verificationCode");
        if(forgetPasswordParam.getPassword()==null||forgetPasswordParam.getPassword().trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"password");
        if(forgetPasswordParam.getPasswordAgain()==null||forgetPasswordParam.getPasswordAgain().trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"passwordAgain");
        if(!forgetPasswordParam.getPasswordAgain().equals(forgetPasswordParam.getPassword()))
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"password not match");

        if(!sysUserService.validateVerificationCode(VerificationCodeEnum.FORGET_PASSWORD.getCode(),forgetPasswordParam.getUserAccount(),forgetPasswordParam.getVerificationCode()))
            return BaseResultEntity.failure(BaseResultEnum.VERIFICATION_CODE);

        return sysUserService.forgetPassword(forgetPasswordParam);
    }

}
