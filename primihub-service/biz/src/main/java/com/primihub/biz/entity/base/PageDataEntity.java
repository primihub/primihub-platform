package com.primihub.biz.entity.base;

import java.util.List;

public class PageDataEntity {

    public PageDataEntity(int total, int pageSize, int index, List data) {
        this.total = total;
        this.pageSize = pageSize;
        this.index = index;
        this.data = data;
    }

    /**
     * 总共的数据量
     */
    private int total;
    /**
     * 每页显示多少条
     */
    private int pageSize;
    /**
     * 共有多少页
     */
    private int totalPage;
    /**
     * 当前是第几页
     */
    private int index;
    /**
     * 数据
     */
    private List data;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getTotalPage() {
        return total % pageSize == 0 ? total / pageSize : total / pageSize + 1;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public List getData() {
        return data;
    }

    public void setData(List data) {
        this.data = data;
    }
}
