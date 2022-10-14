package com.primihub.biz.repository.primaryredis.sys;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

@Repository
public class SysCommonPrimaryRedisRepository {

    @Resource(name="primaryStringRedisTemplate")
    private StringRedisTemplate primaryStringRedisTemplate;

    public Long atomIncrement(String key,Integer timeout,TimeUnit timeUnit){
        Long result=primaryStringRedisTemplate.opsForValue().increment(key);
        primaryStringRedisTemplate.expire(key,timeout, timeUnit);
        return result;
    }

    public Long getCurrentSecondIncr(String key){
        return atomIncrement(key,61,TimeUnit.SECONDS);
    }

    public void setRsaKey(String key,String privateKey){
        primaryStringRedisTemplate.opsForValue().set(key,privateKey);
        primaryStringRedisTemplate.expire(key,61, TimeUnit.SECONDS);
    }

    public void setAuthUserKey(String key,String authUserJson){
        primaryStringRedisTemplate.opsForValue().set(key,authUserJson);
        primaryStringRedisTemplate.expire(key,301, TimeUnit.SECONDS);
    }

    public String getRsaKey(String key){
        return primaryStringRedisTemplate.opsForValue().get(key);
    }

    public Boolean lock(String key){
        return primaryStringRedisTemplate.opsForValue().setIfAbsent(key,"",61,TimeUnit.SECONDS);
    }

    public void unlock(String key){
        primaryStringRedisTemplate.delete(key);
    }

    public void setKeyWithExpire(String key,String value,Long timeout,TimeUnit timeUnit){
        primaryStringRedisTemplate.opsForValue().set(key,value,timeout,timeUnit);
    }

    public String getKey(String key){
        return primaryStringRedisTemplate.opsForValue().get(key);
    }

}
