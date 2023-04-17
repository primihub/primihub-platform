package com.primihub.biz.entity.data.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModelComponentType {
    private String typeCode;
    private String typeName;
    private String inputType;
    private String inputValue;
    private Integer isRequired;
    private String parentValue;
    private List<InputValue> inputValues = new ArrayList<>();
}


