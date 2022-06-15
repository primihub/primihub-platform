package com.primihub.biz.entity.data.req;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DataComponentReq {
    private String componentId;
    private String frontComponentId;
    private String componentCode;
    private String componentName;
    private Integer coordinateY;
    private Integer coordinateX;
    private Integer width;
    private Integer height;
    private String shape;
    private List<DataComponentValue> componentValues;
    private List<DataComponentRelationReq> input = new ArrayList<>();
    private List<DataComponentRelationReq> output = new ArrayList<>();

}
