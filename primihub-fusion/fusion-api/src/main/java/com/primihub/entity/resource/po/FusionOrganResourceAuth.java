package com.primihub.entity.resource.po;

import lombok.Data;

import java.util.Date;

@Data
public class FusionOrganResourceAuth {
    private Long id;

    private Long resourceId;

    private Long organId;

    private String projectId;

    private Integer auditStatus;

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
