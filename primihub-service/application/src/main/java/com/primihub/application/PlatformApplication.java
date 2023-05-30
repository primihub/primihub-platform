package com.primihub.application;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySources;
import com.primihub.biz.config.mq.SingleTaskChannel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.scheduling.annotation.EnableAsync;

@NacosPropertySources({
//        @NacosPropertySource(dataId = "test", autoRefreshed = true),
//        @NacosPropertySource(dataId = "test.yaml", autoRefreshed = true),
        @NacosPropertySource(dataId = "base.json" ,autoRefreshed = true),
        @NacosPropertySource(dataId = "database.yaml" ,autoRefreshed = true),
        @NacosPropertySource(dataId = "redis.yaml" ,autoRefreshed = true)})
@SpringBootApplication(scanBasePackages="com.primihub")
@EnableAsync
@ServletComponentScan(basePackages = {"com.primihub.biz.filter"})
@EnableBinding({SingleTaskChannel.class})
@EnableFeignClients(basePackages = {"com.primihub"})
public class PlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlatformApplication.class, args);
    }

}
