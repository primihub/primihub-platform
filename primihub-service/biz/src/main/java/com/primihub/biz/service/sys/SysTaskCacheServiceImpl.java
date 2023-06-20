package com.primihub.biz.service.sys;

import com.primihub.sdk.task.cache.CacheService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SysTaskCacheServiceImpl implements CacheService {

    @Resource(name="primaryStringRedisTemplate")
    private StringRedisTemplate primaryStringRedisTemplate;

    @Override
    public List<String> get(String key) {
        return primaryStringRedisTemplate.opsForList().range(key,0L,-1L);
    }

    @Override
    public void put(String key, List<String> list) {
        primaryStringRedisTemplate.delete(key);
        primaryStringRedisTemplate.opsForList().rightPushAll(key,list);
        primaryStringRedisTemplate.expire(key,12, TimeUnit.HOURS);
    }

    @Override
    public void invalidate(String key) {
        primaryStringRedisTemplate.delete(key);
    }

    @Override
    public String getType() {
        return "SysTaskCacheServiceImpl";
    }
}
