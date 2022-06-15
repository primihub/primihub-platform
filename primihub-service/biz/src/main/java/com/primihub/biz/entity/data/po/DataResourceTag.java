package com.primihub.biz.entity.data.po;

import lombok.Data;

import java.util.Date;

@Data
public class DataResourceTag {

    public DataResourceTag(String tagName) {
        this.tagName = tagName;
    }

    public DataResourceTag() {
    }

    /**
     * 标签id
     */
    private Long tagId;
    /**
     * 标签名称
     */
    private String tagName;
    /**
     * 是否删除
     */
    private Integer isDel;
    /**
     * 创建时间
     */
    private Date createDate;
    /**
     * 修改时间
     */
    private Date updateDate;
}
