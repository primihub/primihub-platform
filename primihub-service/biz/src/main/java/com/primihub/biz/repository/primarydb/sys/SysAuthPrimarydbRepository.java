package com.primihub.biz.repository.primarydb.sys;

import com.primihub.biz.entity.sys.param.AlterAuthNodeStatusParam;
import com.primihub.biz.entity.sys.po.SysAuth;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SysAuthPrimarydbRepository {
    void insertSysAuth(SysAuth sysAuth);
    void updateRAuthIdAndFullPath(@Param("authId") Long authId,@Param("rAuthId")Long rAuthId,@Param("fullPath")String fullPath);
    void deleteSysAuth(@Param("authId") Long authId);
    void updateSysAuthExplicit(AlterAuthNodeStatusParam alterAuthNodeStatusParam);
}
