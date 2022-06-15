package com.primihub.biz.repository.primaryredis.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class TestRedisRepository {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void testIncr(){
        stringRedisTemplate.opsForValue().increment("test");
    }
}
