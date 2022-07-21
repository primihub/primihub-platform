package com.primihub.biz.entity.data.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ModelProjectOrganVo {
    /**
     * 是否创建者
     */
    private Boolean creator = false;
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
     * 资源数据
     */
    private List<ModelProjectResourceVo> resources = new ArrayList<>();
}
