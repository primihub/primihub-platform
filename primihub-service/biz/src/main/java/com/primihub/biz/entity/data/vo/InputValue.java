package com.primihub.biz.entity.data.vo;
import lombok.Data;

import java.util.List;

@Data
public class InputValue{
    private String key;
    private String val;
    private List<ModelComponentType> param;

    public InputValue(String key, String val) {
        this.key = key;
        this.val = val;
    }

    public InputValue() {
    }
}