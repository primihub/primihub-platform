package com.primihub.biz.entity.data.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel("Meta服务数据集列表参数")
public class DataFResourceReq extends PageReq {
    @ApiModelProperty(value = "数据集唯一ID")
    private String resourceId;
    @ApiModelProperty(value = "数据集名称 like")
    private String resourceName;
    @ApiModelProperty(value = "数据集来源")
    private Integer resourceSource;
    @ApiModelProperty(value = "机构ID")
    private String organId;
    @ApiModelProperty(value = "数据集标签")
    private String tagName;
    @ApiModelProperty(value = "数据集属性是否包含Y列")
    private Integer fileContainsY;
}
