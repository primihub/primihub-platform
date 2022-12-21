package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;


@Data
public class DataResourceAuthRecordVo {
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
    private Long userId=0L;
    /**
     * 审核人员姓名
     */
    private String userName="";
    /**
     * 项目id
     */
    private Long projectId;

    /**
     * 项目名称
     */
    private String projectName;
    /**
     * 资源id
     */
    private Long resourceId;

    /**
     * 资源名称
     */
    private String resourceName;

    /**
     * 机构id
     */
    private Long organId;
    /**
     * 机构名称
     */
    private String organName;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createDate;
}
