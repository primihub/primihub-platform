package com.primihub.application.controller.sys;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.enumeration.OAuthSourceEnum;
import com.primihub.biz.entity.sys.enumeration.VerificationCodeEnum;
import com.primihub.biz.entity.sys.param.LoginParam;
import com.primihub.biz.entity.sys.param.SaveOrUpdateUserParam;
import com.primihub.biz.service.sys.SysOauthService;
import io.swagger.annotations.Api;
import me.zhyd.oauth.model.AuthCallback;
import me.zhyd.oauth.request.AuthRequest;
import me.zhyd.oauth.utils.AuthStateUtils;
import me.zhyd.oauth.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Api(value = "第三方授权接入接口",tags = "第三方授权接入接口")
@RequestMapping("oauth")
@RestController
public class OauthController {

    @Autowired
    private SysOauthService sysOauthService;

    @Value("${primihub.interior.code:null}")
    private String interiorCode;


    @GetMapping("getAuthList")
    public BaseResultEntity getOauthList(){
        return BaseResultEntity.success(sysOauthService.getOauthList());
    }

    @PostMapping("authLogin")
    public BaseResultEntity authLogin(LoginParam loginParam,@RequestHeader(value = "ip",defaultValue = "") String ip){
        if(loginParam.getAuthPublicKey()==null|| "".equals(loginParam.getAuthPublicKey().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"authPublicKey");
        }
        return sysOauthService.authLogin(loginParam,ip);
    }
    @PostMapping("authRegister")
    public BaseResultEntity authRegister(SaveOrUpdateUserParam saveOrUpdateUserParam){
        if(saveOrUpdateUserParam.getAuthPublicKey()==null|| "".equals(saveOrUpdateUserParam.getAuthPublicKey())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"authPublicKey");
        }
        if(saveOrUpdateUserParam.getVerificationCode()==null|| "".equals(saveOrUpdateUserParam.getVerificationCode())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"verificationCode");
        }
        if(saveOrUpdateUserParam.getRegisterType()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"registerType");
        }
        saveOrUpdateUserParam.setUserId(null);
        saveOrUpdateUserParam.setRoleIdList(new Long[]{1000L});
        if (org.apache.commons.lang.StringUtils.isBlank(interiorCode) || !saveOrUpdateUserParam.getVerificationCode().equals(interiorCode) ){
            if(!sysOauthService.validateVerificationCode(VerificationCodeEnum.REGISTER.getCode(),saveOrUpdateUserParam.getUserAccount(),saveOrUpdateUserParam.getVerificationCode())) {
                return BaseResultEntity.failure(BaseResultEnum.VERIFICATION_CODE);
            }
        }
        return sysOauthService.authRegister(saveOrUpdateUserParam);
    }

    @GetMapping("/{source}/render")
    public void renderAuth(HttpServletResponse response, @PathVariable("source") String source) throws IOException {
        if (StringUtils.isEmpty(source) && !OAuthSourceEnum.AUTH_MAP.containsKey(source)){
            oauthError(response,"来源");
        }else {
            AuthRequest authRequest = sysOauthService.getAuthRequest(OAuthSourceEnum.AUTH_MAP.get(source));
            if (authRequest == null){
                oauthError(response, OAuthSourceEnum.GITHUB.getSourceName());
            }else {
                response.sendRedirect(authRequest.authorize(AuthStateUtils.createState()));
            }
        }
    }

    @GetMapping("/{source}/callback")
    public void oauthLogin(AuthCallback callback,HttpServletResponse response,@PathVariable("source") String source) throws IOException {
        if (StringUtils.isEmpty(source) && !OAuthSourceEnum.AUTH_MAP.containsKey(source)){
            oauthError(response,"来源");
        }else {
            if (StringUtils.isEmpty(callback.getCode())) {
                oauthError(response,"code");
            }
            if (StringUtils.isEmpty(callback.getState())) {
                oauthError(response,"state");
            }
            BaseResultEntity baseResultEntity = sysOauthService.authDataLogin(callback, OAuthSourceEnum.AUTH_MAP.get(source));
            if (baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
                response.sendRedirect(baseResultEntity.getResult().toString());
            }else {
                oauthError(response,baseResultEntity.getMsg());
            }
        }
    }

    public void oauthError(HttpServletResponse response,String message) throws IOException {
        response.reset();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        response.getWriter().println(JSONObject.toJSONString(BaseResultEntity.failure(BaseResultEnum.AUTH_LOGIN,message+"-配置异常,请尝试联系我们!")));
    }
}
