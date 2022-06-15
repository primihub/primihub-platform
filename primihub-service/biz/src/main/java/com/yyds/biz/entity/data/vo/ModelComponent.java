package com.yyds.biz.entity.data.vo;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModelComponent {
    private String componentCode;
    private String componentName;
    /**
     * 0 展示 1 不展示
     */
    private int isShow;
    /**
     * 0 必选 1非必选
     */
    private int isMandatory;
    private List<ModelComponentType> componentTypes=new ArrayList<>();
}
