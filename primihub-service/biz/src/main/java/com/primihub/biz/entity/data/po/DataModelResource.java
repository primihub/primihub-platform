package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class DataModelResource {
    public DataModelResource() {
    }

    public DataModelResource(Long modelId, Long resourceId) {
        this.modelId = modelId;
        this.resourceId = resourceId.toString();
        this.alignmentNum = 0;
        this.primitiveParamNum = 0;
        this.modelParamNum = 0;
    }
    public DataModelResource(Long modelId) {
        this.modelId = modelId;
        this.alignmentNum = 0;
        this.primitiveParamNum = 0;
        this.modelParamNum = 0;
    }

    private Long id;
    /**
     * 模型id
     */
    @JsonIgnore
    private Long modelId;
    /**
     * 资源id
     */
    private String resourceId;

    /**
     * 参与类型 0使用数据 1衍生数据
     */
    private Integer takePartType = 0;
    /**
     * 任务ID
     */
    @JsonIgnore
    private Long taskId;
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
