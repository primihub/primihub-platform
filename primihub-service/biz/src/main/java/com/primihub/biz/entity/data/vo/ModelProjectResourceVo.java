package com.primihub.biz.entity.data.vo;

import lombok.Data;

import java.util.List;

@Data
public class ModelProjectResourceVo {

    private String organId;

    private String organName;
    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 资源行数
     */
    private Integer resourceRowsCount;

    /**
     * 资源列数
     */
    private Integer resourceColumnCount;

    /**
     * 资源字段中是否包含y字段 0否 1是
     */
    private Integer resourceContainsY;
    /**
     * 审核状态 0审核中 1同意 2拒绝
     */
    private Integer auditStatus;

    /**
     * 机构项目中参与身份 1发起者 2协作者
     */
    private Integer participationIdentity;
    private List<DataFileFieldVo> resourceField;
    private List<String> fileHandleField;

    private String calculationField;

    private Integer derivation = 0;
}
