package com.primihub.biz.repository.primarydb.sys;

import com.primihub.biz.entity.sys.po.SysFile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SysFilePrimarydbRepository {
    void insertSysFile(SysFile sysFile);
    void updateSysFileCurrentSize(@Param("fileId") Long fileId, @Param("fileCurrentSize") Long fileCurrentSize);
}
