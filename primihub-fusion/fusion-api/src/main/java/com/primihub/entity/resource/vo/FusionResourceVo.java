package com.primihub.entity.resource.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Data
public class FusionResourceVo {

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
     * 开启保护字段列表
     */
    private String openColumnNameList;

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
     * 资源标签
     */
    private List<String> resourceTag;

    /**
     * 机构ID
     */
    private String organId;

    private String organName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone="GMT+8")
    private Date createDate;


}
