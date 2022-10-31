package com.primihub.biz.entity.data.dataenum;

import java.util.HashMap;
import java.util.Map;


public enum SourceEnum {
    mysql(1,"mysql"),
    ;
    private Integer sourceType;
    private String sourceName;
    public static Map<Integer, SourceEnum> SOURCE_MAP=new HashMap(){
        {
            for (SourceEnum e: SourceEnum.values()){
                put(e.sourceType,e);
            }
        }
    };

    SourceEnum(Integer sourceType, String sourceName) {
        this.sourceType = sourceType;
        this.sourceName = sourceName;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public String getSourceName() {
        return sourceName;
    }
}
