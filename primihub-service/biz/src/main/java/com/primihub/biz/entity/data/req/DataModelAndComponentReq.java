package com.primihub.biz.entity.data.req;

import lombok.Data;

import java.util.List;

/**
 * 创建或编辑模型参数
 */
@Data
public class DataModelAndComponentReq {
    /** 模型ID */
    private String modelId;
    /** 模型描述 */
    private String modelDesc;
    /** 模型名称 */
    private String modelName;
    /** 项目ID */
    private Long projectId;
    /**
     * 训练类型 0纵向 1横向 默认纵向
     */
    private Integer trainType = 0;
    /**
     * 是否草稿 0是 1不是 默认是
     */
    private Integer isDraft = 0;
    /** 模型组件参数 */
    private List<DataComponentReq> modelComponents;
    /** 模型前端组件留存参数 */
    private List<DataModelPointComponent> modelPointComponents;

}
