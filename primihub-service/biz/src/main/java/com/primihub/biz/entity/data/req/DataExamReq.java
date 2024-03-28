package com.primihub.biz.entity.data.req;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

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
    private Set<String> fieldValueSet;
}

