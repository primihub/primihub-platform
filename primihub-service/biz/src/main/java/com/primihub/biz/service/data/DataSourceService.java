package com.primihub.biz.service.data;

import com.primihub.biz.convert.DataSourceConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.SourceEnum;
import com.primihub.biz.entity.data.po.DataSource;
import com.primihub.biz.entity.data.req.DataSourceReq;
import com.primihub.biz.service.data.db.AbstractDataDBService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class DataSourceService implements ApplicationContextAware {

    private static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context=applicationContext;
    }

    public BaseResultEntity healthConnection(DataSourceReq req) {
        AbstractDataDBService abstractDataDBService =getDBServiceImpl(req.getDbType());
        if (abstractDataDBService != null){
            DataSource dataSource = DataSourceConvert.DataSourceReqConvertPo(req);
            return abstractDataDBService.healthConnection(dataSource);
        }
        return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"您选择的数据库暂不支持");
    }

    public BaseResultEntity dataSourceTableDetails(DataSourceReq req) {
        AbstractDataDBService abstractDataDBService =getDBServiceImpl(req.getDbType());
        if (abstractDataDBService != null){
            DataSource dataSource = DataSourceConvert.DataSourceReqConvertPo(req);
            return abstractDataDBService.dataSourceTableDetails(dataSource);
        }
        return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"您选择的数据库暂不支持");
    }


    private AbstractDataDBService getDBServiceImpl(Integer dbType){
        SourceEnum sourceEnum = SourceEnum.SOURCE_MAP.get(dbType);
        if (sourceEnum!=null) {
            return (AbstractDataDBService) context.getBean(sourceEnum.getSourceServiceClass());
        }
        return null;
    }

    public BaseResultEntity tableDataStatistics(DataSource dataSource,boolean isY){
        AbstractDataDBService abstractDataDBService =getDBServiceImpl(dataSource.getDbType());
        if (abstractDataDBService != null){
            return abstractDataDBService.tableDataStatistics(dataSource,isY);
        }
        return null;
    }
    public BaseResultEntity dataSourceTableDetails(DataSource dataSource) {
        AbstractDataDBService abstractDataDBService =getDBServiceImpl(dataSource.getDbType());
        if (abstractDataDBService != null){
            return abstractDataDBService.dataSourceTableDetails(dataSource);
        }
        return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"您选择的数据库暂不支持");
    }
}
