package com.primihub.biz.entity.data.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("创建或编辑模型参数")
public class DataModelAndComponentReq {
    @ApiModelProperty(value = "模型ID")
    private String modelId;
    @ApiModelProperty(value = "模型描述")
    private String modelDesc;
    @ApiModelProperty(value = "模型名称")
    private String modelName;
    @ApiModelProperty(value = "项目ID")
    private Long projectId;
    /**
     * 训练类型 0纵向 1横向 默认纵向
     */
    @ApiModelProperty(value = "训练类型 0纵向 1横向 默认纵向")
    private Integer trainType = 0;
    /**
     * 是否草稿 0是 1不是 默认是
     */
    @ApiModelProperty(value = "是否草稿 0是 1不是 默认是")
    private Integer isDraft = 0;

    @ApiModelProperty(value = "模型组件参数")
    private List<DataComponentReq> modelComponents;

    @ApiModelProperty(value = "模型前端组件留存参数")
    private List<DataModelPointComponent> modelPointComponents;

}
