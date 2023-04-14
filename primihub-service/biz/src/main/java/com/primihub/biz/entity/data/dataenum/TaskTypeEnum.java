package com.primihub.biz.entity.data.dataenum;

import java.util.HashMap;
import java.util.Map;

public enum TaskTypeEnum {
    MODEL(1,"模型"),
    PSI(2,"psi"),
    PIR(3,"pir"),
    REASONING(4,"推理"),
    JOINT_STATISTICAL(5,"联合统计"),
    ;
    private Integer taskType;
    private String taskName;
    public static Map<Integer, TaskTypeEnum> TASK_TYPE_MAP=new HashMap(){
        {
            for (TaskTypeEnum e:TaskTypeEnum.values()){
                put(e.taskType,e);
            }
        }
    };

    TaskTypeEnum(Integer taskType, String taskName) {
        this.taskType = taskType;
        this.taskName = taskName;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public String getTaskName() {
        return taskName;
    }
}
