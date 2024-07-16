package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.lpy.DataMobile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DataMobileRepository {
    Set<DataMobile> selectMobileWithScore(@Param("list") Set<String> targetValueSet, @Param("scoreType") String scoreType);
}
