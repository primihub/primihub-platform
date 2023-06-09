package com.primihub.biz.config.database;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.nacos.api.config.annotation.NacosValue;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;

public class SecondaryDruidDataSourceWrapper extends DruidDataSource implements InitializingBean {
    @NacosValue(value="${spring.datasource.druid.secondary.url}",autoRefreshed = true)
    private String url;
    @NacosValue(value="${spring.datasource.druid.secondary.username}",autoRefreshed = true)
    private String username;
    @NacosValue(value="${spring.datasource.druid.secondary.password}",autoRefreshed = true)
    private String password;
    @NacosValue(value="${spring.datasource.druid.secondary.driver-class-name}",autoRefreshed = true)
    private String driverClassName;
    @NacosValue(value="${spring.datasource.druid.secondary.connection-properties}",autoRefreshed = true)
    private String connectionProperties;
    @NacosValue(value="${spring.datasource.druid.secondary.filter.config.enabled}",autoRefreshed = true)
    private Boolean filterConfigEnabled = false;


    public void setMaxWait(int maxWait) {
        this.maxWait = maxWait;
    }

    @Override
    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    @Override
    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    @Override
    public void setConnectionProperties(String connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        super.setUrl(url);
        super.setUsername(username);
        super.setPassword(password);
        super.setDriverClassName(driverClassName);
        super.setInitialSize(initialSize);
        super.setMinIdle(minIdle);
        super.setMaxActive(maxActive);
        super.setMaxWait(maxWait);
        super.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        super.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        super.setValidationQuery(validationQuery);
        super.setTestWhileIdle(testWhileIdle);
        super.setTestOnBorrow(testOnBorrow);
        super.setTestOnReturn(testOnReturn);
        super.setPoolPreparedStatements(poolPreparedStatements);
        super.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
        super.setConnectionProperties(connectionProperties);
        super.setDbType(dbType);
        if (filterConfigEnabled){
            super.addFilters("config");
        }
    }

}
