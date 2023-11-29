package com.primihub.biz.service.sys;

import com.alibaba.fastjson.JSONObject;
import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.service.CaptchaService;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.enumeration.OAuthSourceEnum;
import com.primihub.biz.entity.sys.param.LoginParam;
import com.primihub.biz.entity.sys.param.SaveOrUpdateUserParam;
import com.primihub.biz.entity.sys.po.SysUser;
import com.primihub.biz.entity.sys.config.BaseAuthConfig;
import com.primihub.biz.repository.primaryredis.sys.SysCommonPrimaryRedisRepository;
import com.primihub.biz.repository.secondarydb.sys.SysUserSecondarydbRepository;
import com.primihub.biz.util.crypt.DateUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import lombok.extern.slf4j.Slf4j;
import me.zhyd.oauth.enums.AuthResponseStatus;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.model.AuthResponse;
import me.zhyd.oauth.model.AuthUser;
import me.zhyd.oauth.request.AuthGithubRequest;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.request.AuthWeChatOpenRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class SysOauthService {

    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private SysUserSecondarydbRepository userSecondarydbRepository;
    @Autowired
    private SysCommonPrimaryRedisRepository sysCommonPrimaryRedisRepository;
    @Autowired
    private CaptchaService captchaService;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysOAuthStateCacheService oAuthStateCacheService;

    public AuthRequest getAuthRequest(OAuthSourceEnum source){
        AuthRequest authRequest = null;
        if (source == OAuthSourceEnum.GITHUB){
            BaseAuthConfig baseAuthConfig = baseConfiguration.getAuthConfigs().get(OAuthSourceEnum.GITHUB.getSourceName());
            if (baseAuthConfig!=null){
                // RedirectUri URL encoding needs to be converted
                authRequest = new AuthGithubRequest(baseAuthConfig,oAuthStateCacheService);
            }
        }else if (source == OAuthSourceEnum.WEIXIN){
            BaseAuthConfig baseAuthConfig = baseConfiguration.getAuthConfigs().get(OAuthSourceEnum.WEIXIN.getSourceName());
            if (baseAuthConfig!=null){
                // RedirectUri URL encoding needs to be converted
                authRequest = new AuthWeChatOpenRequest(baseAuthConfig,oAuthStateCacheService);
            }
        }
        return authRequest;
    }

    public Set<String> getOauthList() {
        Set<String> set= new HashSet<>();
        if (baseConfiguration.getAuthConfigs()==null || baseConfiguration.getAuthConfigs().isEmpty()) {
            return set;
        }
        Iterator<Map.Entry<String, BaseAuthConfig>> iterator = baseConfiguration.getAuthConfigs().entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, BaseAuthConfig> next = iterator.next();
            BaseAuthConfig value = next.getValue();
            if (value.getAuthEnable()!=null && value.getAuthEnable() == 0 && StringUtils.isNotBlank(value.getRedirectUrl())) {
                set.add(next.getKey());
            }
        }
        return set;
    }

    public BaseResultEntity authDataLogin(AuthCallback callback,OAuthSourceEnum source) {
        BaseAuthConfig baseAuthConfig = baseConfiguration.getAuthConfigs().get(source.getSourceName());
        log.info(baseAuthConfig.getRedirectUrl());
        if (!getOauthList().contains(source.getSourceName())){
            return BaseResultEntity.failure(BaseResultEnum.AUTH_LOGIN,source.getSourceName()+"授权未配置,不支持登录");
        }
        AuthRequest authRequest = getAuthRequest(source);
        AuthResponse login = authRequest.login(callback);
        log.info(JSONObject.toJSONString(login));
        if (login.getCode() != AuthResponseStatus.SUCCESS.getCode()) {
            return BaseResultEntity.failure(BaseResultEnum.AUTH_LOGIN,source.getSourceName()+"授权登录失败");
        }
        AuthUser authUser = (AuthUser)login.getData();
        String authUuid =  authUser.getSource() + authUser.getUuid();
        log.info(authUuid);
        SysUser sysUser = userSecondarydbRepository.selectUserByAuthUuid(authUuid);
        if (sysUser == null){
            SaveOrUpdateUserParam param = new SaveOrUpdateUserParam();
            if (StringUtils.isNotBlank(authUser.getEmail())){
                SysUser accountUser = userSecondarydbRepository.selectUserByUserAccount(authUser.getEmail());
                if (accountUser==null){
                    param.setUserAccount(authUser.getEmail());
                }
            }
            if (StringUtils.isBlank(param.getUserAccount())){
                param.setUserAccount(String.valueOf(SnowflakeId.getInstance().nextId()));
            }
            param.setRegisterType(4);
            param.setUserName(authUser.getUsername());
            param.setAuthUuid(authUuid);
            param.setRoleIdList(new Long[]{1000L});
            BaseResultEntity base = sysUserService.saveOrUpdateUser(param);
            if (!base.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
                return BaseResultEntity.failure(BaseResultEnum.AUTH_LOGIN,source.getSourceName()+"自动注册失败,请联系管理员!");
            }
        }
        Date date=new Date();
        String dateStr= DateUtil.formatDate(date,DateUtil.DateStyle.TIME_FORMAT_SHORT.getFormat());
        Long seq=sysCommonPrimaryRedisRepository.getCurrentSecondIncr(dateStr);
        String seqStr = String.format("%06d", seq);
        String authPublicKey=new StringBuilder().append(SysConstant.SYS_COMMON_AUTH_PUBLIC_KEY_PREFIX).append(dateStr).append(seqStr).toString();
        sysCommonPrimaryRedisRepository.setAuthUserKey(authPublicKey, authUuid);
        String url = String.format(baseAuthConfig.getRedirectUrl(), authPublicKey, true);
        log.info(url);
        return BaseResultEntity.success(url);
    }

    public BaseResultEntity authLogin(LoginParam loginParam,String ip) {
        loginParam.setToken(loginParam.getTokenKey());
        ResponseModel verification = captchaService.verification(loginParam);
        if (!verification.isSuccess()) {
            return BaseResultEntity.failure(BaseResultEnum.VERIFICATION_CODE,verification.getRepMsg());
        }
        String authUuid = sysCommonPrimaryRedisRepository.getKey(loginParam.getAuthPublicKey());
        log.info(authUuid);
        SysUser sysUser = userSecondarydbRepository.selectUserByAuthUuid(authUuid);
        if (sysUser == null) {
            return BaseResultEntity.failure(BaseResultEnum.ACCOUNT_NOT_FOUND);
        }
        return sysUserService.baseLogin(sysUser,ip);
    }

    public boolean validateVerificationCode(Integer code, String userAccount, String verificationCode) {
        return sysUserService.validateVerificationCode(code,userAccount,verificationCode);
    }

    public BaseResultEntity authRegister(SaveOrUpdateUserParam saveOrUpdateUserParam) {
        String authUserJson = sysCommonPrimaryRedisRepository.getKey(saveOrUpdateUserParam.getAuthPublicKey());
        log.info(authUserJson);
        try {
            if (StringUtils.isBlank(authUserJson)){
                return BaseResultEntity.failure(BaseResultEnum.AUTH_LOGIN,"获取授权信息过期,请重新授权");
            }
            AuthUser authUser = JSONObject.parseObject(authUserJson, AuthUser.class);
            if (authUser==null){
                return BaseResultEntity.failure(BaseResultEnum.AUTH_LOGIN,"获取授权信息异常 null");
            }

            if (StringUtils.isBlank(authUser.getSource()) || StringUtils.isBlank(authUser.getUuid())) {
                return BaseResultEntity.failure(BaseResultEnum.AUTH_LOGIN,"获取授权信息来源或唯一标识为空");
            }
            saveOrUpdateUserParam.setAuthUuid(authUser.getSource()+authUser.getUuid());
            SysUser sysUser = userSecondarydbRepository.selectUserByUserAccount(saveOrUpdateUserParam.getUserAccount());
            if (sysUser!=null){
                saveOrUpdateUserParam.setUserId(sysUser.getUserId());
            }else {
                saveOrUpdateUserParam.setUserName(StringUtils.isNotBlank(authUser.getUsername())?authUser.getUsername():"user");
            }
//            sysCommonPrimaryRedisRepository.setAuthUserKey(saveOrUpdateUserParam.getAuthPublicKey(), saveOrUpdateUserParam.getAuthUuid());
        }catch (Exception e){
            log.info(e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.AUTH_LOGIN,"授权注册失败");
        }

        log.info(JSONObject.toJSONString(saveOrUpdateUserParam));
        return sysUserService.saveOrUpdateUser(saveOrUpdateUserParam);
    }
}
