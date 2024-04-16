package com.primihub.biz.entity.data.dataenum;

public enum ComponentNodeEnum {
    START_NODE("start", "开始"),
    DATA_SET("dataSet", "选择数据集"),
    MODEL("model", "模型选择"),
    DATA_ALIGN("dataAlign", "数据对齐"),
    MPC_STATISTICS("jointStatistical", "联合统计"),
    FIT_TRANSFORM("fitTransform", "缺失值填充"),
    EXCEPTION("exception", "异常值处理"),
    ;

    private final String code;
    private final String name;

    ComponentNodeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }
}
