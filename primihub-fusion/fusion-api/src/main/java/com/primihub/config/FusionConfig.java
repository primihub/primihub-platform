package com.primihub.config;

import com.primihub.interceptor.PinCodeInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FusionConfig {

    @Bean
    public WebMvcConfigurer WebMvcConfigurer(PinCodeInterceptor pinCodeInterceptor) {
        return new WebMvcConfigurer() {
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(pinCodeInterceptor).addPathPatterns("/**").excludePathPatterns("/fusion/**");
            }
        };
    }

}
