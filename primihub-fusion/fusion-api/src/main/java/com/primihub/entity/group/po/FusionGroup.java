package com.primihub.entity.group.po;

import lombok.Data;

import java.util.Date;

@Data
public class FusionGroup {
    private Long id;
    private String groupName;
    private String groupOrganId;
    private Integer isDel;
    private Date cTime;
    private Date uTime;
}
