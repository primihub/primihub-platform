package com.primihub.sdk.task.param;

import com.primihub.sdk.task.annotation.TaskTypeExample;
import com.primihub.sdk.task.factory.AbstractMPCGRPCExecute;

import java.util.List;
import java.util.Map;

@TaskTypeExample(AbstractMPCGRPCExecute.class)
public class TaskMPCParam {
    private List<String> resourceIds;
    private String taskName = "MPC";
    private String taskCode = "MPC";
    private Map<String,Object> paramMap;

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

    public Map<String, Object> getParamMap() {
        return paramMap;
    }

    public void setParamMap(Map<String, Object> paramMap) {
        this.paramMap = paramMap;
    }

    public String getTaskCode() {
        return taskCode;
    }

    public void setTaskCode(String taskCode) {
        this.taskCode = taskCode;
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
