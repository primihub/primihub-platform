package com.primihub.application.controller.sys;


import com.anji.captcha.model.common.ResponseModel;
import com.anji.captcha.model.vo.CaptchaVO;
import com.anji.captcha.service.CaptchaService;
import com.anji.captcha.util.StringUtils;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.param.BaseCaptchaParam;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * 图片验证码
 */
@Api(value = "图片验证码接口",tags = "图片验证码接口")
@RestController
@RequestMapping("/captcha")
public class CaptchaController {

    @Autowired
    private CaptchaService captchaService;

    /**
     * 获取图片验证码
     * @param data
     * @param request
     * @return
     */
    @PostMapping("/get")
    public BaseResultEntity get(@RequestBody BaseCaptchaParam data, HttpServletRequest request) {
        data.setToken(data.getTokenKey());
        assert request.getRemoteHost()!=null;
        data.setBrowserInfo(getRemoteId(request));
        ResponseModel responseModel = captchaService.get(data);
        if (responseModel.isSuccess()){
            return BaseResultEntity.success(responseModel.getRepData());
        }else {
            return BaseResultEntity.failure(BaseResultEnum.VERIFICATION_CODE,responseModel.getRepMsg());
        }
    }

    /**
     * 校验图片验证码
     * @param data
     * @param request
     * @return
     */
    @PostMapping("/check")
    public BaseResultEntity check(@RequestBody BaseCaptchaParam data, HttpServletRequest request) {
        data.setToken(data.getTokenKey());
        data.setBrowserInfo(getRemoteId(request));
        ResponseModel responseModel = captchaService.check(data);
        if (responseModel.isSuccess()){
            return BaseResultEntity.success(responseModel.getRepData());
        }else {
            return BaseResultEntity.failure(BaseResultEnum.VERIFICATION_CODE,responseModel.getRepMsg());
        }
    }


    public static final String getRemoteId(HttpServletRequest request) {
        String xfwd = request.getHeader("X-Forwarded-For");
        String ip = getRemoteIpFromXfwd(xfwd);
        String ua = request.getHeader("user-agent");
        if (StringUtils.isNotBlank(ip)) {
            return ip + ua;
        }
        return request.getRemoteAddr() + ua;
    }

    private static String getRemoteIpFromXfwd(String xfwd) {
        if (StringUtils.isNotBlank(xfwd)) {
            String[] ipList = xfwd.split(",");
            return StringUtils.trim(ipList[0]);
        }
        return null;
    }

}