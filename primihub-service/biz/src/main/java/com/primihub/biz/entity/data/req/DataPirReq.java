package com.primihub.biz.entity.data.req;

import com.primihub.biz.entity.data.base.DataPirKeyQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("提交匿踪查询任务参数")
public class DataPirReq{
    @ApiModelProperty(value = "字符串格式资源ID",required = true,example = "5f8da28388f4-9396eee1-bbd3-46ac-a793-4b81e3e889d7")
    private String resourceId;
    @ApiModelProperty(value = "任务名称",required = true,example = "查询物业员工名单中姓名为张三的人")
    private String taskName;
    @ApiModelProperty(value = "查询条件",required = true)
    private List<DataPirKeyQuery> keyQuerys;

}

