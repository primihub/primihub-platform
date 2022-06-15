package com.primihub.biz.repository.secondarydb.test;

import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public interface TestSecondaryRepository {
    Map testFindOneData();
}
