package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.DataMpcTask;
import com.primihub.biz.entity.data.po.DataScript;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DataMpcRepository {

    List<DataScript> selectDataScriptByUser(@Param("userId") Long userId,@Param("scriptName") String scriptName);

    DataScript selectDataScriptById(@Param("scriptId") Long scriptId);

    List<DataScript> selectDataScriptByPId(@Param("scriptId") Long scriptId);

    DataMpcTask selectDataMpcTaskById(@Param("taskId")Long taskId);

    List<DataMpcTask> selectDataMpcTaskPage(Map<String,Object> map);

    Integer selectDataMpcTaskPageCount(Map<String,Object> map);
}
