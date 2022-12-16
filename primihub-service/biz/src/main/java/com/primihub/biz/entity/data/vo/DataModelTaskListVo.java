package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class DataModelTaskListVo {
    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 任务id展示名
     */
    private String taskIdName;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务描述
     */
    private String taskDesc;

    /**
     * 任务状态(0未开始 1成功 2运行中 3失败 4取消)
     */
    private Integer taskState;

    /**
     * 任务类型 1、模型 2、PSI 3、PIR
     */
    private Integer taskType;

    /**
     * 任务开始时间
     */
    @JsonIgnore
    private Long taskStartTime;

    /**
     * 任务结束时间
     */
    @JsonIgnore
    private Long taskEndTime;

    /**
     * 任务开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date taskStartDate;

    /**
     * 任务结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date taskEndDate;

    /**
     * 任务异常信息
     */
    private String taskErrorMsg;

    public Long getTimeConsuming(){
        if ((taskStartTime!=null&&taskStartTime!=0L)&&(taskEndTime!=null&&taskEndTime!=0L)){
            return (taskEndTime-taskStartTime)/1000;
        }
        return 0L;
    }
    /**
     * 是否协作任务 默认0
     */
    private Integer isCooperation;

}
