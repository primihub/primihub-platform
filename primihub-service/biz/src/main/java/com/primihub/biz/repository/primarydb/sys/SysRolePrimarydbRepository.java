package com.primihub.biz.repository.primarydb.sys;

import com.primihub.biz.entity.sys.po.SysRa;
import com.primihub.biz.entity.sys.po.SysRole;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SysRolePrimarydbRepository {
    void insertSysRole(SysRole sysRole);
    void updateSysRole(Map paramMap);
    void deleteSysRole(@Param("roleId")Long roleId);
    void deleteSysRa(@Param("roleId")Long roleId);
    void insertSysRaBatch(@Param("raList") List<SysRa> raList);
    void deleteSysRaBatch(@Param("authArray") Long[] authArray,@Param("roleId")Long roleId);
}
