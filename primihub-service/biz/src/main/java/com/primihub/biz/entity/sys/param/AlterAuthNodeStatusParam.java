package com.primihub.biz.entity.sys.param;

import lombok.Data;

@Data
public class AlterAuthNodeStatusParam {
    /**
     * 权限id
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
     * 数据权限代码
     */
    private String dataAuthCode;
    /**
     * 是否展示
     */
    private Integer isShow;
    /**
     * 过滤权限
     */
    private String authUrl;
}
