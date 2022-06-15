package com.yyds.biz.repository.primaryredis.sys;

import com.yyds.biz.constant.RedisKeyConstant;
import com.yyds.biz.entity.sys.vo.SysUserListVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class SysUserPrimaryRedisRepository {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public void updateUserLoginStatus(String token, SysUserListVO sysUserListVO){
        String tokenKey=RedisKeyConstant.SYS_USER_LOGIN_TOKEN_STR_KEY.replace("<token>",token);
        stringRedisTemplate.opsForValue().set(tokenKey,sysUserListVO.getUserId().toString());
        String userKey=RedisKeyConstant.SYS_USER_LOGIN_STATUS_HASH_KEY.replace("<user_id>",sysUserListVO.getUserId().toString());
        Map map=new HashMap<>();
        map.put("userId",sysUserListVO.getUserId().toString());
        map.put("userName",sysUserListVO.getUserName());
        map.put("userAccount",sysUserListVO.getUserAccount());
        map.put("roleIdList",sysUserListVO.getRoleIdList());
        map.put("roleIdListDesc",sysUserListVO.getRoleIdListDesc());
        map.put("organIdList",sysUserListVO.getOrganIdList());
        map.put("organIdListDesc",sysUserListVO.getOrganIdListDesc());
        map.put("rOrganIdList",sysUserListVO.getROrganIdList());
        map.put("rOrganIdListDesc",sysUserListVO.getROrganIdListDesc());
        map.put("isForbid",sysUserListVO.getIsForbid().toString());
        map.put("authIdList",sysUserListVO.getAuthIdList());
        map.put("token",token);
        stringRedisTemplate.opsForHash().putAll(userKey,map);
        stringRedisTemplate.expire(tokenKey,1, TimeUnit.HOURS);
        stringRedisTemplate.expire(userKey,1, TimeUnit.HOURS);
    }

    public SysUserListVO findUserLoginStatus(String token){
        String tokenKey=RedisKeyConstant.SYS_USER_LOGIN_TOKEN_STR_KEY.replace("<token>",token);
        String userIdStr=stringRedisTemplate.opsForValue().get(tokenKey);
        if(userIdStr==null||userIdStr.equals("")) return null;
        String userKey=RedisKeyConstant.SYS_USER_LOGIN_STATUS_HASH_KEY.replace("<user_id>",userIdStr);
        Map map=stringRedisTemplate.opsForHash().entries(userKey);
        SysUserListVO sysUserListVO=new SysUserListVO();
        sysUserListVO.setUserId(Long.parseLong(map.get("userId").toString()));
        sysUserListVO.setUserName(map.get("userName").toString());
        sysUserListVO.setUserAccount(map.get("userAccount").toString());
        sysUserListVO.setRoleIdList(map.get("roleIdList").toString());
        sysUserListVO.setRoleIdListDesc(map.get("roleIdListDesc").toString());
        sysUserListVO.setROrganIdList(map.get("rOrganIdList").toString());
        sysUserListVO.setROrganIdListDesc(map.get("rOrganIdListDesc").toString());
        sysUserListVO.setOrganIdList(map.get("organIdList").toString());
        sysUserListVO.setOrganIdListDesc(map.get("organIdListDesc").toString());
        sysUserListVO.setIsForbid(Integer.parseInt(map.get("isForbid").toString()));
        sysUserListVO.setAuthIdList(map.get("authIdList").toString());
        return sysUserListVO;
    }

    public void deleteUserLoginStatus(String token,Long userId){
        String tokenKey=RedisKeyConstant.SYS_USER_LOGIN_TOKEN_STR_KEY.replace("<token>",token);
        String userKey=RedisKeyConstant.SYS_USER_LOGIN_STATUS_HASH_KEY.replace("<user_id>",userId.toString());
        stringRedisTemplate.delete(tokenKey);
        stringRedisTemplate.delete(userKey);
    }

    public void deleteUserLoginStatus(Long userId){
        String userKey=RedisKeyConstant.SYS_USER_LOGIN_STATUS_HASH_KEY.replace("<user_id>",userId.toString());
        Object token=stringRedisTemplate.opsForHash().get(userKey,"token");
        if (token==null)
            return;
        String tokenKey=RedisKeyConstant.SYS_USER_LOGIN_TOKEN_STR_KEY.replace("<token>",token.toString());
        stringRedisTemplate.delete(tokenKey);
        stringRedisTemplate.delete(userKey);
    }

    public void expireUserLoginStatus(String token,Long userId){
        String tokenKey=RedisKeyConstant.SYS_USER_LOGIN_TOKEN_STR_KEY.replace("<token>",token);
        String userKey=RedisKeyConstant.SYS_USER_LOGIN_STATUS_HASH_KEY.replace("<user_id>",userId.toString());
        stringRedisTemplate.expire(tokenKey,1, TimeUnit.HOURS);
        stringRedisTemplate.expire(userKey,1, TimeUnit.HOURS);
    }

}
