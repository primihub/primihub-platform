package com.yyds.biz.entity.data.req;

import lombok.Data;

@Data
public class DataResourceFieldReq {
    /**
     * 字段id
     */
    private Long fieldId;

    /**
     * 字段别名
     */
    private String fieldAs;

    /**
     * 字段类型 string
     */
    private String fieldType;

    /**
     * 字段描述
     */
    private String fieldDesc;

    /**
     * 关键字 0否 1是
     */
    private Integer relevance;

    /**
     * 分组 0否 1是
     */
    private Integer grouping;

    /**
     * 保护开关 0关闭 1开启
     */
    private Integer protectionStatus;
}
