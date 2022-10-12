package com.primihub.biz.entity.data.po;

import lombok.Data;

import java.util.Date;

@Data
public class DataComponentDraft {

    private Long draftId;

    private String draftName;

    private Long userId;

    private String componentJson;

    private String componentImage;

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
