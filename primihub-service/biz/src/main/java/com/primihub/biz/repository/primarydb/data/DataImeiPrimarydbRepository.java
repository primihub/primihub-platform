package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.lpy.DataCore;
import com.primihub.biz.entity.data.po.lpy.DataImei;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataImeiPrimarydbRepository {

    void saveImeiSet(@Param("list")List<DataImei> dataImeiList);

    void saveImei(DataImei dataImei);
}
