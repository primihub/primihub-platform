package com.primihub.biz.entity.data.po;

import lombok.Data;

import java.util.Date;


@Data
public class DataMpcTask {
    public DataMpcTask() {
    }

    public DataMpcTask(String taskIdName, Long scriptId, Long projectId, Long userId) {
        this.taskIdName = taskIdName;
        this.scriptId = scriptId;
        this.projectId = projectId;
        this.userId = userId;
        this.taskStatus = 0;
    }

    /**
     * 任务id
     */
    private Long taskId;

    /**
     * 任务id对外展示
     */
    private String taskIdName;

    /**
     * 脚本id
     */
    private Long scriptId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 任务状态 0未运行 1成功 2运行中 3失败
     */
    private Integer taskStatus;

    /**
     * 任务备注
     */
    private String taskDesc;

    /**
     * 日志信息
     */
    private String  logData;

    /**
     * 结果文件地址
     */
    private String resultFilePath;

    /**
     * 是否删除
     */
    private Integer isDel;

    /**
     * 创建时间
     */
    private Date createDate;

    /**
     * 修改时间
     */
    private Date updateDate;


}
