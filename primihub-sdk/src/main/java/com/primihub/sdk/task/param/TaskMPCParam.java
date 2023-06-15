package com.primihub.sdk.task.param;

import com.primihub.sdk.task.annotation.TaskTypeExample;
import com.primihub.sdk.task.factory.AbstractMPCGRPCExecute;

import java.util.List;
import java.util.Map;

@TaskTypeExample(AbstractMPCGRPCExecute.class)
public class TaskMPCParam {
    private List<String> resourceIds;
    private String taskName = "MPC";
    private Map<String,String> paramMap;

    public List<String> getResourceIds() {
        return resourceIds;
    }

    public void setResourceIds(List<String> resourceIds) {
        this.resourceIds = resourceIds;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Map<String, String> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, String> paramMap) {
        this.paramMap = paramMap;
    }

    @Override
    public String toString() {
        return "MPCTaskParam{" +
                "resourceIds=" + resourceIds +
                ", taskName='" + taskName + '\'' +
                ", paramMap=" + paramMap +
                '}';
    }
}
