package com.primihub.sdk.task.cache.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.primihub.sdk.task.cache.CacheService;
import com.primihub.sdk.task.param.TaskParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CaffeineCacheService implements CacheService {

    private static Cache<String, List<String>> cache;
    private static Cache<String, Map<String, String>> cacheTask;
    static {
        cache = Caffeine.newBuilder().initialCapacity(10).maximumSize(300).expireAfterWrite(3, TimeUnit.HOURS).build();
        cacheTask = Caffeine.newBuilder().initialCapacity(10).maximumSize(300).expireAfterWrite(3, TimeUnit.HOURS).build();
    }


    @Override
    public List<String> get(String key) {
        return cache.get(key, c -> new ArrayList<>());
    }

    @Override
    public Map<String, String> getTaskData(String key) {
        return cacheTask.get(key,c -> null);
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
    public void taskData(TaskParam taskParam) {
        cacheTask.put(taskParam.getTaskId(),new HashMap() {{
            put("requestId", taskParam.getRequestId());
            put("jobId", taskParam.getJobId());
            put("taskId", taskParam.getTaskId());
        }});
    }

    @Override
    public String getType() {
        return "CaffeineCacheService";
    }
}
