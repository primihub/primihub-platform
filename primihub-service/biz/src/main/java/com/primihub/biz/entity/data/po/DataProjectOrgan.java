package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 * 项目资源授权审核表
 * </p>
 *
 * @author 
 * @since 2022-06-22
 */
@Data
public class DataProjectOrgan {

    public DataProjectOrgan() {
    }

    public DataProjectOrgan(String poId, String projectId, String organId, String initiateOrganId, Integer participationIdentity) {
        this.poId = poId;
        this.projectId = projectId;
        this.organId = organId;
        this.initiateOrganId = initiateOrganId;
        this.participationIdentity = participationIdentity;
        this.auditStatus = 0;
    }

    /**
     * id
     */
    @JsonIgnore
    private Long id;

    /**
     * 项目机构关联ID UUID
     */
    @JsonIgnore
    private String poId;

    /**
     * 项目ID
     */
    private String projectId;

    /**
     * 机构ID
     */
    private String organId;

    /**
     * 发起方机构ID
     */
    private String initiateOrganId;

    /**
     * 机构项目中参与身份 1发起者 2协作者
     */
    private Integer participationIdentity;

    /**
     * 审核状态 0审核中 1同意 2拒绝
     */
    private Integer auditStatus;

    /**
     * 审核意见
     */
    private String auditOpinion;

    /**
     * 是否删除
     */
    private Integer isDel;

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
