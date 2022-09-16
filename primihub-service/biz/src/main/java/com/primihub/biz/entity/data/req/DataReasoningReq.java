package com.primihub.biz.entity.data.req;

import lombok.Data;

import java.util.List;

@Data
public class DataReasoningReq {
    private String reasoningName;
    private String reasoningDesc;
    private Long taskId;
    private Long userId;
    private List<DataReasoningResourceReq> resourceList;
}
