package com.yyds.biz.config.database;

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
@MapperScan(basePackages = "com.yyds.biz.repository.resourcesecondarydb",sqlSessionFactoryRef="resourceSecondarySessionFactory")
public class ResourceSecondaryNacosDatabaseConfigConfiguration {

    @Value("classpath*:/mybatis/mapper/resourcesecondarydb/**/*.xml")
    private String locationPattern;

    @Bean(name = "resourceSecondaryDB",initMethod = "init")
    public DruidDataSource dataSource() {
        log.info("Init Resource Secondary DruidDataSource");
        return new ResourceSecondaryDruidDataSourceWrapper();
    }

    @Bean("resourceSecondarySessionFactory")
    public SqlSessionFactory sqlSessionFactory(@Qualifier("resourceSecondaryDB") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
        sessionFactory.setDataSource(dataSource);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sessionFactory.setMapperLocations(resolver.getResources(locationPattern));
        return sessionFactory.getObject();
    }

}
