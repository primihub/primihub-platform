package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class DataPirTaskReq extends PageReq {

    private String serverAddress;

    private String organName;

    private String resourceName;

    private String retrievalId;

    /**
     * 任务状态(0未开始 1成功 2查询中 3失败)
     */
    private Integer taskState;

    private String taskIdName;

}
