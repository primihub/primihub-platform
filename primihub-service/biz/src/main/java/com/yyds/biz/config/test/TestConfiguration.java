package com.yyds.biz.config.test;

import com.alibaba.nacos.api.config.annotation.NacosConfigurationProperties;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@NacosConfigurationProperties(dataId = "test",autoRefreshed = true)
public class TestConfiguration {

    private String first;
    private String second;

}
