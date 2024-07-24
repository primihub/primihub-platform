package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.lpy.DataCore;
import com.primihub.biz.entity.data.po.lpy.DataMap;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface DataMapPrimarydbRepository {
    void saveDataMapList(@Param("list") Set<DataMap> newMapDataSet);
}
