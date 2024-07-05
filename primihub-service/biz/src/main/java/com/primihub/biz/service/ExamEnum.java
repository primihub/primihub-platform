package com.primihub.biz.service;

import java.util.HashMap;
import java.util.Map;

public enum ExamEnum {
    ;
    private String targetValue;
    private String executeService;

    public static Map<String, String> EXAM_TYPE_MAP = new HashMap<String, String>() {
        {
            for (ExamEnum e : ExamEnum.values()) {
                put(e.targetValue, e.executeService);
            }
        }
    };

    ExamEnum(String targetValue, String executeService) {
        this.targetValue = targetValue;
        this.executeService = executeService;
    }
}
