package com.primihub.gateway;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySources;
import com.primihub.biz.config.grpc.GrpcServerConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannelConsumer;
import com.primihub.biz.config.thread.ThreadPoolConfig;
import com.primihub.biz.service.sys.SysCaptchaCacheService;
import com.primihub.biz.service.sys.SysFusionService;
import com.primihub.biz.service.sys.SysOauthService;
import com.primihub.biz.service.sys.SysUserService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@NacosPropertySources({
//        @NacosPropertySource(dataId = "test", autoRefreshed = true),
//        @NacosPropertySource(dataId = "test.yaml", autoRefreshed = true),
        @NacosPropertySource(dataId = "base.json" ,autoRefreshed = true),
        @NacosPropertySource(dataId = "database.yaml" ,autoRefreshed = true),
        @NacosPropertySource(dataId = "redis.yaml" ,autoRefreshed = true)})
@SpringBootApplication(scanBasePackages="com.primihub")
@ComponentScan(
    basePackages = {"com.primihub"},
    excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = {GrpcServerConfiguration.class,SingleTaskChannelConsumer.class, SysFusionService.class , ThreadPoolConfig.class, SysCaptchaCacheService.class, SysUserService.class, SysOauthService.class}),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = {"com.primihub.biz.service.data.*","com.primihub.biz.service.schedule.*","com.primihub.biz.service.test.*","com.primihub.biz.config.captcha.*"})
    }
)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
