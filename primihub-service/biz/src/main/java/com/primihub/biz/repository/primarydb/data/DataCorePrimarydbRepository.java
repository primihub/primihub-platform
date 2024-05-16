package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.DataCore;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataCorePrimarydbRepository {
    void saveDataCoreSet(@Param("list") List<DataCore> nonexistentDataCoreList);

    void saveDataCore(DataCore dataCore);
}
