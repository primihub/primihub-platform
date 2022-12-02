package com.primihub.biz.service.data;

import com.primihub.biz.convert.DataSourceConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.SourceEnum;
import com.primihub.biz.entity.data.po.DataSource;
import com.primihub.biz.entity.data.req.DataSourceReq;
import com.primihub.biz.service.data.db.DataDBService;
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
        DataDBService dataDBService =getDBServiceImpl(req.getDbType());
        if (dataDBService != null){
            DataSource dataSource = DataSourceConvert.DataSourceReqConvertPo(req);
            return dataDBService.healthConnection(dataSource);
        }
        return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"您选择的数据库暂不支持");
    }

    public BaseResultEntity dataSourceTableDetails(DataSourceReq req) {
        DataDBService dataDBService =getDBServiceImpl(req.getDbType());
        if (dataDBService != null){
            DataSource dataSource = DataSourceConvert.DataSourceReqConvertPo(req);
            return dataDBService.dataSourceTableDetails(dataSource);
        }
        return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"您选择的数据库暂不支持");
    }


    private DataDBService getDBServiceImpl(Integer dbType){
        SourceEnum sourceEnum = SourceEnum.SOURCE_MAP.get(dbType);
        if (sourceEnum!=null) {
            return (DataDBService) context.getBean(sourceEnum.getSourceServiceClass());
        }
        return null;
    }

    public BaseResultEntity tableDataStatistics(DataSource dataSource,boolean isY){
        DataDBService dataDBService =getDBServiceImpl(dataSource.getDbType());
        if (dataDBService != null){
            return dataDBService.tableDataStatistics(dataSource,isY);
        }
        return null;
    }
    public BaseResultEntity dataSourceTableDetails(DataSource dataSource) {
        DataDBService dataDBService =getDBServiceImpl(dataSource.getDbType());
        if (dataDBService != null){
            return dataDBService.dataSourceTableDetails(dataSource);
        }
        return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"您选择的数据库暂不支持");
    }
}
