package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.lpy.DataImei;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DataImeiRepository {
    Set<DataImei> selectImei(@Param("list") Set<String> set);

    Set<DataImei> selectImeiWithScore(@Param("list") Set<String> liDongSet, @Param("scoreType") String scoreType);
}
