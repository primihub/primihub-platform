package com.primihub.biz.entity.data.dataenum;

import com.primihub.biz.service.data.db.impl.MySqlServiceAbstract;
import com.primihub.biz.service.data.db.impl.OtherServiceAbstract;
import com.primihub.biz.service.data.db.impl.SqliteServiceAbstract;

import java.util.HashMap;
import java.util.Map;


public enum SourceEnum {
    mysql(1,"mysql", MySqlServiceAbstract.class),
    sqlite(2,"sqlite", SqliteServiceAbstract.class),
    hive(3,"hive", OtherServiceAbstract.class),
    dm(4,"dm", OtherServiceAbstract.class),
    sql_server(5,"SqlServer", OtherServiceAbstract.class),
    oracle(6,"oracle", OtherServiceAbstract.class),
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
