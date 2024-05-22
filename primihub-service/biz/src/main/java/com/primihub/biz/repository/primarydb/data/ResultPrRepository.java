package com.primihub.biz.repository.primarydb.data;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;

@Repository
public interface ResultPrRepository {
    void savePsiResultList(@Param("list") List<LinkedHashMap<String, Object>> list);

    void savePirResultList(@Param("list") List<LinkedHashMap<String, Object>> list);
}
