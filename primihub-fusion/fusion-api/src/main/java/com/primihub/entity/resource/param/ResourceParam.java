package com.primihub.entity.resource.param;

import lombok.Data;

@Data
public class ResourceParam extends PageParam {
    private String resourceId;
    private String resourceName;
    private Integer resourceType;
    private String organId;
    private String tagName;
    private String globalId;
    private Integer cancelUnion=0;
}
