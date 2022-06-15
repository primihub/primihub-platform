package com.primihub.biz.config.database;

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Slf4j
@Configuration
@MapperScan(basePackages = "com.primihub.biz.repository.resourceprimarydb",sqlSessionFactoryRef="resourcePrimarySessionFactory")
public class ResourcePrimaryNacosDatabaseConfigConfiguration {

    @Value("classpath*:/mybatis/mapper/resourceprimarydb/**/*.xml")
    private String locationPattern;

    @Bean(name = "resourcePrimaryDB",initMethod = "init")
    public DruidDataSource dataSource() {
        log.info("Init Resource Primary DruidDataSource");
        return new ResourcePrimaryDruidDataSourceWrapper();
    }

    @Bean("resourcePrimarySessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("resourcePrimaryDB") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resolver.getResources(locationPattern));
        return sessionFactory.getObject();
    }

}
