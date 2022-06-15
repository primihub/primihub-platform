package com.primihub.biz.entity.sys.po;

import lombok.Data;

import java.util.Date;

@Data
public class SysRa {
    /**
     * id
     */
    private Long id;
    /**
     * 角色id
     */
    private Long roleId;
    /**
     * 权限id
     */
    private Long authId;
    /**
     * 是否删除
     */
    private Integer isDel;
    /**
     * 创建时间
     */
    private Date cTime;
    /**
     * 更新时间
     */
    private Date uTime;
}
