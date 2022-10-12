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
import com.primihub.biz.entity.sys.po.SysUser;
import com.primihub.biz.entity.sys.vo.BaseAuthConfig;
import com.primihub.biz.repository.primaryredis.sys.SysCommonPrimaryRedisRepository;
import com.primihub.biz.repository.secondarydb.sys.SysUserSecondarydbRepository;
import com.primihub.biz.util.crypt.DateUtil;
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

    public AuthRequest getAuthRequest(OAuthSourceEnum source){
        AuthRequest authRequest = null;
        if (source == OAuthSourceEnum.GITHUB){
            BaseAuthConfig baseAuthConfig = baseConfiguration.getAuthConfigs().get(OAuthSourceEnum.GITHUB.getSourceName());
            if (baseAuthConfig!=null){
                // RedirectUri URL encoding needs to be converted
                authRequest = new AuthGithubRequest(baseAuthConfig);
            }
        }else if (source == OAuthSourceEnum.WEIXIN){
            BaseAuthConfig baseAuthConfig = baseConfiguration.getAuthConfigs().get(OAuthSourceEnum.WEIXIN.getSourceName());
            if (baseAuthConfig!=null){
                // RedirectUri URL encoding needs to be converted
                authRequest = new AuthWeChatOpenRequest(baseAuthConfig);
            }
        }
        return authRequest;
    }

    public Set<String> getOauthList() {
        Set<String> set= new HashSet<>();
        Iterator<Map.Entry<String, BaseAuthConfig>> iterator = baseConfiguration.getAuthConfigs().entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String, BaseAuthConfig> next = iterator.next();
            BaseAuthConfig value = next.getValue();
            if (value.getAuthEnable()!=null && value.getAuthEnable() == 0 && StringUtils.isNotBlank(value.getRedirectUrl()))
                set.add(next.getKey());
        }
        return set;
    }

    public BaseResultEntity authDataLogin(AuthCallback callback,OAuthSourceEnum source) {
        if (!getOauthList().contains(source.getSourceName())){
            return BaseResultEntity.failure(BaseResultEnum.AUTH_LOGIN,source.getSourceName()+"授权未配置,不支持登录");
        }
        AuthRequest authRequest = getAuthRequest(source);
        AuthResponse login = authRequest.login(callback);
        if (login.getCode() != AuthResponseStatus.SUCCESS.getCode())
            return BaseResultEntity.failure(BaseResultEnum.AUTH_LOGIN,source.getSourceName()+"授权登录失败");
        AuthUser authUser = (AuthUser)login.getData();
        String authUuid =  authUser.getSource() + authUser.getUuid();
        log.info(authUuid);
        SysUser sysUser = userSecondarydbRepository.selectUserByAuthUuid(authUuid);
        Date date=new Date();
        String dateStr= DateUtil.formatDate(date,DateUtil.DateStyle.TIME_FORMAT_SHORT.getFormat());
        Long seq=sysCommonPrimaryRedisRepository.getCurrentSecondIncr(dateStr);
        String seqStr = String.format("%06d", seq);
        String authPublicKey=new StringBuilder().append(SysConstant.SYS_COMMON_AUTH_PUBLIC_KEY_PREFIX).append(dateStr).append(seqStr).toString();
        sysCommonPrimaryRedisRepository.setAuthUserKey(authPublicKey, authUuid);
        BaseAuthConfig baseAuthConfig = baseConfiguration.getAuthConfigs().get(source.getSourceName());
        String url = String.format(baseAuthConfig.getRedirectUrl(), authPublicKey, sysUser != null);
        log.info(url);
        return BaseResultEntity.success(url);
    }

    public BaseResultEntity authLogin(LoginParam loginParam) {
        loginParam.setToken(loginParam.getTokenKey());
        ResponseModel verification = captchaService.verification(loginParam);
        if (!verification.isSuccess())
            return BaseResultEntity.failure(BaseResultEnum.VERIFICATION_CODE,verification.getRepMsg());
        String authUuid = sysCommonPrimaryRedisRepository.getKey(loginParam.getAuthPublicKey());
        SysUser sysUser = userSecondarydbRepository.selectUserByAuthUuid(authUuid);
        if (sysUser == null)
            return BaseResultEntity.failure(BaseResultEnum.ACCOUNT_NOT_FOUND);
        return sysUserService.baseLogin(sysUser);
    }
}
