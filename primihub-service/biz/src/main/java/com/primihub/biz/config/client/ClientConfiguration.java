package com.primihub.biz.config.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ClientConfiguration {

    @Bean(name="primaryRestTemplate")
    @Primary
    @LoadBalanced
    public RestTemplate initPrimaryRestTemplate(){
        return new RestTemplate();
    }


    @Bean("restHttpRequestFactory")
    @ConfigurationProperties(prefix = "rest.template.connection")
    public HttpComponentsClientHttpRequestFactory restHttpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory();
    }

    @Bean(name="soaRestTemplate")
    public RestTemplate initSoaRestTemplate(@Qualifier("restHttpRequestFactory") HttpComponentsClientHttpRequestFactory factory){
        return new RestTemplate(factory);
    }

}
