package com.primihub.biz.entity.data.po;

import lombok.Data;

import java.util.Date;

@Data
public class DataResourceVisibilityAuth {
    private Long id;
    private Long resourceId;
    private String organGlobalId;
    private String organName;
    private Integer isDel;
    private Date cTime;
    private Date uTime;

    public DataResourceVisibilityAuth() {
    }

    public DataResourceVisibilityAuth(Long resourceId, String organGlobalId, String organName) {
        this.resourceId = resourceId;
        this.organGlobalId = organGlobalId;
        this.organName = organName;
        this.isDel = 0;
    }
}
