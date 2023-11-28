package com.primihub.biz.entity.data.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("提交匿踪查询任务参数")
public class DataPirReq{
    @ApiModelProperty(value = "字符串格式资源ID",required = true)
    private String resourceId;
    @ApiModelProperty(value = "查询内容",required = true)
    private String pirParam;
    @ApiModelProperty(value = "任务名称",required = true)
    private String taskName;
    @ApiModelProperty(value = "根据那些列查询",required = true)
    private String keyColumns;
    @ApiModelProperty(value = "结果展示那些列",notes = "不填写则输出keyColumns参数以外的列")
    private String labelColumns;
}
