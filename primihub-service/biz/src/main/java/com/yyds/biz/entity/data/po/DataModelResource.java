package com.yyds.biz.entity.data.po;

import lombok.Data;

import java.util.Date;

@Data
public class DataModelResource {
    public DataModelResource() {
    }

    public DataModelResource(Long modelId, Long resourceId) {
        this.modelId = modelId;
        this.resourceId = resourceId;
        this.alignmentNum = 0;
        this.primitiveParamNum = 0;
        this.modelParamNum = 0;
    }

    private Long id;
    /**
     * 模型id
     */
    private Long modelId;
    /**
     * 资源id
     */
    private Long resourceId;
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
