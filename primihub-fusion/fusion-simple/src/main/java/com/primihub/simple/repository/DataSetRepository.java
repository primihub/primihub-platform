package com.primihub.simple.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.primihub.entity.DataSet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DataSetRepository extends BaseMapper<DataSet> {
//public interface DataSetRepository extends IService<DatSet> {
}
