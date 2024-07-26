package com.primihub.biz.entity.data.dataenum;

import com.primihub.biz.constant.RemoteConstant;

import java.util.HashMap;
import java.util.Map;

public enum ExamEnum {
    ID_NUM(RemoteConstant.INPUT_FIELD_ID_NUM, "examExecuteIdNum"),
    IMEI(RemoteConstant.INPUT_FIELD_IMEI, "examExecuteImei"),
    PHONE_NUM(RemoteConstant.INPUT_FIELD_PHONE, "examExecutePhoneNum"),
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
