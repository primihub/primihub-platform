package com.primihub.biz.entity.data.dataenum;

public enum FieldTypeEnum {
    STRING(0,"string"),
    INTEGER(1,"integer"),
    DOUBLE(2,"double"),
    LONG(3,"long"),
    ENUM(4,"enum"),
    BOOLEAN(5,"boolean"),
    ;

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
        return STRING.typeName;
    }

    public static FieldTypeEnum getEnumByTypeName(String typeName){
        for (FieldTypeEnum value : values()) {
            if (value.typeName.equals(typeName)){
                return value;
            }
        }
        return STRING;
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
