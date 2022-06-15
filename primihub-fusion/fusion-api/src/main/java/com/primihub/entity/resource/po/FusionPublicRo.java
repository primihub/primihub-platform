package com.primihub.entity.resource.po;

import lombok.Data;

import java.util.Date;

@Data
public class FusionPublicRo {

    public FusionPublicRo() {
    }

    public FusionPublicRo(Long resourceId, String organId) {
        this.resourceId = resourceId;
        this.organId = organId;
    }

    /**
     * 自增ID
     */
    private Long id;

    /**
     * 资源ID
     */
    private Long resourceId;

    /**
     * 机构ID
     */
    private String organId;

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
