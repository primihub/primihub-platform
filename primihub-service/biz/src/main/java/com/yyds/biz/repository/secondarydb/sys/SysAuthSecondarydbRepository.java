package com.yyds.biz.repository.secondarydb.sys;

import com.yyds.biz.entity.sys.po.SysAuth;
import com.yyds.biz.entity.sys.vo.SysAuthNodeVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysAuthSecondarydbRepository {
    SysAuth selectSysAuthByAuthId(@Param("authId") Long authId);
    List<SysAuthNodeVO> selectAllSysAuthForBFS();
}
