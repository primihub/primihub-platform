package com.primihub.biz.repository.resourceprimarydb.data;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface DataResourcePrimaryRepository {
    void createDataTable(Map<String,Object> map);
    void dropDataTable(@Param("tableName") String tableName);
    void insertDataTable(Map<String,Object> map);
}
