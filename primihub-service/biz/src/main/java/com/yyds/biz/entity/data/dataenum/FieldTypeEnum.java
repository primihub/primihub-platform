package com.yyds.biz.entity.data.dataenum;

public enum FieldTypeEnum {
    STRING(0,"string");

    private Integer code;
    private String typeName;

    FieldTypeEnum(Integer code, String typeName) {
        this.code = code;
        this.typeName = typeName;
    }

    public static String getTypeName(Integer code){
        for (FieldTypeEnum value : values()) {
            if (value.code==code){
                return value.typeName;
            }
        }
        return "";
    }

    public static FieldTypeEnum getEnumByTypeName(String typeName){
        for (FieldTypeEnum value : values()) {
            if (value.typeName.equals(typeName)){
                return value;
            }
        }
        return null;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }
}
