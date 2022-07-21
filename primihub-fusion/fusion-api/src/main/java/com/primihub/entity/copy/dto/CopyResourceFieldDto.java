package com.primihub.entity.copy.dto;

import lombok.Data;

import java.util.Date;

@Data
public class CopyResourceFieldDto {

    /**
     * 字段名称
     */
    private String fieldName;

    /**
     * 字段别名
     */
    private String fieldAs;

    /**
     * 字段类型 默认0 string
     */
    private Integer fieldType;

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
