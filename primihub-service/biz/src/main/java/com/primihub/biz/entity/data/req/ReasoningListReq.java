package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class ReasoningListReq extends PageReq {
    private Long id;

    private String reasoningName;

    private Integer reasoningState;

    private Long userId;

    private Integer isAdmin = 0;
}
