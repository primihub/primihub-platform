package com.primihub.biz.entity.sys.config;

import me.zhyd.oauth.config.AuthConfig;

public class BaseAuthConfig extends AuthConfig {

    private String redirectUrl;

    private Integer authEnable = 1;

    public Integer getAuthEnable() {
        return authEnable;
    }

    public void setAuthEnable(Integer authEnable) {
        this.authEnable = authEnable;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public void setRedirectUrl(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }
}
