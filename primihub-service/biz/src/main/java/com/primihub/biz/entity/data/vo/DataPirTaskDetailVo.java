package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@Data
@ApiModel("匿踪查询任务详情")
public class DataPirTaskDetailVo {
    @ApiModelProperty(value = "任务名称",example = "查询物业员工名单中姓名为张三的人")
    private String taskName;
    @ApiModelProperty(value = "任务全局唯一ID",example = "1724332376334200834")
    private String taskIdName;
    @ApiModelProperty(value = "任务状态:INIT(0,\"初始未开始\"),SUCCESS(1,\"成功\"),IN_OPERATION(2,\"运行中\"),FAIL(3,\"失败\"),CANCEL(4,\"取消\"),DELETE(5,\"删除\")",example = "1")
    private Integer taskState;
    @ApiModelProperty(value = "机构名称",example = "机构B")
    private String organName;
    @ApiModelProperty(value = "资源名称",example = "华众物业服务员工名单数据资源")
    private String resourceName;
    @ApiModelProperty(value = "资源ID",example = "5f8da28388f4-9396eee1-bbd3-46ac-a793-4b81e3e889d7")
    private String resourceId;
    @ApiModelProperty(value = "查询内容",example = "张三")
    private String retrievalId;
    @ApiModelProperty(value = "任务错误信息",example = "")
    private String taskError;
    @ApiModelProperty(value = "任务创建时间",example = "2023-11-14 15:44:09")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    /**
     * 任务开始时间
     */
    @ApiModelProperty(value = "任务开始时间",example = "1699947849721")
    private Long taskStartTime;

    /**
     * 任务结束时间
     */
    @ApiModelProperty(value = "任务结束时间",example = "1699947851781")
    private Long taskEndTime;

    @ApiModelProperty(value = "结果文件示例数据(最多50条)")
    private List<LinkedHashMap<String, Object>> list;

    @ApiModelProperty(value = "总耗时(秒)",example = "5")
    public Long getConsuming() {
        if (taskStartTime==null) {
            return 0L;
        }
        if (taskEndTime==null||taskEndTime==0) {
            return (System.currentTimeMillis() - taskStartTime)/1000;
        }
        return (taskEndTime - taskStartTime)/1000;
    }
}
