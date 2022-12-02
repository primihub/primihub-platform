package com.primihub.biz.entity.sys.param;

import lombok.Data;

@Data
public class SaveOrUpdateUserParam {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 账号名称
     */
    private String userAccount;
    /**
     * 用户昵称
     */
    private String userName;
    /**
     * 角色id集合
     */
    private Long[] roleIdList;
    /**
     * 是否禁用
     */
    private Integer isForbid;
    /**
     * 密码
     */
    private String password;
    /**
     * 密码验证
     */
    private String passwordAgain;
    /**
     * 注册类型1：管理员创建 2：邮箱 3：手机 4: 第三方注册
     */
    private Integer registerType;
    /**
     * 验证码
     */
    private String verificationCode;

    private String authPublicKey;

    private String authUuid;
}
