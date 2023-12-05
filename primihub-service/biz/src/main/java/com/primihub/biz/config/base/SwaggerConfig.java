package com.primihub.biz.config.base;


import com.github.xiaoymin.swaggerbootstrapui.annotations.EnableSwaggerBootstrapUI;
import com.primihub.biz.entity.base.BaseResultEnum;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.builders.ResponseMessageBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.ResponseMessage;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableSwagger2
@EnableSwaggerBootstrapUI
public class SwaggerConfig {

    private static ApiInfo apiInfo;

    private static List<ResponseMessage> responseMessageList =  new ArrayList<>();

    static {
        apiInfo = new ApiInfoBuilder()
                .title("PrimiHub Platform 平台接口文档")
                .description("基于多方安全计算、联邦学习、可信计算等技术自主研发的分布式隐私计算平台。涵盖隐私求交、联合建模、联合统计、匿踪查询、数据资源管理、算法容器管理、异构平台互联互通等功能，满足各场景下的数据价值流通需求。")
                .contact(new Contact("PrimiHub", "https://primihub.com/", "business@primihub.com"))
                .version("1.6.12")
                .build();
        for (BaseResultEnum value : BaseResultEnum.values()) {
            responseMessageList.add(new ResponseMessageBuilder().code(value.getReturnCode()).message(value.getMessage()).build());
        }
    }

    @Bean
    public Docket allApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.primihub.application.controller"))
                .paths(PathSelectors.any())
                .build()
                .globalResponseMessage(RequestMethod.GET,responseMessageList)
                .globalResponseMessage(RequestMethod.POST,responseMessageList);
    }

    @Bean
    public Docket sysApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.primihub.application.controller.sys"))
                .paths(PathSelectors.any())
                .build()
                .groupName("sys 系统接口")
                .globalResponseMessage(RequestMethod.GET,responseMessageList)
                .globalResponseMessage(RequestMethod.POST,responseMessageList);
    }
    @Bean
    public Docket dataApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.primihub.application.controller.data"))
                .paths(PathSelectors.any())
                .build()
                .groupName("data 业务接口")
                .globalResponseMessage(RequestMethod.GET,responseMessageList)
                .globalResponseMessage(RequestMethod.POST,responseMessageList);
    }

    @Bean
    public Docket testApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.primihub.application.controller.test"))
                .paths(PathSelectors.any())
                .build()
                .groupName("test 测试接口")
                .globalResponseMessage(RequestMethod.GET,responseMessageList)
                .globalResponseMessage(RequestMethod.POST,responseMessageList);
    }

    @Bean
    public Docket shareApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.primihub.application.controller.share"))
                .paths(PathSelectors.any())
                .build()
                .groupName("share 协同接口")
                .globalResponseMessage(RequestMethod.GET,responseMessageList)
                .globalResponseMessage(RequestMethod.POST,responseMessageList);
    }

    @Bean
    public Docket scheduleApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.primihub.application.controller.schedule"))
                .paths(PathSelectors.any())
                .build()
                .groupName("schedule 调度接口")
                .globalResponseMessage(RequestMethod.GET,responseMessageList)
                .globalResponseMessage(RequestMethod.POST,responseMessageList);
    }

}
