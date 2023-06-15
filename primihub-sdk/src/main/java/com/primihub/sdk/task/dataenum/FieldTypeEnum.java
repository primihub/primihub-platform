package com.primihub.sdk.task.dataenum;


import java.util.HashMap;
import java.util.Map;

public enum FieldTypeEnum {
    STRING(0,"String","STRING"),
    INTEGER(1,"Integer","INT32"),
    DOUBLE(2,"Double","DOUBLE"),
    LONG(3,"Long","INT64"),
//    ENUM(4,"Enum"),
    BOOLEAN(5,"Boolean","BOOL"),
    ;

    private Integer code;
    private String typeName;
    private String nodeTypeName;

    FieldTypeEnum(Integer code, String typeName, String nodeTypeName) {
        this.code = code;
        this.typeName = typeName;
        this.nodeTypeName = nodeTypeName;
    }

    public static Map<Integer, FieldTypeEnum> FIELD_TYPE_MAP=new HashMap(){
        {
            for (FieldTypeEnum e:FieldTypeEnum.values()){
                put(e.code,e);
            }
        }
    };

    public static String getTypeName(Integer code){
        for (FieldTypeEnum value : values()) {
            if (value.code.equals(code)){
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

    public String getNodeTypeName() {
        return nodeTypeName;
    }

    public void setNodeTypeName(String nodeTypeName) {
        this.nodeTypeName = nodeTypeName;
    }
}
