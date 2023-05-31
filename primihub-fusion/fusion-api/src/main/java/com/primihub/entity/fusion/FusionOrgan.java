package com.primihub.entity.fusion;

import lombok.Data;

import java.util.Date;

@Data
public class FusionOrgan {
    private Long id;
    private String globalId;
    private String globalName;
    private Date registerTime;
    private Integer isDel;
    private Date cTime;
    private Date uTime;
}
