package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class DataPirReq{
    private String resourceId;
    private String pirParam;
    private String taskName;
    private String keyColumns;
    private String labelColumns;
}
