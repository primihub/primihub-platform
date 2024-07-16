package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.lpy.DataCore;
import com.primihub.biz.entity.data.po.lpy.DataImei;
import com.primihub.biz.entity.data.po.lpy.DataMobile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataMobilePrimarydbRepository {
    void saveDataCoreSet(@Param("list") List<DataCore> nonexistentDataCoreList);

    void saveDataCore(DataCore dataCore);

    void saveImeiSet(@Param("list") List<DataImei> dataImeiList);

    void saveMobileList(@Param("list") List<DataMobile> newExistDataMobileList);
}
