package com.primihub.biz.entity.data.po;

import lombok.Data;

import java.util.Date;

/**
 * 项目资源授权审核表
 */
@Data
public class DataResourceAuthRecord {
    public DataResourceAuthRecord() {
    }

    public DataResourceAuthRecord(Long projectId, Long resourceId) {
        this.projectId = projectId;
        this.resourceId = resourceId;
        this.recordStatus = 0;
        this.userId=0L;
        this.userName="";
    }

    /**
     * 审核id
     */
    private Long recordId;
    /**
     * 审核状态 0.未审核 1.已审核
     */
    private Integer recordStatus;
    /**
     * 审核人员id
     */
    private Long userId;
    /**
     * 审核人员姓名
     */
    private String userName;
    /**
     * 项目id
     */
    private Long projectId;
    /**
     * 资源id
     */
    private Long resourceId;
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
