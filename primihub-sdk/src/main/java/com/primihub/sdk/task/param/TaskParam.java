package com.primihub.sdk.task.param;

import java.util.UUID;

public class TaskParam<T> {
    private String requestId = UUID.randomUUID().toString().replace("-","");
    private String taskId;
    private String jobId;
    private Boolean isSuccess = true;
    private Boolean isEnd = false;
    private String error;
    private Integer partyCount;
    private Boolean isOpenGetStatus = true;
    private T taskContentParam;

    public TaskParam(T taskContentParam) {
        this.taskContentParam = taskContentParam;
    }

    public TaskParam() {
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getJobId() {
        if (jobId == null || "".equals(jobId)){
            return "1";
        }
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Boolean getSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean success) {
        isSuccess = success;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getPartyCount() {
        return partyCount;
    }

    public void setPartyCount(Integer partyCount) {
        this.partyCount = partyCount;
    }

    public Boolean getEnd() {
        return isEnd;
    }

    public void setEnd(Boolean end) {
        isEnd = end;
    }

    public Boolean getOpenGetStatus() {
        return isOpenGetStatus;
    }

    public void setOpenGetStatus(Boolean openGetStatus) {
        isOpenGetStatus = openGetStatus;
    }

    public T getTaskContentParam() {
        return taskContentParam;
    }

    public void setTaskContentParam(T taskContentParam) {
        this.taskContentParam = taskContentParam;
    }

    @Override
    public String toString() {
        return "TaskParam{" +
                "requestId='" + requestId + '\'' +
                ", taskId='" + taskId + '\'' +
                ", jobId='" + jobId + '\'' +
                ", isSuccess=" + isSuccess +
                ", isEnd=" + isEnd +
                ", error='" + error + '\'' +
                ", partyCount=" + partyCount +
                ", isOpenGetStatus=" + isOpenGetStatus +
                ", taskContentParam=" + taskContentParam +
                '}';
    }
}
