package com.yyds.biz.entity.data.req;

import lombok.Data;

@Data
public class PageReq {
    private Integer pageNo = 1;
    private Integer pageSize = 5;
    private Integer offset;

    public Integer getOffset() {
        return (pageNo-1)*pageSize;
    }
}
