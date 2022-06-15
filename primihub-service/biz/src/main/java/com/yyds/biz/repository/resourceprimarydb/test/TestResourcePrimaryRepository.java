package com.yyds.biz.repository.resourceprimarydb.test;

import org.springframework.stereotype.Repository;

@Repository
public interface TestResourcePrimaryRepository {
    void createTestTable();
    void dropTestTable();
    void insertTestTable();
}
