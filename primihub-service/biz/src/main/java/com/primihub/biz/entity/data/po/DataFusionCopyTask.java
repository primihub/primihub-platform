package com.primihub.biz.entity.data.po;

import lombok.Data;

import java.util.Date;

@Data
public class DataFusionCopyTask {
    public DataFusionCopyTask() {
    }

    public DataFusionCopyTask(Integer taskType, Long currentOffset, Long targetOffset, String taskTable, String fusionServerAddress) {
        this.taskType = taskType;
        this.currentOffset = currentOffset;
        this.targetOffset = targetOffset;
        this.taskTable = taskTable;
        this.fusionServerAddress = fusionServerAddress;
        this.latestErrorMsg="";
    }

    private Long id;
    private Integer taskType;
    private Long currentOffset;
    private Long targetOffset;
    private String taskTable;
    private String fusionServerAddress;
    private String latestErrorMsg;
    private Integer isDel;
    private Date cTime;
    private Date uTime;
}
