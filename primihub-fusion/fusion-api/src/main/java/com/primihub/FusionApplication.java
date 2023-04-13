package com.primihub;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySources;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@NacosPropertySources({@NacosPropertySource(dataId = "database.yaml" ,autoRefreshed = true)})
@SpringBootApplication
public class FusionApplication {
    public static void main(String[] args){
        SpringApplication.run(FusionApplication.class, args);
    }
}
