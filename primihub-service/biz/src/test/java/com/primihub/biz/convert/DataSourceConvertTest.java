package com.primihub.biz.convert;

import com.primihub.biz.entity.data.po.DataSource;
import com.primihub.biz.entity.data.req.DataSourceReq;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataSourceConvertTest {

    @Test
    void dataSourceReqConvertPo_shouldMapAllFields() {
        DataSourceReq req = new DataSourceReq();
        req.setId(1L);
        req.setDbType(1);
        req.setDbDriver("com.mysql.cj.jdbc.Driver");
        req.setDbUrl("jdbc:mysql://localhost:3306/db");
        req.setDbName("testdb");
        req.setDbTableName("users");
        req.setDbUsername("root");
        req.setDbPassword("password");

        DataSource result = DataSourceConvert.DataSourceReqConvertPo(req);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getDbType()).isEqualTo(1);
        assertThat(result.getDbDriver()).isEqualTo("com.mysql.cj.jdbc.Driver");
        assertThat(result.getDbUrl()).isEqualTo("jdbc:mysql://localhost:3306/db");
        assertThat(result.getDbName()).isEqualTo("testdb");
        assertThat(result.getDbTableName()).isEqualTo("users");
        assertThat(result.getDbUsername()).isEqualTo("root");
        assertThat(result.getDbPassword()).isEqualTo("password");
    }
}
