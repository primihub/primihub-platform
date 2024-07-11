package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.lpy.DataCore;
import com.primihub.biz.entity.data.po.lpy.DataImei;
import com.primihub.biz.entity.data.vo.DataCoreVo;
import com.primihub.biz.entity.data.vo.lpy.ExamResultVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DataImeiRepository {
    Set<DataCore> selectExistentDataCore(@Param("list") Set<String> fieldValueSet);

    Set<DataCore> selectDataCoreFromIdNum(@Param("list") Set<String> idNumSet, @Param("scoreType") String scoreType);

    Set<DataCoreVo> selectDataCoreWithScore(@Param("scoreType") String scoreType);

    Set<ExamResultVo> selectExamResultVo(@Param("list") Set<String> fieldValueSet);

    Set<DataImei> selectImei(@Param("set") Set<String> set);
}
