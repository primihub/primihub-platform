package com.primihub.biz.config.client;

import org.apache.http.HttpHost;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Scope;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class HttpClientConfiguration {

    @Bean(name = "primaryRestTemplate")
    @Primary
    @LoadBalanced
    public RestTemplate initPrimaryRestTemplate() {
        return new RestTemplate();
    }


    @Bean("restHttpRequestFactory")
    @Scope("prototype")
    @ConfigurationProperties(prefix = "rest.template.connection")
    public HttpComponentsClientHttpRequestFactory restHttpRequestFactory() {
        return new HttpComponentsClientHttpRequestFactory();
    }

    @Bean(name = "soaRestTemplate")
    public RestTemplate initSoaRestTemplate(@Qualifier("restHttpRequestFactory") HttpComponentsClientHttpRequestFactory factory) {
        return new RestTemplate(factory);
    }

    @Bean(name = "proxyRestTemplate")
    public RestTemplate initproxyRestTemplate(@Qualifier("restHttpRequestFactory") HttpComponentsClientHttpRequestFactory factory) {
        String proxyHost = "118.190.39.100";
        int proxyPort = 30900;
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        HttpClient httpClient = HttpClientBuilder.create()
                .setProxy(proxy)
                .build();
        factory.setHttpClient(httpClient);
        return new RestTemplate(factory);
    }

}
