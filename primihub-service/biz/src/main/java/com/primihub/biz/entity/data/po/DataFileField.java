package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class DataFileField {

    public DataFileField() {
    }

    public DataFileField(Long fileId, String fieldName,Integer fieldType, String fieldAs) {
        this.fileId = fileId;
        this.fieldName = fieldName;
        this.fieldAs = fieldAs;
        this.fieldType = fieldType;
        this.relevance = 0;
        this.grouping = 0;
        this.protectionStatus = 0;
        this.isDel = 0;
    }

    public DataFileField(Long fileId, String fieldName, String fieldAs) {
        this.fileId = fileId;
        this.fieldName = fieldName;
        this.fieldAs = fieldAs;
        this.fieldType = 0;
        this.relevance = 0;
        this.grouping = 0;
        this.protectionStatus = 0;
        this.isDel = 0;
    }

    public DataFileField(Long fileId, Long resourceId, String fieldName, String fieldAs) {
        this.fileId = fileId;
        this.resourceId = resourceId;
        this.fieldName = fieldName;
        this.fieldAs = fieldAs;
        this.fieldType = 0;
        this.relevance = 0;
        this.grouping = 0;
        this.protectionStatus = 0;
        this.isDel = 0;
    }

    /**
     * 字段id
     */
    private Long fieldId;

    /**
     * 文件id
     */
    private Long fileId;

    /**
     * 资源id
     */
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

    /**
     * 是否删除
     */
    @JsonIgnore
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 修改时间
     */
    @JsonIgnore
    private Date updateDate;


}
