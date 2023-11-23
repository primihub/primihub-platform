package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 资源表
 */
@Data
public class DataResourceVo {
    /**
     * 资源id
     */
    private Long resourceId;
    /**
     * 资源名称
     */
    private String resourceName;
    /**
     * 资源描述
     */
    private String resourceDesc;
    /**
     * 授权类型 1.公开 2.私有
     */
    private Integer resourceAuthType;
    /**
     * 资源来源 文件上传 数据库链接
     */
    private Integer resourceSource;
    /**
     * 资源数
     */
    private Integer resourceNum;
    /**
     * 文件id
     */
    private Long fileId;
    /**
     * 文件大小
     */
    private Integer fileSize;
    /**
     * 文件后缀
     */
    private String fileSuffix;
    /**
     * 文件行数
     */
    private Integer fileRows;
    /**
     * 文件列数
     */
    private Integer fileColumns;
    /**
     * 文件处理状态 0 未处理 1处理中 2处理完成
     */
    private Integer fileHandleStatus;
    /**
     * 文件字段是否包含y字段 0否 1是
     */
    private Integer fileContainsY;
    /**
     * 文件字段y值内容不为空的行数
     */
    private Integer fileYRows;
    /**
     * 文件字段y值内容不为空的行数在总行的占比
     */
    private BigDecimal fileYRatio;
    /**
     * 数据库id
     */
    private Long dbId;
    /**
     * 用户id
     */
    private Long userId;

    private String userName;
    /**
     * 机构id
     */
    private Long organId;

    /**
     * 机构名称
     */
    private String organName;
    /**
     * 资源标识路径
     */
    @JsonIgnore
    private String url;
    /**
     * 创建时间
     */
   @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    /**
     * 标签列表
     */
    private List<ResourceTagListVo> tags;

    private String[] fileHandleField;
    /**
     * 资源hash文件编码
     */
    private String resourceHashCode;

    /**
     * 资源状态 目前有 0上线 1下线
     */
    private Integer resourceState;
    /**
     * 中心节点资源id
     */
    private String resourceFusionId;
    /**
     * organFusionId
     */
    private String organFusionId;
}
