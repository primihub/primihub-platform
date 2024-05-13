package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.DataCore;
import com.primihub.biz.entity.data.vo.DataCoreVo;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface DataCoreRepository {
    // todo
    Set<DataCore> selectExistentDataCore(Set<String> fieldValueSet);

    Set<DataCore> selectDataCoreFromIdNum(Set<String> idNumSet);
    Set<DataCoreVo> selectDataCoreWithScore(String scoreType);
}
