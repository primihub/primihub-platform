package com.primihub.sdk.task.cache.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.primihub.sdk.task.cache.CacheService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CaffeineCacheService implements CacheService {

    private static Cache<String, List<String>> cache;
    static {
        cache = Caffeine.newBuilder().initialCapacity(10).maximumSize(300).expireAfterWrite(3, TimeUnit.HOURS).build();
    }


    @Override
    public List<String> get(String key) {
        return cache.get(key, c -> new ArrayList<>());
    }

    @Override
    public void put(String key, List<String> list) {
        cache.put(key,list);
    }

    @Override
    public void invalidate(String key) {
        cache.invalidate(key);
    }

    @Override
    public String getType() {
        return "CaffeineCacheService";
    }
}
