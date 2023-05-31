package com.primihub.biz.repository.secondarydb.sys;

import com.primihub.biz.entity.sys.param.OrganParam;
import com.primihub.biz.entity.sys.po.SysOrgan;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SysOrganSecondarydbRepository {
    List<SysOrgan> selectSysOrgan();
    List<SysOrgan> selectSysOrganByExamine();
    List<SysOrgan> selectSysOrganByOrganIds(Set<String> organIds);
    SysOrgan selectSysOrganByApplyId(String applyId);
    SysOrgan selectSysOrganByApplyIdOrOrganId(@Param("applyId") String applyId,@Param("organId") String organId);
    SysOrgan selectSysOrganByOrganId(String organId);
    List<SysOrgan> selectOrganByOrganId(String organId);

    SysOrgan selectSysOrganById(Long id);

    List<SysOrgan> selectSysOrganByParam(OrganParam param);
    Integer selectSysOrganByParamCount(OrganParam param);
}
