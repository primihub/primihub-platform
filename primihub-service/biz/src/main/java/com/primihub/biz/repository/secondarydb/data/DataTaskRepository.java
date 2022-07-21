package com.primihub.biz.repository.secondarydb.data;


import com.primihub.biz.entity.data.po.DataTask;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DataTaskRepository {
    DataTask selectDataTaskByTaskId(@Param("taskId") Long taskId);

    List<DataTask> selectDataTaskByTaskIds(@Param("taskIds") Set<Long> taskIds);
}
