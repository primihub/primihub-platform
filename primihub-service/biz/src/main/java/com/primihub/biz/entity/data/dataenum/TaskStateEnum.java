package com.primihub.biz.entity.data.dataenum;

import java.util.HashMap;
import java.util.Map;


public enum TaskStateEnum {
    INIT(0,"初始未开始"),
    SUCCESS(1,"成功"),
    IN_OPERATION(2,"运行中"),
    FAIL(3,"失败"),
    CANCEL(4,"取消"),
    DELETE(5,"删除"),
    ;
    private Integer stateType;
    private String stateDesc;
    public static Map<Integer, TaskStateEnum> TASK_STATE_MAP=new HashMap(){
        {
            for (TaskStateEnum e:TaskStateEnum.values()){
                put(e.stateType,e);
            }
        }
    };

    TaskStateEnum(Integer stateType, String stateDesc) {
        this.stateType = stateType;
        this.stateDesc = stateDesc;
    }

    public Integer getStateType() {
        return stateType;
    }

    public String getStateDesc() {
        return stateDesc;
    }
}
