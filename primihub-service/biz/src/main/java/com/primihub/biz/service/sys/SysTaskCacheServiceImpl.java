package com.primihub.biz.service.sys;

import com.primihub.sdk.task.cache.CacheService;
import com.primihub.sdk.task.param.TaskParam;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class SysTaskCacheServiceImpl implements CacheService {

    @Resource(name = "primaryStringRedisTemplate")
    private StringRedisTemplate primaryStringRedisTemplate;

    @Override
    public List<String> get(String key) {
        return primaryStringRedisTemplate.opsForList().range(key, 0L, -1L);
    }

    @Override
    public Map<String, String> getTaskData(String key) {
        Map<Object, Object> entries = primaryStringRedisTemplate.opsForHash().entries(key);
        if (entries==null || entries.isEmpty()){
            return null;
        }
        Map<String, String> map = new HashMap<>();
        for (Object mapKey : entries.keySet()) {
            map.put(mapKey.toString(),entries.get(mapKey).toString());
        }
        return map;
    }

    @Override
    public void put(String key, List<String> list) {
        primaryStringRedisTemplate.delete(key);
        primaryStringRedisTemplate.opsForList().rightPushAll(key, list);
        primaryStringRedisTemplate.expire(key, 12, TimeUnit.HOURS);
    }

    @Override
    public void invalidate(String key) {
        primaryStringRedisTemplate.delete(key);
    }

    @Override
    public void taskData(TaskParam taskParam) {
        primaryStringRedisTemplate.opsForHash().putAll(taskParam.getTaskId(), new HashMap() {{
            put("requestId", taskParam.getRequestId());
            put("jobId", taskParam.getJobId());
            put("taskId", taskParam.getTaskId());
        }});
        primaryStringRedisTemplate.expire(taskParam.getTaskId(), 12, TimeUnit.HOURS);
    }

    @Override
    public String getType() {
        return "SysTaskCacheServiceImpl";
    }
}
