package com.primihub.biz.entity.sys.param;

import lombok.Data;

@Data
public class FindOrganPageParam {
    /**
     * 父机构id
     */
    private Long pOrganId;
    /**
     * 机构名称
     */
    private String organName;

}
