package com.primihub.biz.repository.primaryredis.data;

import com.primihub.biz.constant.RedisKeyConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class DataRedisRepository {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void pirTaskResultHandle(String taskId, List<String[]> datas){
        String setKey = RedisKeyConstant.PIR_TASK_DATA_SET.replace("<taskId>", taskId);
        String listKey = RedisKeyConstant.PIR_TASK_DATA_LIST.replace("<taskId>", taskId);
        for (String[] data : datas) {
            String dataStr = String.join(",",data);
            String dataHashCode = String.valueOf(dataStr.hashCode());
            if(stringRedisTemplate.opsForSet().isMember(setKey, dataHashCode) == Boolean.FALSE){
                stringRedisTemplate.opsForSet().add(setKey,dataHashCode);
                stringRedisTemplate.opsForList().rightPush(listKey,dataStr);
            }
        }

    }

    public List<String> getPirTaskResultData(String taskId){
        return stringRedisTemplate.opsForList().range(RedisKeyConstant.PIR_TASK_DATA_LIST.replace("<taskId>", taskId),0L,Long.MAX_VALUE);
    }

    public void deletePirTaskResultK(String taskId){
        String setKey = RedisKeyConstant.PIR_TASK_DATA_SET.replace("<taskId>", taskId);
        String listKey = RedisKeyConstant.PIR_TASK_DATA_LIST.replace("<taskId>", taskId);
        stringRedisTemplate.delete(setKey);
        stringRedisTemplate.delete(listKey);
    }
}
