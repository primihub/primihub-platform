package com.primihub.biz.entity.data.po;


import lombok.Data;

import java.util.Date;

@Data
public class DataRT {
    private Long id;
    /**
     * 资源id
     */
    private Long resourceId;
    /**
     * 标签id
     */
    private Long tagId;
    /**
     * 是否删除
     */
    private boolean isDel;
    /**
     * 创建时间
     */
    private Date createDate;
    /**
     * 修改时间
     */
    private Date updateDate;

}
