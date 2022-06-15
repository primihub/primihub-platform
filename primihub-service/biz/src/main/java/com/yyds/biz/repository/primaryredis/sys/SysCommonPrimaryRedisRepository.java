package com.yyds.biz.repository.primaryredis.sys;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Repository
public class SysCommonPrimaryRedisRepository {

    @Resource(name="primaryStringRedisTemplate")
    private StringRedisTemplate primaryStringRedisTemplate;

    public Long getCurrentSecondIncr(String key){
        Long currentSecondIncr=primaryStringRedisTemplate.opsForValue().increment(key);
        primaryStringRedisTemplate.expire(key,61, TimeUnit.SECONDS);
        return currentSecondIncr;
    }

    public void setRsaKey(String key,String privateKey){
        primaryStringRedisTemplate.opsForValue().set(key,privateKey);
        primaryStringRedisTemplate.expire(key,61, TimeUnit.SECONDS);
    }

    public String getRsaKey(String key){
        return primaryStringRedisTemplate.opsForValue().get(key);
    }

}
