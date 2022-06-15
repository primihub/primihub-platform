package com.yyds.biz.repository.secondarydb.sys;

import com.yyds.biz.entity.sys.po.SysOrgan;
import com.yyds.biz.entity.sys.vo.SysOrganListVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface SysOrganSecondarydbRepository {
    List<SysOrganListVO> selectOrganListByParam(Map paramMap);
    Long selectOrganListCountByParam(Map paramMap);
    SysOrgan selectSysOrganByOrganId(@Param("organId")Long organId);
    List<SysOrgan> selectSysOrganByBatchOrganId(@Param("organIdSet") Set<Long> organIdSet);
}
