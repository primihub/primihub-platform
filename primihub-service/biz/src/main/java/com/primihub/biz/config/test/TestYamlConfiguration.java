package com.primihub.biz.config.test;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class TestYamlConfiguration {

//    @NacosValue(value="${third}",autoRefreshed = true)
    private String third;
//    @NacosValue(value="${forth.id}",autoRefreshed = true)
    private String id;
}
