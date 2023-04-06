package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class DataFResourceReq extends PageReq {
    private String resourceId;
    private String resourceName;
    private Integer resourceSource;
    private String organId;
    private String tagName;
    private String serverAddress;
    private Integer fileContainsY;
}
