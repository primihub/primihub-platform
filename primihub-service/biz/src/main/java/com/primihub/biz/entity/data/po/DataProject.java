package com.primihub.biz.entity.data.po;

import lombok.Data;

import java.util.Date;

/**
 * 项目表
 */
@Data
public class DataProject {
    /**
     * 项目id
     */
    private Long projectId;
    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 项目描述
     */
    private String projectDesc;
    /**
     * 机构id
     */
    private Long organId;
    /**
     * 机构数
     */
    private Integer organNum;
    /**
     * 资源数
     */
    private Integer resourceNum;
    /**
     * 机构id数组 ,间隔
     */
    private String resourceOrganIds;
    /**
     * 已授权资源数
     */
    private Integer authResourceNum;
    /**
     * 用户id
     */
    private Long userId;
    /**
     * 是否删除
     */
    private Integer isDel;
    /**
     * 创建时间
     */
    private Date createDate;
    /**
     * 修改时间
     */
    private Date updateDate;
}
