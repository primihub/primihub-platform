package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class DataComponentRelationReq {
    private String componentId;
    private String portId;
    private String componentCode;
    private String pointType;
    private String pointJson;
}
