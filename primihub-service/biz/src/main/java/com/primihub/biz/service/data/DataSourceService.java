package com.primihub.biz.service.data;

import com.primihub.biz.convert.DataSourceConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.SourceEnum;
import com.primihub.biz.entity.data.po.DataSource;
import com.primihub.biz.entity.data.req.DataSourceReq;
import com.primihub.biz.service.data.db.impl.MySqlService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class DataSourceService {

    @Autowired
    private MySqlService mySqlService;

    public BaseResultEntity healthConnection(DataSourceReq req) {
        DataSource dataSource = DataSourceConvert.DataSourceReqConvertPo(req);
        if (req.getDbType().equals(SourceEnum.mysql.getSourceType())){
            return mySqlService.healthConnection(dataSource);
        }
        return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"您选择的数据库暂不支持");
    }

    public BaseResultEntity dataSourceTableDetails(DataSourceReq req) {
        DataSource dataSource = DataSourceConvert.DataSourceReqConvertPo(req);
        if (req.getDbType().equals(SourceEnum.mysql.getSourceType())){
            return mySqlService.dataSourceTableDetails(dataSource);
        }
        return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"您选择的数据库暂不支持");
    }

    public BaseResultEntity tableDataStatistics(DataSource dataSource,boolean isY){
        if (dataSource.getDbType().equals(SourceEnum.mysql.getSourceType())){
            return mySqlService.tableDataStatistics(dataSource,isY);
        }
        return null;
    }
}
