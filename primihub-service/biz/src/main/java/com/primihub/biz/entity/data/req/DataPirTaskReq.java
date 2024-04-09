package com.primihub.biz.entity.data.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("匿踪任务列表查询条件")
@Data
public class DataPirTaskReq extends PageReq {

    @ApiModelProperty(value = "机构名称")
    private String organName;
    @ApiModelProperty(value = "资源名称")
    private String resourceName;
    @ApiModelProperty(value = "检索内容")
    private String retrievalId;
    @ApiModelProperty(value = "任务状态(0未开始 1成功 2查询中 3失败)")
    private Integer taskState;
    @ApiModelProperty(value = "任务ID")
    private String taskId;
    @ApiModelProperty(value = "任务名称")
    private String taskName;
    @ApiModelProperty(value = "开始日期")
    private String startDate;
    @ApiModelProperty(value = "结束日期")
    private String endDate;

}
