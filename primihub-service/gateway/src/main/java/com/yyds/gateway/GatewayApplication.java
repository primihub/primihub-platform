package com.yyds.gateway;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySources;
import com.yyds.biz.config.grpc.GrpcServerConfiguration;
import com.yyds.biz.config.mq.SingleTaskChannelConsumer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@NacosPropertySources({
        @NacosPropertySource(dataId = "test", autoRefreshed = true),
        @NacosPropertySource(dataId = "test.yaml", autoRefreshed = true),
        @NacosPropertySource(dataId = "base.json" ,autoRefreshed = true),
        @NacosPropertySource(dataId = "database.yaml" ,autoRefreshed = true),
        @NacosPropertySource(dataId = "redis.yaml" ,autoRefreshed = true)})
@SpringBootApplication(scanBasePackages="com.yyds")
@ComponentScan(
    basePackages = {"com.yyds"},
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {GrpcServerConfiguration.class,SingleTaskChannelConsumer.class}),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.yyds.biz.service.data.*")
    }
)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
