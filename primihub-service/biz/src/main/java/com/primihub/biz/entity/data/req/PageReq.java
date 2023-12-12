package com.primihub.biz.entity.data.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("列表分页参数")
public class PageReq {
    @ApiModelProperty(value = "第几页",example = "1")
    private Integer pageNo = 1;
    @ApiModelProperty(value = "每页条数",example = "20")
    private Integer pageSize = 5;

    private Integer offset;

    public Integer getOffset() {
        return (pageNo-1)*pageSize;
    }
}
