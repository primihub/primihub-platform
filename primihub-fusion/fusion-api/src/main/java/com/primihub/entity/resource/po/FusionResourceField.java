package com.primihub.entity.resource.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class FusionResourceField {

    /**
     * 字段id
     */
    @JsonIgnore
    private Long fieldId;

    /**
     * 资源id
     */
    @JsonIgnore
    private Long resourceId;

    private Integer fieldIndex;

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
     * 是否删除
     */
    @JsonIgnore
    private Integer isDel;

    /**
     * 创建时间
     */
    @JsonIgnore
    private Date cTime;

    /**
     * 更新时间
     */
    @JsonIgnore
    private Date uTime;


}
