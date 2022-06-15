package com.yyds.biz.entity.data.po;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author 
 * @since 2022-04-25
 */
@Getter
@Setter
public class DataPsi {

    /**
     * psi 主键
     */
    private Long id;

    /**
     * 本机构id
     */
    private Long ownOrganId;

    /**
     * 本机构资源id
     */
    private Long ownResourceId;

    /**
     * 本机构资源关键字
     */
    private String ownKeyword;

    /**
     * 其他机构id
     */
    private Long otherOrganId;

    /**
     * 其他机构资源id
     */
    private Long otherResourceId;

    /**
     * 其他机构资源关键字
     */
    private String otherKeyword;

    /**
     * 文件路径输出类型 0默认 自动生成
     */
    private Integer outputFilePathType;

    /**
     * 输出内容是否不去重 默认0 不去重 1去重
     */
    private Integer outputNoRepeat;

    /**
     * 是否对"可统计"的附加列做全表统计 默认0 是 1不是
     */
    private Integer columnCompleteStatistics;

    /**
     * 结果名称
     */
    private String resultName;

    /**
     * 输出内容 默认0 0交集 1差集
     */
    private Integer outputContent;

    /**
     * 输出格式
     */
    private String outputFormat;

    /**
     * 结果获取方 多机构","号间隔
     */
    private String resultOrganIds;

    /**
     * 备注
     */
    private String remarks;

    private Long userId;
    /**
     * 是否删除
     */
    @JsonIgnore
    private Integer isDel;

    /**
     * 创建时间
     */
    @JsonIgnore
    private Date createDate;

    /**
     * 修改时间
     */
    @JsonIgnore
    private Date updateDate;


}
