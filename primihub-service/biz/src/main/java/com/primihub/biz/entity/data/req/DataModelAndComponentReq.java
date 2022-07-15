package com.primihub.biz.entity.data.req;

import lombok.Data;

import java.util.List;

@Data
public class DataModelAndComponentReq {
    private String modelId;
    private String modelDesc;
    private String modelName;
    private Long projectId;
    /**
     * 训练类型 0纵向 1横向 默认纵向
     */
    private Integer trainType = 0;
    /**
     * 是否草稿 0是 1不是 默认是
     */
    private Integer isDraft = 0;

    private List<DataComponentReq> modelComponents;

    private List<DataModelPointComponent> modelPointComponents;

}
