package com.yyds.biz.entity.base;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum BaseFunctionHandleEnum {
    Test("test","testService","testTask"),
    DATA_RESOURCE_FILE_TASK("dataResourceFileTask","dataResourceTaskService","handleDataResourceFile"),
    ;

    private String handleType;
    private String beanName;
    private String functionName;

    public static Map<String,BaseFunctionHandleEnum> FUNCTION_MAP=new HashMap(){
        {
            for (BaseFunctionHandleEnum e:BaseFunctionHandleEnum.values()){
                put(e.handleType,e);
            }
        }
    };

    BaseFunctionHandleEnum(String handleType, String beanName, String functionName) {
        this.handleType = handleType;
        this.beanName = beanName;
        this.functionName = functionName;
    }
}
