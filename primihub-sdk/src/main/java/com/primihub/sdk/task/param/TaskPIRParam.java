package com.primihub.sdk.task.param;

import com.primihub.sdk.task.annotation.TaskTypeExample;
import com.primihub.sdk.task.factory.AbstractPirGRPCExecute;

/**
 * pir 匿踪查询组装类
 */
@TaskTypeExample(AbstractPirGRPCExecute.class)
public class TaskPIRParam {
    /**
     * 查询key
     */
    private String[] queryParam;
    /**
     * 协作方数据集ID
     */
    private String serverData;
    /**
     * 默认
     */
    private Integer pirType = 1;
    /**
     * 文件内容输出路径
     */
    private String outputFullFilename;

    public String[] getQueryParam() {
        return queryParam;
    }

    public void setQueryParam(String[] queryParam) {
        this.queryParam = queryParam;
    }

    public String getServerData() {
        return serverData;
    }

    public void setServerData(String serverData) {
        this.serverData = serverData;
    }

    public Integer getPirType() {
        return pirType;
    }

    public void setPirType(Integer pirType) {
        this.pirType = pirType;
    }

    public String getOutputFullFilename() {
        return outputFullFilename;
    }

    public void setOutputFullFilename(String outputFullFilename) {
        this.outputFullFilename = outputFullFilename;
    }

    @Override
    public String toString() {
        return "PirTaskParam{" +
                ", queryParam='" + queryParam + '\'' +
                ", serverData='" + serverData + '\'' +
                ", pirType=" + pirType +
                ", outputFullFilename='" + outputFullFilename + '\'' +
                '}';
    }
}
