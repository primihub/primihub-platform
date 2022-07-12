package com.primihub.biz.entity.data.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DataProjectOrganVo {
    /**
     * 本地真实ID
     */
    private Long id;
    /**
     * 项目id
     */
    private String projectId;
    /**
     * 是否创建者
     */
    private Boolean creator = false;
    /**
     * 是否本机构
     */
    private Boolean thisInstitution = false;
    /**
     * 机构项目中参与身份 1发起者 2协作者
     */
    private Integer participationIdentity;
    /**
     * 机构id
     */
    private String organId;
    /**
     * 机构名称
     */
    private String organName;
    /**
     * 审核状态 0审核中 1同意 2拒绝
     */
    private Integer auditStatus;
    /**
     * 审核意见
     */
    private String auditOpinion;

    private List<DataProjectResourceVo> resources;

}
