package com.primihub.biz.entity.data.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("成功后的模型列表参数")
public class ModelTaskSuccessReq extends PageReq {
    @ApiModelProperty(value = "模型ID")
    private Long modelId;
    @ApiModelProperty(value = "模型名称")
    private String modelName;
    @ApiModelProperty(value = "模型类型")
    private Integer modelType;
    @ApiModelProperty(value = "开始时间")
    private String startDate;
    @ApiModelProperty(value = "结束时间")
    private String endDate;
    private Long startTime;
    private Long endTime;
    private Long userId;
    private Integer isAdmin = 0;
}
