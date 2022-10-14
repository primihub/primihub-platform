package com.primihub.biz.entity.sys.param;

import lombok.Data;

@Data
public class LoginParam extends BaseCaptchaParam {
    private String userAccount;
    private String userPassword;
    private String validateKeyName;
    private String authPublicKey;
}
