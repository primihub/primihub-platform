package com.primihub.biz.entity.data.req;

import lombok.Data;

/**
 * 匿踪任务列表查询请求条件
 */
@Data
public class DataPirTaskReq extends PageReq {
    /** 机构名称 */
    private String organName;
    /** 资源名称 */
    private String resourceName;
    /** 检索内容 */
    private String retrievalId;
    /** 任务状态(0未开始 1成功 2查询中 3失败) */
    private Integer taskState;
    /** 任务ID */
    private String taskId;
    /** 任务名称 */
    private String taskName;
    /** 开始日期 */
    private String startDate;
    /** 结束日期 */
    private String endDate;

}
