package com.primihub.biz.repository.secondarydb.sys;

import com.primihub.biz.entity.sys.po.SysFile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface SysFileSecondarydbRepository {
    SysFile selectSysFileByFileId(Long fileId);
    List<SysFile> selectSysFileByFileIds(@Param("fileIds") Set<Long> fileIds);
}
