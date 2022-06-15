package com.yyds.biz.entity.sys.po;

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
     * 机构id集合
     */
    private String organIdList;
    /**
     * 根机构id集合
     */
    private String rOrganIdList;
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
     * 创建时间
     */
    private Date cTime;
    /**
     * 修改时间
     */
    private Date uTime;
}
