package com.primihub.repository;

import com.primihub.entity.DataSet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface DataSetRepository {
    void insertDataSet(DataSet dataSet);
    void insertBatchDataSet(@Param("list") List<DataSet> list);
    void updateDataSet(DataSet dataSet);
    void deleteDataSet(DataSet dataSet);
    List<DataSet> getDataSetByIds(@Param("ids") Set<String> ids);
    List<DataSet> getDataSetOwnAll();
    List<DataSet> getTestDataSet();
    DataSet getDataSetById(String id);

}
