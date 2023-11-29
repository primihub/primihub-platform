package com.primihub.biz.repository.secondarydb.data;


import com.primihub.biz.entity.data.po.DataPirTask;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.req.DataPirTaskReq;
import com.primihub.biz.entity.data.req.DataTaskReq;
import com.primihub.biz.entity.data.vo.DataPirTaskVo;
import com.primihub.biz.entity.data.vo.DataTaskVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DataTaskRepository {
    DataTask selectDataTaskByTaskId(@Param("taskId") Long taskId);

    DataTask selectDataTaskByTaskIdName(@Param("taskIdName") String taskIdName);

    List<DataTask> selectDataTaskByTaskIds(@Param("taskIds") Set<Long> taskIds);

    List<DataPirTaskVo> selectDataPirTaskPage(DataPirTaskReq req);

    Integer selectDataPirTaskCount(DataPirTaskReq req);

    List<DataTaskVo> selectDataTaskList(DataTaskReq req);

    Integer selectDataTaskListCount(DataTaskReq req);


    DataPirTask selectPirTaskById(Long id);
}
