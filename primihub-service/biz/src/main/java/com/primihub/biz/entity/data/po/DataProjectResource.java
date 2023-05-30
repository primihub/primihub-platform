package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 项目资源关系表
 * </p>
 *
 * @author 
 * @since 2022-06-22
 */
@Data
public class DataProjectResource {

    public DataProjectResource() {
    }

    public DataProjectResource(String prId, String projectId, String initiateOrganId, String organId, Integer participationIdentity) {
        this.prId = prId;
        this.projectId = projectId;
        this.initiateOrganId = initiateOrganId;
        this.organId = organId;
        this.participationIdentity = participationIdentity;
        this.auditStatus = 0;
    }

    /**
     * id
     */
    @JsonIgnore
    private Long id;

    /**
     * 项目资源ID  UUID
     */
    @JsonIgnore
    private String prId;

    /**
     * 项目id
     */
    private String projectId;

    /**
     * 发起方机构ID
     */
    private String initiateOrganId;

    /**
     * 机构ID
     */
    private String organId;

    /**
     * 机构项目中参与身份 1发起者 2协作者
     */
    private Integer participationIdentity;

    /**
     * 是否删除
     */
    private Integer isDel;

    /**
     * 资源ID
     */
    private String resourceId;

    /**
     * 审核状态 0审核中 1同意 2拒绝
     */
    private Integer auditStatus;

    /**
     * 审核意见
     */
    private String auditOpinion;

    /**
     * 创建时间
     */
    @JsonIgnore
    private Date createDate;

    /**
     * 修改时间
     */
    @JsonIgnore
    private Date updateDate;


}
