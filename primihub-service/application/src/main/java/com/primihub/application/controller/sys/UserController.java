package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.enumeration.VerificationCodeEnum;
import com.primihub.biz.entity.sys.param.*;
import com.primihub.biz.service.sys.SysUserService;
import io.swagger.annotations.Api;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@Api(value = "用户接口",tags = "用户接口")
@RequestMapping("user")
@RestController
public class UserController {

    @Autowired
    private SysUserService sysUserService;
    @Value("${primihub.interior.code:null}")
    private String interiorCode;

    @PostMapping("login")
    public BaseResultEntity login(LoginParam loginParam,@RequestHeader(value = "ip",defaultValue = "") String ip){
        if(loginParam.getUserAccount()==null|| "".equals(loginParam.getUserAccount().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userAccount");
        }
        if(loginParam.getUserPassword()==null|| "".equals(loginParam.getUserPassword().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userPassword");
        }
        if(loginParam.getValidateKeyName()==null|| "".equals(loginParam.getValidateKeyName().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"validateKeyName");
        }
        return sysUserService.login(loginParam,ip);
    }

    @GetMapping("logout")
    public BaseResultEntity logout(@RequestHeader(value = "token",defaultValue = "") String token,@RequestHeader(value = "userId",defaultValue = "-1") Long userId){
        return sysUserService.logout(token,userId);
    }

    @PostMapping("saveOrUpdateUser")
    public BaseResultEntity saveOrUpdateUser(SaveOrUpdateUserParam saveOrUpdateUserParam){
        if(saveOrUpdateUserParam.getUserId()!=null){
            if(saveOrUpdateUserParam.getUserId()<0) {
                return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"userId");
            }
        }
        if(saveOrUpdateUserParam.getRegisterType()==null){
            saveOrUpdateUserParam.setRegisterType(1);
        }
        return sysUserService.saveOrUpdateUser(saveOrUpdateUserParam);
    }

    @PostMapping("deleteSysUser")
    public BaseResultEntity deleteSysUser(Long userId){
        if(userId==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        if(userId<0) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"userId");
        }
        return sysUserService.deleteSysUser(userId);
    }

    @GetMapping("findUserPage")
    public BaseResultEntity findUserPage(FindUserPageParam findUserPageParam,
                                         @RequestParam(defaultValue = "1") Integer pageNum,
                                         @RequestParam(defaultValue = "10")Integer pageSize){
        if(findUserPageParam.getRoleId()!=null&&findUserPageParam.getRoleId()<0) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"roleId");
        }
        return sysUserService.findUserPage(findUserPageParam,pageNum,pageSize);
    }

    @PostMapping("initPassword")
    public BaseResultEntity initPassword(Long userId){
        if(userId==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        if(userId<0) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"userId");
        }
        return sysUserService.initPassword(userId);
    }

    @PostMapping("updatePassword")
    public BaseResultEntity updatePassword(@RequestHeader("userId") Long userId,
                                           String password,
                                           String validateKeyName){
        if(userId==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        if(userId<0) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"userId");
        }
        if(password==null|| "".equals(password.trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userPassword");
        }
        if(validateKeyName==null|| "".equals(validateKeyName.trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"validateKeyName");
        }
        return sysUserService.updatePassword(userId,password,validateKeyName);
    }

    @GetMapping("findUserByAccount")
    public BaseResultEntity findUserByAccount(String userAccount){
        if(userAccount==null|| "".equals(userAccount.trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userAccount");
        }
        return sysUserService.findUserByAccount(userAccount);
    }

    @PostMapping("register")
    public BaseResultEntity register(SaveOrUpdateUserParam saveOrUpdateUserParam){
        if(saveOrUpdateUserParam.getVerificationCode()==null|| "".equals(saveOrUpdateUserParam.getVerificationCode())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"verificationCode");
        }
        if(saveOrUpdateUserParam.getPassword()==null|| "".equals(saveOrUpdateUserParam.getPassword().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"password");
        }
        if(saveOrUpdateUserParam.getPasswordAgain()==null|| "".equals(saveOrUpdateUserParam.getPasswordAgain().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"passwordAgain");
        }
        if(!saveOrUpdateUserParam.getPasswordAgain().equals(saveOrUpdateUserParam.getPassword())) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"password not match");
        }
        if(saveOrUpdateUserParam.getRegisterType()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"registerType");
        }
        saveOrUpdateUserParam.setUserId(null);
        saveOrUpdateUserParam.setRoleIdList(new Long[]{1000L});
        if (StringUtils.isBlank(interiorCode) || !saveOrUpdateUserParam.getVerificationCode().equals(interiorCode) ){
            if(!sysUserService.validateVerificationCode(VerificationCodeEnum.REGISTER.getCode(),saveOrUpdateUserParam.getUserAccount(),saveOrUpdateUserParam.getVerificationCode())) {
                return BaseResultEntity.failure(BaseResultEnum.VERIFICATION_CODE);
            }
        }
        return sysUserService.saveOrUpdateUser(saveOrUpdateUserParam);
    }

    @PostMapping("sendVerificationCode")
    public BaseResultEntity sendVerificationCode(SendVerificationCodeParam sendVerificationCodeParam){
        if((sendVerificationCodeParam.getCellphone()==null|| "".equals(sendVerificationCodeParam.getCellphone()))&&(sendVerificationCodeParam.getEmail()==null|| "".equals(sendVerificationCodeParam.getEmail()))) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"cellphone or email");
        }
        if(sendVerificationCodeParam.getCodeType()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"codeType");
        }
        if(!VerificationCodeEnum.COPY_MAP.containsKey(sendVerificationCodeParam.getCodeType())) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"codeType");
        }
        return sysUserService.sendVerificationCode(sendVerificationCodeParam);
    }

    @PostMapping("forgetPassword")
    public BaseResultEntity forgetPassword(ForgetPasswordParam forgetPasswordParam){
        if(forgetPasswordParam.getUserAccount()==null|| "".equals(forgetPasswordParam.getUserAccount())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userAccount");
        }
        if(forgetPasswordParam.getVerificationCode()==null|| "".equals(forgetPasswordParam.getVerificationCode())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"verificationCode");
        }
        if(forgetPasswordParam.getPassword()==null|| "".equals(forgetPasswordParam.getPassword().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"password");
        }
        if(forgetPasswordParam.getPasswordAgain()==null|| "".equals(forgetPasswordParam.getPasswordAgain().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"passwordAgain");
        }
        if(!forgetPasswordParam.getPasswordAgain().equals(forgetPasswordParam.getPassword())) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"password not match");
        }

        if (StringUtils.isBlank(interiorCode) || !forgetPasswordParam.getVerificationCode().equals(interiorCode) ) {
            if (!sysUserService.validateVerificationCode(VerificationCodeEnum.FORGET_PASSWORD.getCode(), forgetPasswordParam.getUserAccount(), forgetPasswordParam.getVerificationCode())) {
                return BaseResultEntity.failure(BaseResultEnum.VERIFICATION_CODE);
            }
        }
        return sysUserService.forgetPassword(forgetPasswordParam);
    }
    @PostMapping("changeUserAccount")
    public BaseResultEntity changeUserAccount(SaveOrUpdateUserParam param,@RequestHeader("userId") Long userId){
        if (userId == null || userId<=0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
//        if (param.getRegisterType() != 2 || param.getRegisterType() != 3)
//            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"registerType");
        if (param.getUserAccount()==null || "".equals(param.getUserAccount().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"UserAccount");
        }
        if (param.getVerificationCode()==null || "".equals(param.getVerificationCode().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"verificationCode");
        }
        if (StringUtils.isBlank(interiorCode) || !param.getVerificationCode().equals(interiorCode) ) {
            if (!sysUserService.validateVerificationCode(VerificationCodeEnum.CHANGE_ACCOUNT.getCode(), param.getUserAccount(), param.getVerificationCode())) {
                return BaseResultEntity.failure(BaseResultEnum.VERIFICATION_CODE);
            }
        }
        param.setUserId(userId);
        return sysUserService.changeUserAccount(param);
    }

    @PostMapping("relieveUserAccount")
    public BaseResultEntity relieveUserAccount(@RequestHeader("userId") Long userId){
        if (userId == null || userId<=0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        return sysUserService.relieveUserAccount(userId);
    }

}
