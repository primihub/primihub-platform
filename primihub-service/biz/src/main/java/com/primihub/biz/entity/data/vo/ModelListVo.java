package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class ModelListVo {
    // 模型id
    private Long modelId;
    // 模型名称
    private String modelName;
    private String projectName;
    private Integer resourceNum;
    private Long latestTaskId;
    private String latestTaskIdName;
    private String latestTaskName;
    //最近一次任务状态
    private Integer latestTaskStatus;
    /**
     * 任务开始时间
     */
    @JsonIgnore
    private Long latestTaskStartTime;

    /**
     * 任务结束时间
     */
    @JsonIgnore
    private Long latestTaskEndTime;

    /**
     * 任务开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date latestTaskStartDate;

    /**
     * 任务结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date taskEndDate;

    public Long getTimeConsuming(){
        if ((latestTaskStartTime!=null&&latestTaskStartTime!=0L)&&(latestTaskEndTime!=null&&latestTaskEndTime!=0L)){
            return (latestTaskEndTime-latestTaskStartTime)/1000;
        }
        return 0L;
    }

    private Integer TaskType;

}
