package com.primihub.entity.resource.po;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;


@Data
public class FusionResource {


    /**
     * 自增ID
     */
    private Long id;

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
    private byte[] resourceColumnNameList;

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

    private String authOrgans;

    /**
     * 资源hash文件编码
     */
    private String resourceHashCode;

    /**
     * 资源状态 目前有 0上线 1下线
     */
    private Integer resourceState;

    private String userName;

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
