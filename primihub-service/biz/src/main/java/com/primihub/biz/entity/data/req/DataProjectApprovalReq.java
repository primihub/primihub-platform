package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class DataProjectApprovalReq {
    /**
     * 审批类型
     * 1、项目机构审批
     * 2、项目资源审批
     */
    private Integer type;

    /**
     * 审批ID
     * 1、项目机构审批ID
     * 2、项目资源审批ID
     */
    private Long id;

    /**
     * 审核状态 1同意 2拒绝
     */
    private Integer auditStatus;

    /**
     * 审核意见
     */
    private String auditOpinion;

    /**
     * 项目ID
     */
    private String projectId;
    /**
     * 机构ID
     */
    private String organId;


}
