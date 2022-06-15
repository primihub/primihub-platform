package com.primihub.entity.copy.enumeration;

import com.primihub.entity.copy.dto.CopyResourceDto;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum CopyEnum {

    RSOURCE("data_resource",CopyResourceDto.class,"resourceService","batchSaveResource");
    ;

    private String tableName;
    private Class clazz;
    private String beanName;
    private String functionName;

    public static Map<String, CopyEnum> FUSION_COPY_MAP=new HashMap(){
        {
            for (CopyEnum e:CopyEnum.values()){
                put(e.tableName,e);
            }
        }
    };

    CopyEnum(String tableName, Class clazz, String beanName, String functionName) {
        this.tableName = tableName;
        this.clazz = clazz;
        this.beanName = beanName;
        this.functionName = functionName;
    }
}
