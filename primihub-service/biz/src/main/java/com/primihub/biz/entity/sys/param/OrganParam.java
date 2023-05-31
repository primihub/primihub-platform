package com.primihub.biz.entity.sys.param;

import com.primihub.biz.entity.data.req.PageReq;
import lombok.Data;

@Data
public class OrganParam extends PageReq {
    private String organId;
    private String organName;
    /**
     * 0待审批 1同意 2拒绝
     */
    private Integer examineState;
}
