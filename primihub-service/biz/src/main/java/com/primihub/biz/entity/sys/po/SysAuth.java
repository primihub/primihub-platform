package com.primihub.biz.entity.sys.po;

import lombok.Data;

import java.util.Date;

/**
 * 资源权限表
 */
@Data
public class SysAuth {
    /**
     * 资源id
     */
    private Long authId;
    /**
     * 资源名称
     */
    private String authName;
    /**
     * 权限类型（1.菜单 2.列表 3.按钮）
     */
    private Integer authType;
    /**
     * 权限代码
     */
    private String authCode;
    /**
     * 上级id
     */
    private Long pAuthId;
    /**
     * 根id
     */
    private Long rAuthId;
    /**
     * 完整路径
     */
    private String fullPath;
    /**
     * 过滤路径
     */
    private String authUrl;
    /**
     * 数据权限代码
     */
    private String dataAuthCode;
    /**
     * 顺序
     */
    private Integer authIndex;
    /**
     * 深度
     */
    private Integer authDepth;
    /**
     * 是否展示
     */
    private Integer isShow;
    /**
     * 是否可编辑
     */
    private Integer isEditable;
    /**
     * 是否删除
     */
    private Integer isDel;
    /**
     * 创建时间
     */
    private Date cTime;
    /**
     * 修改时间
     */
    private Date uTime;
}
