package com.primihub.biz.entity.data.req;

import lombok.Data;

/**
 * 分页请求参数
 */
@Data
public class PageReq {
    /** 页序号 */
    private Integer pageNo = 1;
    /** 页尺寸 */
    private Integer pageSize = 5;
    /** 起始数据偏移量 */
    private Integer offset;

    public Integer getOffset() {
        return (pageNo-1)*pageSize;
    }
}
