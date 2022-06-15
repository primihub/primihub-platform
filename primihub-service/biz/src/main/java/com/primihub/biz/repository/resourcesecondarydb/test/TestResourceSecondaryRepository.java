package com.primihub.biz.repository.resourcesecondarydb.test;

import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface TestResourceSecondaryRepository {
    List<Map> findAll();
}
