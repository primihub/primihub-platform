package com.primihub.biz.entity.data.req;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DataExamReq {
    private String resourceId;
    private String taskName;
    private String targetOrganId;
    private String originOrganId;
    private String taskId;
    private Integer taskState;
    private String targetResourceId;
}

