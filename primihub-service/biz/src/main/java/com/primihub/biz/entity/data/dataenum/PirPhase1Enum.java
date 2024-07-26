package com.primihub.biz.entity.data.dataenum;

import com.primihub.biz.constant.RemoteConstant;

import java.util.HashMap;
import java.util.Map;

public enum PirPhase1Enum {
    ID_NUM(RemoteConstant.INPUT_FIELD_ID_NUM, "pirPhase1ExecuteIdNum"),
    IMEI(RemoteConstant.INPUT_FIELD_IMEI, "pirPhase1ExecuteImei"),
    PHONE_NUM(RemoteConstant.INPUT_FIELD_PHONE, "pirPhase1ExecutePhoneNum"),
    ;
    private String targetValue;
    private String executeService;

    public static Map<String, String> PIR_PHASE1_TYPE_MAP = new HashMap<String, String>() {
        {
            for (PirPhase1Enum e : PirPhase1Enum.values()) {
                put(e.targetValue, e.executeService);
            }
        }
    };

    PirPhase1Enum(String targetValue, String executeService) {
        this.targetValue = targetValue;
        this.executeService = executeService;
    }
}
