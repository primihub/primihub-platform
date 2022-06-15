package com.primihub.biz.entity.sys.param;

import lombok.Data;

@Data
public class CreateOrganNodeParam {

    /**
     * 机构名称
     */
    private String organName;
    /**
     * 父机构Id
     */
    private Long pOrganId;
    /**
     * 顺序
     */
    private Integer organIndex;
}
