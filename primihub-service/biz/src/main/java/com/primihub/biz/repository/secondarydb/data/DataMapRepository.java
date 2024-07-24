package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.lpy.DataMap;
import com.primihub.biz.entity.data.vo.lpy.ExamResultVo;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

public interface DataMapRepository {
    Set<DataMap> selectDataMap(@Param("list") Set<String> rawSet);

    Set<ExamResultVo> selectIdNumExamResultVo(@Param("list") Set<String> rawSet);
}
