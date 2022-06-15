package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.DataFusionCopyTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DataCopySecondarydbRepository {
    List<DataFusionCopyTask> selectNotFinishedTask(@Param("threeDayAgo") Date threeDayAgo, @Param("tenMinuteAgo") Date tenMinuteAgo);
}
