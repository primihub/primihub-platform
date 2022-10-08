package com.primihub.biz.config.captcha;


import com.anji.captcha.service.CaptchaCacheService;
import com.anji.captcha.service.impl.CaptchaServiceFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 存储策略自动配置.
 *
 */
@Configuration
public class AjCaptchaStorageAutoConfiguration {

    @Bean(name = "AjCaptchaCacheService")
    public CaptchaCacheService captchaCacheService(@Qualifier("ajCaptchaProperties") AjCaptchaProperties ajCaptchaProperties){
        //缓存类型redis/local/....
        return CaptchaServiceFactory.getCache(ajCaptchaProperties.getCacheType().name());
    }
}