package com.yyds.biz.entity.sys.param;

import lombok.Data;

@Data
public class AlterOrganNodeStatusParam {
    /**
     * 机构id
     */
    private Long organId;
    /**
     * 机构名称
     */
    private String organName;
}
