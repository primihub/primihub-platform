package com.primihub.biz.tool.nodedata;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AddressInfoEntity {
    private String ip;
    private BigDecimal lat;
    private BigDecimal lon;
    private String country;
    private String city;
    private String region;

    public AddressInfoEntity(String ip, BigDecimal lat, BigDecimal lon, String country,String city, String region) {
        this.ip = ip;
        this.lat = lat;
        this.lon = lon;
        this.country = country;
        this.city = city;
        this.region = region;
    }

    public AddressInfoEntity() {
    }
}
