package com.primihub.sdk.task.param;

import com.primihub.sdk.task.annotation.TaskTypeExample;
import com.primihub.sdk.task.factory.AbstractPsiGRPCExecute;

import java.util.Arrays;
import java.util.List;

/**
 * psi 隐私求交组装类
 */
@TaskTypeExample(AbstractPsiGRPCExecute.class)
public class TaskPSIParam {
    /**
     * 发起方数据集ID
     */
    private String clientData;
    /**
     * 协作方数据集ID
     */
    private String serverData;
    /**
     * 输出内容 默认0 0交集 1差集
     */
    private Integer psiType = 0;
    /**
     *  0、ECDH
     *  1、KKRT
     *  2、TEE
     *  默认0
     */
    private Integer psiTag = 0;
    /**
     * 发起方数据集列的下标
     */
    private Integer[] clientIndex;
    /**
     * 协作方方数据集列的下标
     */
    private Integer[] serverIndex;
    /**
     * 文件输出路径
     */
    private String outputFullFilename;
    /**
     * 默认0 默认psi不开启，当进行数据对齐改为1
     */
    private Integer syncResultToServer = 0;
    /**
     * 数据对齐服务端存放地址
     */
    private String serverOutputFullFilname;

    private String teeData;

    public String getClientData() {
        return clientData;
    }

    public void setClientData(String clientData) {
        this.clientData = clientData;
    }

    public String getServerData() {
        return serverData;
    }

    public void setServerData(String serverData) {
        this.serverData = serverData;
    }

    public Integer getPsiType() {
        return psiType;
    }

    public void setPsiType(Integer psiType) {
        this.psiType = psiType;
    }

    public Integer getPsiTag() {
        return psiTag;
    }

    public void setPsiTag(Integer psiTag) {
        this.psiTag = psiTag;
    }

    public Integer[] getClientIndex() {
        return clientIndex;
    }

    public void setClientIndex(Integer[] clientIndex) {
        this.clientIndex = clientIndex;
    }

    public Integer[] getServerIndex() {
        return serverIndex;
    }

    public void setServerIndex(Integer[] serverIndex) {
        this.serverIndex = serverIndex;
    }

    public String getOutputFullFilename() {
        return outputFullFilename;
    }

    public void setOutputFullFilename(String outputFullFilename) {
        this.outputFullFilename = outputFullFilename;
    }

    public Integer getSyncResultToServer() {
        return syncResultToServer;
    }

    public void setSyncResultToServer(Integer syncResultToServer) {
        this.syncResultToServer = syncResultToServer;
    }

    public String getServerOutputFullFilname() {
        return serverOutputFullFilname;
    }

    public void setServerOutputFullFilname(String serverOutputFullFilname) {
        this.serverOutputFullFilname = serverOutputFullFilname;
    }

    public String getTeeData() {
        return teeData;
    }

    public void setTeeData(String teeData) {
        this.teeData = teeData;
    }

    @Override
    public String toString() {
        return "PsiTaskParam{" +
                ", clientData='" + clientData + '\'' +
                ", serverData='" + serverData + '\'' +
                ", psiType=" + psiType +
                ", psiTag=" + psiTag +
                ", clientIndex=" + Arrays.toString(clientIndex) +
                ", serverIndex=" + Arrays.toString(serverIndex) +
                ", outputFullFilename='" + outputFullFilename + '\'' +
                ", syncResultToServer='" + syncResultToServer + '\'' +
                ", serverOutputFullFilname='" + serverOutputFullFilname + '\'' +
                '}';
    }
}
