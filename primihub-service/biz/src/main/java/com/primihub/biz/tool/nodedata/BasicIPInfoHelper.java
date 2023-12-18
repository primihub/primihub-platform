package com.primihub.biz.tool.nodedata;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Slf4j
public class BasicIPInfoHelper {
    private RestTemplate restTemplate;

    public BasicIPInfoHelper(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public AddressInfoEntity getAddressInfo(){
        // 获取客户端ip
        ResponseEntity<JSONObject> ipInfoData = restTemplate.getForEntity("http://ipinfo.io", JSONObject.class);
        log.info("获取客户端ip json:{}",JSONObject.toJSONString(ipInfoData));
        if (ipInfoData==null || ipInfoData.getBody()==null){
            return null;
        }
        JSONObject body = ipInfoData.getBody();
        String ip = ipInfoData.getBody().getString("ip");
        String loc = body.getString("loc");
        if (StringUtils.isBlank(loc)){
            return null;
        }
        String[] locs = loc.split(",");
        if (locs.length!=2){
            return null;
        }
        String lat = locs[0];
        String lon = locs[1];
        String country = body.getString("country");
        if (StringUtils.isBlank(lat)||StringUtils.isBlank(lon)||StringUtils.isBlank(country)){
            return null;
        }
        String city = body.getString("city");
        String region = body.getString("region");
        return new AddressInfoEntity(ip,new BigDecimal(lat),new BigDecimal(lon),country,city,region);
    }

    public static void main(String[] args) {
        RestTemplate restTemplate = new RestTemplate();
        BasicIPInfoHelper basicIPInfoHelper = new BasicIPInfoHelper(restTemplate);
        AddressInfoEntity addressInfo = basicIPInfoHelper.getAddressInfo();
        System.out.println(JSONObject.toJSONString(addressInfo));
    }
}
