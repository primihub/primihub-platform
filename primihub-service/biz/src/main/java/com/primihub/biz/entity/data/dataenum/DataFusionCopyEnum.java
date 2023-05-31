package com.primihub.biz.entity.data.dataenum;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum DataFusionCopyEnum {
    RESOURCE("data_resource","dataResourceService","findCopyResourceList"),
    FUSION_RESOURCE("fusion_resource","dataResourceService","findFusionCopyResourceList"),
    ;

    private String tableName;
    private String beanName;
    private String functionName;

    public static Map<String, DataFusionCopyEnum> FUSION_COPY_MAP=new HashMap(){
        {
            for (DataFusionCopyEnum e:DataFusionCopyEnum.values()){
                put(e.tableName,e);
            }
        }
    };

    DataFusionCopyEnum(String tableName, String beanName, String functionName) {
        this.tableName = tableName;
        this.beanName = beanName;
        this.functionName = functionName;
    }
}
