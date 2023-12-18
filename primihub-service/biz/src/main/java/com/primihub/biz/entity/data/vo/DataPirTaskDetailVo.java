package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;

@Data
public class DataPirTaskDetailVo {

    private String taskName;
    private String taskIdName;
    private Integer taskState;
    private String organName;
    private String resourceName;
    private String resourceId;
    private String retrievalId;
    private String taskError;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;

    /**
     * 任务开始时间
     */
    private Long taskStartTime;

    /**
     * 任务结束时间
     */
    private Long taskEndTime;

    private List<LinkedHashMap<String, Object>> list;

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
