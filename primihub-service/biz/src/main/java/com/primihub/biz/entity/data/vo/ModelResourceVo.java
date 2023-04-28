package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ModelResourceVo {
    /**
     * 资源id
     */
    private String resourceId;

    /**
     * 资源名称
     */
    private String resourceName;
    /**
     * 资源类型
     */
    private Integer resourceType;
    /**
     * 机构项目中参与身份 1发起者 2协作者
     */
    private Integer participationIdentity;

    /**
     * 机构id
     */
    private String organId;
    /**
     * 机构名称
     */
    private String organName;

    /**
     * 原始记录数
     */
    private Integer fileNum;
    /**
     * 对齐后记录数量
     */
    private Integer alignmentNum;
    /**
     * 原始变量数量
     */
    private Integer primitiveParamNum;
    /**
     * 入模变量数量
     */
    private Integer modelParamNum;

    private Integer available;

    private Integer takePartType;

    @JsonIgnore
    private String url;
}
