package com.primihub.sdk.task.cache;

import java.util.List;

public interface CacheService {
    List<String> get(String key);
    void put(String key,List<String> list);
    void invalidate(String key);
    String getType();
}
