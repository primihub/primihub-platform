package com.primihub.biz.entity.data.dataenum;

public enum PSIEnum {
    TYPE_INTERSECTION(0, "交集"),
    TYPE_DIFFERENCE(1, "差集"),
    TAG_ECDH(0, "ECDH"),
    TAG_KKRT(1, "KKRT"),
    TAG_TEE(2, "TEE"),

    SYNC_OFF(0, "PSI默认"),
    SYNC_ON(1, "PSI数据对齐开启"),
    ;

    private final int code;
    private final String desc;

    PSIEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
