package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.DataCore;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DataCorePrimarydbRepository {
    void saveDataCoreSet(@Param("list") Set<DataCore> nonexistentDataCoreSet);

    void saveDataCore(DataCore dataCore);
}
