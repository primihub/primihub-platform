package com.primihub.biz.entity.sys.po;

import lombok.Data;

import java.util.Date;

/**
 * 机构表
 */
@Data
public class SysOrgan {
    /**
     * 机构id
     */
    private Long organId;
    /**
     * 机构名称
     */
    private String organName;
    /**
     * 上级id
     */
    private Long pOrganId;
    /**
     * 根级id
     */
    private Long rOrganId;
    /**
     * 完整路径
     */
    private String fullPath;
    /**
     * 顺序
     */
    private Integer organIndex;
    /**
     * 机构深度
     */
    private Integer organDepth;
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
