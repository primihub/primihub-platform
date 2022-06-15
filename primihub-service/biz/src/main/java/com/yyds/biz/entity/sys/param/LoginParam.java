package com.yyds.biz.entity.sys.param;

import lombok.Data;

@Data
public class LoginParam {
    private String userAccount;
    private String userPassword;
    private String validateKeyName;
}
