package com.primihub.biz.entity.data.po;

import lombok.Data;

import java.util.Date;


/**
 * 项目资源关系表
 */
@Data
public class DataProjectResource {
    public DataProjectResource() {
    }

    public DataProjectResource(Long projectId, Long resourceId) {
        this.projectId = projectId;
        this.resourceId = resourceId;
    }

    public DataProjectResource(Long projectId, Long resourceId,boolean isHold) {
        this.projectId = projectId;
        this.resourceId = resourceId;
        // isHold 资源持有者 自动授权
        if (isHold){
            this.isAuthed = 1;
        }else {
            this.isAuthed = 0;
        }
    }

    /**
     * 自增id
     */
    private Long id;
    /**
     * 项目id
     */
    private Long projectId;
    /**
     * 资源id
     */
    private Long resourceId;
    /**
     * 是否授权
     */
    private Integer isAuthed;
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
