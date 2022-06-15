package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.DataFusionCopyTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DataCopyPrimarydbRepository {
    void updateCopyInfo(@Param("id")Long id, @Param("currentOffset")Long currentOffset, @Param("latestErrorMsg")String latestErrorMsg);

    void saveCopyInfo(DataFusionCopyTask copyTask);
}
