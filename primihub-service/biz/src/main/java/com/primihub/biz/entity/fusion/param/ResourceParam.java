package com.primihub.biz.entity.fusion.param;

import com.primihub.biz.entity.data.req.PageReq;
import lombok.Data;

import java.util.List;

@Data
public class ResourceParam extends PageReq {
    private String resourceId;
    private String resourceName;
    private Integer resourceType;
    private String organId;
    private List<String> organIds;
    private String tagName;
    private Integer fileContainsY;
    private String globalId;
}
