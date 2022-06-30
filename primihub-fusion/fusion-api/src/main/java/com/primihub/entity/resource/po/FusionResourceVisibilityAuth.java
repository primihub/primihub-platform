package com.primihub.entity.resource.po;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@Data
public class FusionResourceVisibilityAuth {
    private Long id;
    private String resourceId;
    private String organGlobalId;
    private Integer isDel;
    private Date cTime;
    private Date uTime;

    public FusionResourceVisibilityAuth(String resourceId, String organGlobalId) {
        this.resourceId = resourceId;
        this.organGlobalId = organGlobalId;
        this.isDel = 0;
    }
}
