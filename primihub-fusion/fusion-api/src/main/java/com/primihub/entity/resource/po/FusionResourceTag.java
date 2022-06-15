package com.primihub.entity.resource.po;

import lombok.Data;

import java.util.Date;

@Data
public class FusionResourceTag {


    /**
     * 自增ID
     */
    private Long id;

    /**
     * 标签名称
     */
    private String name;

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
