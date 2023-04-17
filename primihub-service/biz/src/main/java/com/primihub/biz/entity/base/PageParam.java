package com.primihub.biz.entity.base;

import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Data
public class PageParam {
    private Integer pageNum;
    private Integer pageSize;
    private Integer pageIndex;
    private Long itemTotalCount;
    private Long pageCount;
    private Boolean isLoadMore;

    public PageParam(Integer pageNum, Integer pageSize) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pageIndex = (pageNum-1)*pageSize;
    }

    public void initItemTotalCount(Long totalCount){
        this.itemTotalCount=totalCount;
        this.pageCount=new BigDecimal(totalCount).divide(new BigDecimal(pageSize),0, RoundingMode.UP).longValue();
    }

    public Boolean isLoadMore(List list){
        isLoadMore=true;
        if(list==null||list.size()<=pageSize) {
            isLoadMore=false;
        } else {
            list.remove(list.size()-1);
        }
        return isLoadMore;
    }

}
