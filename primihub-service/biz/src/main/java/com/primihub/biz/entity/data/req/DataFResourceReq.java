package com.primihub.biz.entity.data.req;

import lombok.Data;

import java.util.List;

@Data
public class DataFResourceReq extends PageReq {
    private String resourceId;
    private String resourceName;
    private Integer resourceSource;
    private String organId;
    private String tagName;
    private Integer fileContainsY;
}
