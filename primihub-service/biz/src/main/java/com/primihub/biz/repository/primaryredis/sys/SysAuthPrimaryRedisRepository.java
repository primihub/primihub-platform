package com.primihub.biz.repository.primaryredis.sys;

import com.alibaba.fastjson.JSON;
import com.primihub.biz.constant.RedisKeyConstant;
import com.primihub.biz.entity.sys.vo.SysAuthNodeVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
public class SysAuthPrimaryRedisRepository {

    @Resource(name="primaryStringRedisTemplate")
    private StringRedisTemplate primaryStringRedisTemplate;

    public List<SysAuthNodeVO> getSysAuthForBFS(){
        String result=primaryStringRedisTemplate.opsForValue().get(RedisKeyConstant.SYS_AUTH_BFS_LIST_STR_KEY);
        return JSON.parseArray(result,SysAuthNodeVO.class);
    }

    public void setSysAuthForBFS(List<SysAuthNodeVO> list){
        primaryStringRedisTemplate.opsForValue().set(RedisKeyConstant.SYS_AUTH_BFS_LIST_STR_KEY,JSON.toJSON(list).toString());
        primaryStringRedisTemplate.expire(RedisKeyConstant.SYS_AUTH_BFS_LIST_STR_KEY,3, TimeUnit.DAYS);
    }

    public void deleteSysAuthForBfs(){
        primaryStringRedisTemplate.delete(RedisKeyConstant.SYS_AUTH_BFS_LIST_STR_KEY);
    }

}
