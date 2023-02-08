package com.primihub.entity.fusion.param;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FusionOrganExtendsParam {
    private String globalId;
    private String pinCode;
    private String ip;
    private BigDecimal lat;
    private BigDecimal lon;
    private String country;
}
