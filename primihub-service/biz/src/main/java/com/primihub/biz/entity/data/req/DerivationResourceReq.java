package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class DerivationResourceReq extends PageReq {
    private Long resourceId;

    private String resourceName;

    private String tag;

    private String taskIdName;

    private Long projectId;

    private String startDate;

    private String endDate;
}
