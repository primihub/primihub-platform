package com.yyds.biz.repository.primarydb.sys;

import com.yyds.biz.entity.sys.po.SysUo;
import com.yyds.biz.entity.sys.po.SysUr;
import com.yyds.biz.entity.sys.po.SysUser;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface SysUserPrimarydbRepository {
    void insertSysUser(SysUser sysUser);
    void updateSysUserExplicit(Map paramMap);
    void insertSysUrBatch(@Param("urList") List<SysUr> urList);
    void deleteSysUrBatch(/*@Param("roleArray") Long[] roleArray,*/ @Param("userId")Long userId);
    void insertSysUoBatch(@Param("uoList") List<SysUo> uoList);
    void deleteSysUoBatch(@Param("organArray") Long[] organArray, @Param("userId")Long userId);
    void updateSysUserDelStatus(@Param("userId")Long userId);
}
