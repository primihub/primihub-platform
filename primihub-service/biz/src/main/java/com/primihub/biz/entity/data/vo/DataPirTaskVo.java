package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class DataPirTaskVo {

    private String serverAddress;

    private Long taskId;

    private String taskIdName;

    private String organId;

    private String organName;

    private String resourceId;

    private String resourceName;

    private Integer resourceRowsCount;

    private Integer resourceColumnCount;

    private Integer resourceContainsY;

    private Integer resourceYRowsCount;

    private Integer resourceState;

    private BigDecimal resourceYRatio;

    private String retrievalId;

    private Integer available;

    private String taskName;

    /**
     * 任务状态(0未开始 1成功 2查询中 3失败)
     */
    private Integer taskState;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;
    private Long consuming;
    @JsonIgnore
    private Long taskStart;
    @JsonIgnore
    private Long taskEnd;

    public Long getConsuming() {
        if (taskStart==null) {
            return 0L;
        }
        if (taskEnd==null||taskEnd==0) {
            return (System.currentTimeMillis() - taskStart)/1000;
        }
        return (taskEnd - taskStart)/1000;
    }
}
