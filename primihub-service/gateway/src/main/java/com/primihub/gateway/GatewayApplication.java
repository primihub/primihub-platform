package com.primihub.gateway;

import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySource;
import com.alibaba.nacos.spring.context.annotation.config.NacosPropertySources;
import com.primihub.biz.config.base.SwaggerConfig;
import com.primihub.biz.config.base.TemplatesConfig;
import com.primihub.biz.config.grpc.GrpcConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.config.mq.SingleTaskChannelConsumer;
import com.primihub.biz.config.thread.ThreadPoolConfig;
import com.primihub.biz.service.sys.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.hazelcast.HazelcastAutoConfiguration;
import org.springframework.cloud.stream.binding.BindingService;
import org.springframework.cloud.stream.config.BindingServiceConfiguration;
import org.springframework.cloud.stream.function.FunctionConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

@NacosPropertySources({
        @NacosPropertySource(dataId = "base.json" ,autoRefreshed = true),
        @NacosPropertySource(dataId = "database.yaml" ,autoRefreshed = true),
        @NacosPropertySource(dataId = "redis.yaml" ,autoRefreshed = true)})
@SpringBootApplication(scanBasePackages="com.primihub",exclude = {HazelcastAutoConfiguration.class, BindingServiceConfiguration.class, FunctionConfiguration.class})
@ComponentScan(
    basePackages = {"com.primihub"},
    excludeFilters = {
        @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                value = {
                        SingleTaskChannelConsumer.class,
                        ThreadPoolConfig.class,
                        TemplatesConfig.class,
                        SysCaptchaCacheServiceImpl.class,
                        SysUserService.class,
                        SysOauthService.class,
                        SysSseEmitterService.class,
                        SysWebSocketService.class,
                        SysEmailService.class,
                        SysOrganService.class,
                        SysAsyncService.class,
                        GrpcConfiguration.class,
                        SwaggerConfig.class
                }),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = {"com.primihub.biz.service.data.*","com.primihub.biz.service.schedule.*","com.primihub.biz.service.share.*","com.primihub.biz.service.test.*","com.primihub.biz.config.captcha.*"})
    }
)
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }

}
