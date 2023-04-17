package com.primihub.entity.fusion.po;

import lombok.Data;

import java.util.Date;

@Data
public class FusionOrgan {
    private Long id;
    private String globalId;
    private String globalName;
    private String pinCodeMd;
    private String gatewayAddress;
    private Date registerTime;
    private String publicKey;
    private String privateKey;
    private Integer isDel;
    private Date cTime;
    private Date uTime;
}
