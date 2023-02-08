package com.primihub.biz.tool.nodedata;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressInfoEntity {
    private String ip;
    private BigDecimal lat;
    private BigDecimal lon;
    private String country;

    public AddressInfoEntity(String ip, BigDecimal lat, BigDecimal lon, String country) {
        this.ip = ip;
        this.lat = lat;
        this.lon = lon;
        this.country = country;
    }

    public AddressInfoEntity() {
    }
}
