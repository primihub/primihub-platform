package com.primihub.biz.entity.sys.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
 * 用户基础信息表
 */
@Data
public class SysUser {
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 账号名称
     */
    private String userAccount;
    /**
     * 账号密码
     */
    private String userPassword;
    /**
     * 用户昵称
     */
    private String userName;
    /**
     * 角色id集合
     */
    private String roleIdList;
    /**
     * 是否禁用
     */
    private Integer isForbid;
    /**
     * 是否删除
     */
    private Integer isDel;
    /**
     * 是否可编辑
     */
    private Integer isEditable;
    /**
     * 注册类型1：管理员创建 2：邮箱 3：手机
     */
    private Integer registerType;

    /**
     * 第三方uuid
     */
    private String authUuid;
    /**
     * 用户最近登录的客户端IP地址
     */
    private String ip;
    /**
     * 创建时间
     */
    private Date cTime;
    /**
     * 修改时间
     */
    private Date uTime;
}
