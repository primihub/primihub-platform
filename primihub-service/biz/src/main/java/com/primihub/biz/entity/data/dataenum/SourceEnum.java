package com.primihub.biz.entity.data.dataenum;

import com.primihub.biz.service.data.db.impl.MySqlService;
import com.primihub.biz.service.data.db.impl.SqliteService;

import java.util.HashMap;
import java.util.Map;


public enum SourceEnum {
    mysql(1,"mysql", MySqlService.class),
    sqlite(2,"sqlite", SqliteService.class),
    ;
    private Integer sourceType;
    private String sourceName;
    private Class sourceServiceClass;
    public static Map<Integer, SourceEnum> SOURCE_MAP=new HashMap(){
        {
            for (SourceEnum e: SourceEnum.values()){
                put(e.sourceType,e);
            }
        }
    };

    SourceEnum(Integer sourceType, String sourceName,Class sourceServiceClass) {
        this.sourceType = sourceType;
        this.sourceName = sourceName;
        this.sourceServiceClass = sourceServiceClass;
    }

    public Integer getSourceType() {
        return sourceType;
    }

    public String getSourceName() {
        return sourceName;
    }

    public Class getSourceServiceClass() {
        return sourceServiceClass;
    }
}
