package com.primihub.biz.repository.primarydb.sys;

import com.primihub.biz.entity.sys.po.SysOrgan;
import org.springframework.stereotype.Repository;

@Repository
public interface SysOrganPrimarydbRepository {
    void insertSysOrgan(SysOrgan sysOrgan);
    void updateSysOrgan(SysOrgan sysOrgan);

}
