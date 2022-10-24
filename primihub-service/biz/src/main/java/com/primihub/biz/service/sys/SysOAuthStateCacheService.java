package com.primihub.biz.service.sys;

import me.zhyd.oauth.cache.AuthCacheConfig;
import me.zhyd.oauth.cache.AuthStateCache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SysOAuthStateCacheService implements AuthStateCache {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void cache(String key, String value) {
        stringRedisTemplate.opsForValue().set(key, value, AuthCacheConfig.timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void cache(String key, String value, long timeout) {
        stringRedisTemplate.opsForValue().set(key, value, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    @Override
    public boolean containsKey(String key) {
        return stringRedisTemplate.hasKey(key);
    }
}
