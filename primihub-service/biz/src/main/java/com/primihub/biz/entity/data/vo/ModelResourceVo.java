package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class ModelResourceVo {
    /**
     * 资源id
     */
    private Long resourceId;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 机构id
     */
    private Long organId;
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

    @JsonIgnore
    private String url;
}
