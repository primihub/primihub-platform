package com.primihub.entity.copy.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class CopyResourceFieldDto {

    /**
     * 资源id
     */
    @JsonIgnore
    private Long resourceId;

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


}
