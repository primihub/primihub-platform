package com.primihub.biz.entity.data.req;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DataModelPointComponent {
    private String frontComponentId;
    private String shape;
    private Map<String,String> input;
    private Map<String,String> output;
}
