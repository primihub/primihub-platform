package com.primihub.biz.entity.sys.po;

import lombok.Data;

import java.util.Date;

@Data
public class SysUo {
    /**
     * 自增id
     */
    private Long id;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 机构id
     */
    private Long organId;
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
