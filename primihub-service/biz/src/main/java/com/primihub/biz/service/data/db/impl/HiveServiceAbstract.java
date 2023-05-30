package com.primihub.biz.service.data.db.impl;

import com.primihub.biz.convert.DataResourceConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.po.DataFileField;
import com.primihub.biz.entity.data.po.DataSource;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.data.db.AbstractDataDBService;
import com.primihub.biz.util.DataUtil;
import com.primihub.biz.util.dbclient.HiveHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class HiveServiceAbstract extends AbstractDataDBService {

    protected static final String QUERY_TABLES_SQL = "show tables in <dataBaseName>";
    protected static final String QUERY_DETAILS_SQL = "SELECT * FROM <tableName> LIMIT 50";
    protected static final String QUERY_COUNT_SQL = "select count(*) total from <tableName>";
    protected static final String QUERY_COUNT_Y_SQL = "select count(*) ytotal from <tableName>";

    @Autowired
    private DataResourceService dataResourceService;

    @Override
    public BaseResultEntity healthConnection(DataSource dbSource) {
        String url = dbSource.getDbUrl();
        String dataBaseName = (String) DataUtil.getJDBCData(url).get("database");
        if (StringUtils.isBlank(dataBaseName)) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"解析数据库名称失败");
        }
        dbSource.setDbName(dataBaseName);
        return dataSourceTables(dbSource);
    }

    @Override
    public BaseResultEntity dataSourceTables(DataSource dbSource) {
        HiveHelper hiveHelper = null;
        ResultSet resultSet = null;
        try {
            hiveHelper = new HiveHelper(dbSource.getDbUrl(),dbSource.getDbUsername(),dbSource.getDbPassword());
            resultSet = hiveHelper.executeQuery(QUERY_TABLES_SQL.replace("<dataBaseName>",dbSource.getDbName()));
            List<Object> tableNameList = new ArrayList<>();
            while (resultSet.next()){
                tableNameList.add(resultSet.getString("tab_name"));
            }
            Map<String,Object> map = new HashMap<>();
            map.put("dbSource",dbSource);
            map.put("tableNames",tableNameList);
            return BaseResultEntity.success(map);
        }catch (Exception e){
            e.printStackTrace();
            log.info("url:{}-------e:{}",dbSource.getDbUrl(),e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"连接失败,请检查数据源地址是否正确");
        }finally {
            if (hiveHelper!=null){
                hiveHelper.destroyed();
            }
        }
    }

    @Override
    public BaseResultEntity dataSourceTableDetails(DataSource dbSource) {
        HiveHelper hiveHelper = null;
        ResultSet resultSet = null;
        try {
            hiveHelper = new HiveHelper(dbSource.getDbUrl(),dbSource.getDbUsername(),dbSource.getDbPassword());
            resultSet = hiveHelper.executeQuery(QUERY_DETAILS_SQL.replace("<tableName>",dbSource.getDbTableName()));
            List<Map<String, Object>> details = new ArrayList<>();
            while (resultSet.next()){
                Map<String, Object> map = new HashMap<>();
                int columnCount = resultSet.getMetaData().getColumnCount();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = resultSet.getMetaData().getColumnName(i);
                    map.put(columnName,resultSet.getString(columnName));
                }
                details.add(map);
            }
            List<String> columns = details.get(0).keySet().stream().collect(Collectors.toList());
//            TreeSet<String> columns = new TreeSet<>(details.get(0).keySet());
            List<DataFileField> dataFileFields = dataResourceService.batchInsertDataDataSourceField(columns, details.get(0));
            Map<String,Object> map = new HashMap<>();
            map.put("fieldList",dataFileFields.stream().map(DataResourceConvert::DataFileFieldPoConvertVo).collect(Collectors.toList()));
            map.put("dataList",details);
            return BaseResultEntity.success(map);
        }catch (Exception e){
            log.info("url:{}-------e:{}",dbSource.getDbUrl(),e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"连接失败,请检查数据源地址是否正确");
        }finally {
            if (hiveHelper!=null){
                hiveHelper.destroyed();
            }
        }
    }

    @Override
    public BaseResultEntity tableDataStatistics(DataSource dataSource, boolean isY) {
        HiveHelper hiveHelper = null;
        ResultSet resultSet = null;
        try {
            hiveHelper = new HiveHelper(dataSource.getDbUrl(),dataSource.getDbUsername(),dataSource.getDbPassword());
            resultSet = hiveHelper.executeQuery(QUERY_COUNT_SQL.replace("<tableName>",dataSource.getDbTableName()));
            Map<String, Object> map = new HashMap<>();
            while (resultSet.next()){
                map.put("total",resultSet.getString("total"));
            }
            if (isY){
                resultSet = hiveHelper.executeQuery(QUERY_COUNT_Y_SQL.replace("<tableName>",dataSource.getDbTableName()));
                while (resultSet.next()){
                    map.put("ytotal",resultSet.getString("ytotal"));
                }
            }
            return BaseResultEntity.success(map);
        }catch (Exception e){
            log.info("url:{}-------e:{}",dataSource.getDbUrl(),e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"连接失败,请检查数据源地址是否正确");
        }finally {
            if (hiveHelper!=null){
                hiveHelper.destroyed();
            }
        }
    }
}
