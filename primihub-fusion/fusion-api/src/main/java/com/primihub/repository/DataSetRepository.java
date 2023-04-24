package com.primihub.repository;

import com.primihub.entity.DataSet;
import com.primihub.entity.fusion.po.FusionOrgan;
import com.primihub.entity.fusion.po.FusionOrganExtends;
import com.primihub.entity.fusion.vo.OrganExtendsVo;
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
    List<DataSet> getDataSetByIds(@Param("ids") List<String> ids);
}
