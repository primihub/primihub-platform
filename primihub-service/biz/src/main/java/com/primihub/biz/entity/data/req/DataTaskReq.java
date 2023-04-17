package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class DataTaskReq extends PageReq {
    private Long start;
    private Long end;
    private String taskName;
    private String taskDesc;
    private String taskId;
    private Integer taskType;
}
