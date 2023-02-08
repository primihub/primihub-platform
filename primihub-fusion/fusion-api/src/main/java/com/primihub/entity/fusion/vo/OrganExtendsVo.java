package com.primihub.entity.fusion.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrganExtendsVo {
    private String globalId;
    private String globalName;
    /**
     * 纬度
     */
    private BigDecimal lat;

    /**
     * 经度
     */
    private BigDecimal lon;

    /**
     * 区域
     */
    private String country;
}
