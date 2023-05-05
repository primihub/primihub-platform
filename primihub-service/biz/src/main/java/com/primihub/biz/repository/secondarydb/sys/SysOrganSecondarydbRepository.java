package com.primihub.biz.repository.secondarydb.sys;

import com.primihub.biz.entity.sys.param.OrganParam;
import com.primihub.biz.entity.sys.po.SysOrgan;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SysOrganSecondarydbRepository {
    List<SysOrgan> selectSysOrgan();
    List<SysOrgan> selectSysOrganByExamine();
    List<SysOrgan> selectSysOrganByOrganIds(Set<String> organIds);
    SysOrgan selectSysOrganByApplyId(String applyId);

    List<SysOrgan> selectSysOrganByParam(OrganParam param);
    Integer selectSysOrganByParamCount(OrganParam param);
}
