package com.primihub.biz.repository.secondarydb.sys;

import com.primihub.biz.entity.sys.po.SysUser;
import com.primihub.biz.entity.sys.vo.SysUserListVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface SysUserSecondarydbRepository {
    List<SysUserListVO> selectSysUserListByParam(Map paramMap);
    SysUser selectSysUserByUserId(@Param("userId")Long userId);
    SysUser selectSysUserContainPassByUserId(@Param("userId")Long userId);
    List<SysUser> selectSysUserByUserIdSet(@Param("userIdSet") Set<Long> userIdSet);
    Long selectSysUserListCountByParam(Map paramMap);
    Boolean isExistUserAccount(@Param("userAccount") String userAccount);
    SysUser selectUserByUserAccount(@Param("userAccount")String userAccount);
    SysUser selectUserByAuthUuid(@Param("authUuid")String authUuid);
}
