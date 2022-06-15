package com.primihub.entity.resource.param;

import lombok.Data;

@Data
public class PageParam {
    private Integer pageNo = 1;
    private Integer pageSize = 5;
    private Integer offset;

    public Integer getOffset() {
        return (pageNo-1)*pageSize;
    }
}
