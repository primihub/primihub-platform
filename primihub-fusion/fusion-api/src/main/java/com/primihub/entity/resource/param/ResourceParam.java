package com.primihub.entity.resource.param;

import lombok.Data;

import java.util.List;

@Data
public class ResourceParam extends PageParam {
    private String resourceId;
    private String resourceName;
    private Integer resourceType;
    private String organId;
    private List<String> organIds;
    private String tagName;
    private Integer fileContainsY;
    private String globalId;
}
