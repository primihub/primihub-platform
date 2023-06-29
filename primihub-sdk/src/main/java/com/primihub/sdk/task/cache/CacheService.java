package com.primihub.sdk.task.cache;

import com.primihub.sdk.task.param.TaskParam;

import java.util.List;
import java.util.Map;

public interface CacheService {
    List<String> get(String key);
    Map<String,String> getTaskData(String key);
    void put(String key,List<String> list);
    void invalidate(String key);
    void taskData(TaskParam taskParam);
    String getType();
}
