package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.lpy.DataMobile;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataMobilePrimarydbRepository {
    void saveMobileList(@Param("list") List<DataMobile> newExistDataMobileList);
}
