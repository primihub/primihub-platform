package com.primihub.biz.service.data.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.po.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;


public abstract class AbstractDataDBService {


    /**
     * database connection
     * @param dbSource
     * @return
     */
    public abstract BaseResultEntity healthConnection(DataSource dbSource);

    /**
     * Database Table List
     * @param dbSource
     * @return
     */
    public abstract BaseResultEntity dataSourceTables(DataSource dbSource);

    /**
     * Database Table Details
     * @param dbSource
     * @return
     */
    public abstract BaseResultEntity dataSourceTableDetails(DataSource dbSource);

    public abstract BaseResultEntity tableDataStatistics(DataSource dataSource, boolean isY);

    protected DruidDataSource getJdbcDataSource(DataSource dbSource) {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setDriverClassName(dbSource.getDbDriver());
        dataSource.setUrl(dbSource.getDbUrl());
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setBreakAfterAcquireFailure(true);
        dataSource.setConnectionErrorRetryAttempts(0);
        dataSource.setUsername(dbSource.getDbUsername());
        dataSource.setMaxWait(5000);
        dataSource.setPassword(dbSource.getDbPassword());
        return dataSource;
    }
    protected JdbcTemplate getJdbcTemplate(DruidDataSource dataSource){
        return new JdbcTemplate(dataSource);
    }

    protected JdbcTemplate getJdbcTemplate(DataSource dbSource){
        return new JdbcTemplate(getJdbcDataSource(dbSource));
    }
}
