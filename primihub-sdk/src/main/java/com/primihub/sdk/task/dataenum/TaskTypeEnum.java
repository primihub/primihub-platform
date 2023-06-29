package com.primihub.sdk.task.dataenum;

import com.primihub.sdk.task.factory.*;

import java.util.HashMap;
import java.util.Map;

public enum TaskTypeEnum {
    MODEL(1,"模型", AbstractComponentGRPCExecute.class),
    PSI(2,"psi", AbstractPsiGRPCExecute.class),
    PIR(3,"pir", AbstractPirGRPCExecute.class),
    REASONING(4,"推理",AbstractComponentGRPCExecute.class),
    JOINT_STATISTICAL(5,"联合统计", AbstractMPCGRPCExecute.class),
    DATA_SET(6,"数据集注册", AbstractDataSetGRPCExecute.class),
    ;
    private Integer taskType;
    private String taskName;
    private Class taskClass;
    public static Map<Integer, TaskTypeEnum> TASK_TYPE_MAP=new HashMap(){
        {
            for (TaskTypeEnum e:TaskTypeEnum.values()){
                put(e.taskType,e);
            }
        }
    };

    TaskTypeEnum(Integer taskType, String taskName,Class taskClass) {
        this.taskType = taskType;
        this.taskName = taskName;
        this.taskClass = taskClass;
    }

    public Integer getTaskType() {
        return taskType;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskType(Integer taskType) {
        this.taskType = taskType;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Class getTaskClass() {
        return taskClass;
    }

    public void setTaskClass(Class taskClass) {
        this.taskClass = taskClass;
    }
}
