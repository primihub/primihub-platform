package com.primihub.biz.config.base;


import com.primihub.biz.entity.base.BaseResultEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import springfox.documentation.builders.*;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;
import springfox.documentation.service.ResponseMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Value("${swagger2.enable:false}")
    private boolean swaggerEnable;

    @Bean
    public Docket controllerApi() {
        List<ResponseMessage> responseMessageList = new ArrayList<>();
        for (BaseResultEnum value : BaseResultEnum.values()) {
            responseMessageList.add(new ResponseMessageBuilder().code(value.getReturnCode()).message(value.getMessage()).build());
        }
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(new ApiInfoBuilder()
                        .title("PrimiHub Platform 平台接口文档")
                        .description("基于多方安全计算、联邦学习、可信计算等技术自主研发的分布式隐私计算平台。涵盖隐私求交、联合建模、联合统计、匿踪查询、数据资源管理、算法容器管理、异构平台互联互通等功能，满足各场景下的数据价值流通需求。")
                        .contact(new Contact("PrimiHub", "https://primihub.com/", "business@primihub.com"))
                        .version("1.6.12")
                        .build())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.primihub"))
                .paths(PathSelectors.any())
                .build()
                .enable(swaggerEnable)
                .globalResponseMessage(RequestMethod.GET,responseMessageList)
                .globalResponseMessage(RequestMethod.POST,responseMessageList)
                .globalOperationParameters(getParameter());
    }


    private List<Parameter> getParameter() {
        List<Parameter> params = new ArrayList<>();
        params.add(new ParameterBuilder().name("token")
                .description("请求令牌")
                .modelRef(new ModelRef("String"))
                .parameterType("header")
                .defaultValue("excalibur_forever_ABCDEFGHIJKLMN")
                .required(false).build());
        params.add(new ParameterBuilder().name("timestamp")
                .description("时间戳")
                .modelRef(new ModelRef("String"))
                .parameterType("header")
                .defaultValue(System.currentTimeMillis()+"")
                .required(false).build());
        params.add(new ParameterBuilder().name("nonce")
                .description("随机数")
                .modelRef(new ModelRef("String"))
                .parameterType("header")
                .defaultValue((new Random().nextInt(900) + 100)+"")
                .required(false).build());
        return params;
    }
}
