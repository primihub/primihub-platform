package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 项目表
 * </p>
 *
 * @author 
 * @since 2022-06-22
 */
@Data
public class DataProject {

    /**
     * 自增ID
     */
    @JsonIgnore
    private Long id;

    /**
     * 项目ID 机构后12位+UUID
     */
    private String projectId;

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
    private String createdOrganId;

    /**
     * 机构名称
     */
    private String createdOrganName;

    /**
     * 创建者名称
     */
    private String createdUsername;

    /**
     * 资源数
     */
    private Integer resourceNum;

    /**
     * 协作方机构名称 保存三个
     */
    private String providerOrganNames;

    /**
     * 项目状态 0审核中 1可用 2关闭
     */
    private Integer status;

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
