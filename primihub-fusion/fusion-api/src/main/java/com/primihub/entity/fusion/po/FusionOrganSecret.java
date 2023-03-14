package com.primihub.entity.fusion.po;

import lombok.Data;

import java.util.Date;

@Data
public class FusionOrganSecret {
    private Long id;
    private Long globalId;
    private String publicKey;
    private String privateKey;
    private Integer isDel;
    private Date cTime;
    private Date uTime;
}
