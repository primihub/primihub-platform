package com.primihub.biz.repository.resourcesecondarydb.data;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DataResourceSecondaryRepository {

    List<Map> findAll(@Param("sql") String sql);
}
