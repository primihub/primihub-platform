package com.primihub.entity.group.po;

import lombok.Data;

import java.util.Date;

@Data
public class FusionGo {
    private Long id;
    private Long groupId;
    private String organGlobalId;
    private Integer isDel;
    private Date cTime;
    private Date uTime;
}
