package com.primihub.biz.repository.secondarydb.sys;

import com.primihub.biz.entity.sys.po.SysAuth;
import com.primihub.biz.entity.sys.vo.SysAuthNodeVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SysAuthSecondarydbRepository {
    SysAuth selectSysAuthByAuthId(@Param("authId") Long authId);
    List<SysAuthNodeVO> selectAllSysAuthForBFS();
}
