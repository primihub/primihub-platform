package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

/**
 * <p>
 * 
 * </p>
 *
 * @author a
 * @since 2022-06-07
 */
@Getter
@Setter
@ToString
public class DataTaskVo {

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
     * 任务状态(0未开始 1成功 2运行中 3失败 4取消)
     */
    private Integer taskState;

    /**
     * 任务类型 1、模型 2、PSI 3、PIR 4 推理
     */
    private Integer taskType;


    /**
     * 任务开始时间
     */
    private Long taskStartTime;

    /**
     * 任务结束时间
     */
    private Long taskEndTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;


}
