package com.primihub.biz.entity.data.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 资源表
 */
@Data
public class DataResourceCopyVo {

    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 资源描述
     */
    private String resourceDesc;

    /**
     * 资源类型 上传...
     */
    private Integer resourceType;

    /**
     * 授权类型（公开，私有，可见性）
     */
    private Integer resourceAuthType;

    /**
     * 资源行数
     */
    private Integer resourceRowsCount;

    /**
     * 资源列数
     */
    private Integer resourceColumnCount;

    /**
     * 字段列表
     */
    private String resourceColumnNameList;

    /**
     * 资源字段中是否包含y字段 0否 1是
     */
    private Integer resourceContainsY;

    /**
     * 文件字段y值内容不为空和0的行数
     */
    private Integer resourceYRowsCount;

    /**
     * 文件字段y值内容不为空的行数在总行的占比
     */
    private BigDecimal resourceYRatio;

    /**
     * 资源标签 格式tag,tag
     */
    private String resourceTag;

    /**
     * 机构ID
     */
    private String organId;

    /**
     * 可见机构id
     */
    private List<String> authOrganList;

    /**
     * 资源字段列表
     */
    private List<DataResourceFieldCopyVo> fieldList;

    private Integer isDel = 0;
    /**
     * 资源hash文件编码
     */
    private String resourceHashCode;

    /**
     * 资源状态 目前有 0上线 1下线
     */
    private Integer resourceState;

    private String userName;

    private Map<String,Object> dataSet;
}
