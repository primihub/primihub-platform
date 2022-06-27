package com.primihub.biz.entity.base;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum BaseFunctionHandleEnum {
    Test("test","testService","testTask"),
    BATCH_DATA_FUSION_RESOURCE_TASK("batchDataFusionResource","dataTaskService","batchDataFusionResource"),
    SINGLE_DATA_FUSION_RESOURCE_TASK("singleDataFusionResource","dataTaskService","singleDataFusionResource"),
    SPREAD_PROJECT_DATA_TASK("spreadProjectData","dataTaskService","spreadProjectData"),
    COMPARE_AND_FIX_LOCAL_ORGAN_NAME_TASK("compareAndFixLocalOrganName","dataTaskService","compareAndFixLocalOrganName"),
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
