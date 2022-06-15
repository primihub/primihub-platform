package com.primihub.biz.repository.secondarydb.sys;

import com.primihub.biz.entity.sys.po.SysRole;
import com.primihub.biz.entity.sys.vo.SysRoleListVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface SysRoleSecondarydbRepository {
    SysRole selectSysRoleByRoleId(@Param("roleId") Long roleId);
    Set<Long> selectRaByRoleId(@Param("roleId") Long roleId);
    List<SysRoleListVO> selectRoleListByParam(Map paramMap);
    Long selectRoleListCountByParam(Map paramMap);
    List<SysRole> selectSysRoleByBatchRoleId(@Param("roleIdSet") Set<Long> roleIdSet);
    Set<Long> selectRaByBatchRoleId(@Param("roleIdSet") Set<Long> roleIdSet);
    Long selectUserCountByRole(@Param("roleId") Long roleId);
}
