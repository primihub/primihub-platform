package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class ComponentDraftReq {
    private Long draftId;

    private String draftName;

    private Long userId;

    private String componentJson;

    private String componentImage;
}
