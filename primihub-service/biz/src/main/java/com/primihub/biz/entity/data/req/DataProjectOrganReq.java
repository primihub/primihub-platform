package com.primihub.biz.entity.data.req;

import lombok.Data;

import java.util.List;


@Data
public class DataProjectOrganReq {
    /**
     * 机构ID
     */
    private String organId;
    /**
     * 机构项目中参与身份 1发起者 2协作者
     */
    private Integer participationIdentity;

    /**
     * 资源列表
     */
    private List<String> resourceIds;
}
