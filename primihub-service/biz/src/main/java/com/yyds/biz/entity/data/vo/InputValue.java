package com.yyds.biz.entity.data.vo;
import lombok.Data;

@Data
public class InputValue{
    private String key;
    private String val;

    public InputValue(String key, String val) {
        this.key = key;
        this.val = val;
    }

    public InputValue() {
    }
}