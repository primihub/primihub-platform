package com.primihub.biz.repository.primaryredis.sys;

import com.primihub.biz.constant.RedisKeyConstant;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.sys.vo.SysUserListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
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
        if(userIdStr==null|| "".equals(userIdStr)) {
            return null;
        }
        String userKey=RedisKeyConstant.SYS_USER_LOGIN_STATUS_HASH_KEY.replace("<user_id>",userIdStr);
        Map map=stringRedisTemplate.opsForHash().entries(userKey);
        SysUserListVO sysUserListVO=new SysUserListVO();
        sysUserListVO.setUserId(Long.parseLong(map.get("userId").toString()));
        sysUserListVO.setUserName(map.get("userName").toString());
        sysUserListVO.setUserAccount(map.get("userAccount").toString());
        sysUserListVO.setRoleIdList(map.get("roleIdList").toString());
        sysUserListVO.setRoleIdListDesc(map.get("roleIdListDesc").toString());
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
        if (token==null) {
            return;
        }
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

    public Long loginErrorRecordNumber(Long userId){
        String userKey=RedisKeyConstant.SYS_USER_LOGIN_PASS_ERRER_KEY.replace("<user_id>",userId.toString());
        Long increment = stringRedisTemplate.opsForValue().increment(userKey);
        if (increment == null || increment == 1L){
            stringRedisTemplate.expire(userKey, 1, TimeUnit.HOURS);
        }
        if (increment!=null && increment >= SysConstant.SYS_USER_PASS_ERRER_NUM){
            log.info("The password exceeds the number of errors user_id:{} num:{}",userId,SysConstant.SYS_USER_PASS_ERRER_NUM);
            stringRedisTemplate.expire(userKey,SysConstant.SYS_USER_LOGIN_LIMIT_NUM, TimeUnit.HOURS);
        }
        return increment;
    }

    public void deleteLoginErrorRecordNumber(Long userId){
        String userKey=RedisKeyConstant.SYS_USER_LOGIN_PASS_ERRER_KEY.replace("<user_id>",userId.toString());
        stringRedisTemplate.delete(userKey);
    }

    public Long loginVerificationNumber(Long userId){
        String userKey=RedisKeyConstant.SYS_USER_LOGIN_PASS_ERRER_KEY.replace("<user_id>",userId.toString());
        String userVal = stringRedisTemplate.opsForValue().get(userKey);
        return StringUtils.isBlank(userVal)?0L:Long.parseLong(userVal);
    }

}
