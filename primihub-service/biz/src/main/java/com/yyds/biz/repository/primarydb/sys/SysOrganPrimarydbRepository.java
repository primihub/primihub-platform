package com.yyds.biz.repository.primarydb.sys;

import com.yyds.biz.entity.sys.param.AlterOrganNodeStatusParam;
import com.yyds.biz.entity.sys.po.SysOrgan;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface SysOrganPrimarydbRepository {
    void insertSysOrgan(SysOrgan sysOrgan);
    void updateROrganIdAndFullPath(@Param("organId") Long organId,@Param("rOrganId")Long rOrganId,@Param("fullPath")String fullPath);
    void updateSysOrganExplicit(Map paramMap);
    void updateOrganDelStatus(@Param("organId")Long organId);
}
