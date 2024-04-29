package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.DataCore;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DataCorePrimarydbRepository {
    // todo
    void saveDataCoreSet(Set<DataCore> nonexistentDataCoreSet);
}
