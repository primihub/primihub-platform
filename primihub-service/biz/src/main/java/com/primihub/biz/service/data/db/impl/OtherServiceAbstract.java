package com.primihub.biz.service.data.db.impl;


import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.constant.RedisKeyConstant;
import com.primihub.biz.convert.DataResourceConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.po.DataFileField;
import com.primihub.biz.entity.data.po.DataSource;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.data.db.AbstractDataDBService;
import com.primihub.biz.service.data.db.impl.dbenum.OtherEunm;
import com.primihub.biz.util.DataUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import com.primihub.sdk.util.FreemarkerTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.seatunnel.SeaTunnelEngineProxy;
import org.apache.seatunnel.engine.client.job.JobMetricsRunner;
import org.apache.seatunnel.engine.core.job.JobDAGInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OtherServiceAbstract extends AbstractDataDBService {

    @Resource
    private Environment environment;
    @Autowired
    private DataResourceService dataResourceService;

    @Resource(name="primaryStringRedisTemplate")
    private StringRedisTemplate primaryStringRedisTemplate;
    @Override
    public BaseResultEntity healthConnection(DataSource dbSource) {
        if (StringUtils.isBlank(dbSource.getDbName())){
            String dataBaseName = (String) DataUtil.getJDBCData(dbSource.getDbUrl()).get("database");
            if (StringUtils.isBlank(dataBaseName)) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"解析数据库名称失败");
            }
            dbSource.setDbName(dataBaseName);
        }
        return dataSourceTables(dbSource);
    }


    @Override
    public BaseResultEntity dataSourceTables(DataSource dbSource) {
        if (!OtherEunm.DB_DRIVER_MAP.containsKey(dbSource.getDbDriver())){
            return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL, dbSource.getDbDriver()+"当前驱动器还未开发,请持续关注我们的发布信息");
        }
        SeaTunnelEngineProxy instance = SeaTunnelEngineProxy.getInstance();
        if (!instance.open()){
            return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"您选择的数据库暂不支持,开源版仅支持csv、mysql、sqllite数据源，如需支持其他数据源请查询企业版相关信息");
        }
        OtherEunm otherEunm = OtherEunm.DB_DRIVER_MAP.get(dbSource.getDbDriver());
        String sql = otherEunm.tablesSql().replace("<database>",dbSource.getDbName());
        BaseResultEntity baseResultEntity = restoreJobContent(dbSource, sql, otherEunm);
        if (!baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
            return baseResultEntity;
        }
        List<Object> tableNameList = new ArrayList<>();
        List<Map<String, String>> result = (List<Map<String, String>>)baseResultEntity.getResult();
        for (Map<String, String> map : result) {
            for (String s : map.keySet()) {
                tableNameList.add(map.get(s));
            }
        }
        Map<String,Object> map = new HashMap<>();
        map.put("dbSource",dbSource);
        map.put("tableNames",tableNameList);
        return BaseResultEntity.success(map);
    }

    @Override
    public BaseResultEntity dataSourceTableDetails(DataSource dbSource) {
        if (!OtherEunm.DB_DRIVER_MAP.containsKey(dbSource.getDbDriver())){
            return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL, dbSource.getDbDriver()+"当前驱动器还未开发,请持续关注我们的发布信息");
        }
        SeaTunnelEngineProxy instance = SeaTunnelEngineProxy.getInstance();
        if (!instance.open()){
            return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"您选择的数据库暂不支持,开源版仅支持csv、mysql、sqllite数据源，如需支持其他数据源请查询企业版相关信息");
        }
        OtherEunm otherEunm = OtherEunm.DB_DRIVER_MAP.get(dbSource.getDbDriver());
        String sql = otherEunm.tablesDetailsSql().replace("<database>",dbSource.getDbName()).replace("<tableName>",dbSource.getDbTableName());
        BaseResultEntity baseResultEntity = restoreJobContent(dbSource, sql, otherEunm);
        if (!baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
            return baseResultEntity;
        }
        List<Map<String, Object>> result = (List<Map<String, Object>> )baseResultEntity.getResult();
        List<String> columns = result.get(0).keySet().stream().collect(Collectors.toList());
        List<DataFileField> dataFileFields = dataResourceService.batchInsertDataDataSourceField(columns, result.get(0));
        Map<String,Object> map = new HashMap<>();
        map.put("fieldList",dataFileFields.stream().map(DataResourceConvert::DataFileFieldPoConvertVo).collect(Collectors.toList()));
        map.put("dataList",result);
        return BaseResultEntity.success(map);
    }

    @Override
    public BaseResultEntity tableDataStatistics(DataSource dataSource, boolean isY) {
        if (!OtherEunm.DB_DRIVER_MAP.containsKey(dataSource.getDbDriver())){
            return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL, dataSource.getDbDriver()+"当前驱动器还未开发,请持续关注我们的发布信息");
        }
        SeaTunnelEngineProxy instance = SeaTunnelEngineProxy.getInstance();
        if (!instance.open()){
            return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"您选择的数据库暂不支持,开源版仅支持csv、mysql、sqllite数据源，如需支持其他数据源请查询企业版相关信息");
        }
        OtherEunm otherEunm = OtherEunm.DB_DRIVER_MAP.get(dataSource.getDbDriver());
        String tablesCountSql = otherEunm.tablesCountSql().replace("<database>",dataSource.getDbName()).replace("<tableName>", dataSource.getDbTableName());
        BaseResultEntity baseResultEntity = restoreJobContent(dataSource, tablesCountSql, otherEunm);
        if (!baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
            return baseResultEntity;
        }
        List<Map<String, Object>> result = (List<Map<String, Object>> )baseResultEntity.getResult();
        Map<String, Object> map = new HashMap<>();
        map.put("total",result.get(0).get(result.get(0).keySet().stream().collect(Collectors.toList()).get(0)));
        if (isY){
            String tablesCountYSql = otherEunm.tablesCountYSql().replace("<database>",dataSource.getDbName()).replace("<tableName>", dataSource.getDbTableName());
            baseResultEntity = restoreJobContent(dataSource, tablesCountYSql, otherEunm);
            if (!baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
                return baseResultEntity;
            }
            result = (List<Map<String, Object>> )baseResultEntity.getResult();
            map.put("ytotal",result.get(0).get(result.get(0).keySet().stream().collect(Collectors.toList()).get(0)));
        }
        return BaseResultEntity.success(map);
    }

    public Map<String,Object> getTemplatesParam(DataSource dataSource,String sql,String traceId){
        Map<String,Object> map = new HashMap<>();
        map.put("traceId",traceId);
        map.put("driver",dataSource.getDbDriver());
        map.put("url",dataSource.getDbUrl());
        map.put("dbusername",dataSource.getDbUsername());
        map.put("dbpassword",dataSource.getDbPassword());
        map.put("sql",sql);
        map.put("host",environment.getProperty("spring.rabbitmq.host"));
        map.put("port",environment.getProperty("spring.rabbitmq.port"));
        map.put("virtual_host",environment.getProperty("spring.rabbitmq.virtual-host"));
        map.put("username",environment.getProperty("spring.rabbitmq.username"));
        map.put("password",environment.getProperty("spring.rabbitmq.password"));
        if (map.get("virtual_host") == null || StringUtils.isBlank(map.get("virtual_host").toString())){
            map.put("virtual_host","/");
        }
        return map;
    }

    private BaseResultEntity restoreJobContent(DataSource dataSource,String sql,OtherEunm otherEunm){
        BaseResultEntity baseResult = null;
        try {
            String traceId = SnowflakeId.getInstance().nextId()+"";
            String content = FreemarkerTemplate.getInstance().generateTemplateStr(getTemplatesParam(dataSource, sql, traceId), otherEunm.template());
            Long jobInstanceId = System.currentTimeMillis();
            Long jobEngineId = System.currentTimeMillis();
            SeaTunnelEngineProxy.getInstance().restoreJobContent(content,jobInstanceId,jobEngineId);
            long start = System.currentTimeMillis();
            while (true){
                String jobPipelineStatusStr =  SeaTunnelEngineProxy.getInstance().getJobPipelineStatusStr(jobEngineId.toString());
                JSONObject jsonObject = JSONObject.parseObject(jobPipelineStatusStr);
                String jobStatus = jsonObject.getString("jobStatus");
                if ("FAILING".equals(jobStatus) || "FAILED".equals(jobStatus)){
                    // 失败
                    baseResult = BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,jsonObject.getString("errorMessage"));
                    break;
                }else if ("FINISHED".equals(jobStatus)){
                    String key = RedisKeyConstant.SEATUNNEL_DATA_LIST_KEY.replace("<traceId>", traceId);
                    JobMetricsRunner.JobMetricsSummary jobMetricsSummary = SeaTunnelEngineProxy.getInstance().getJobMetricsSummary(jobEngineId);
                    while (jobMetricsSummary.getSourceReadCount() != getRedisKeySize(key)){
                        if ((System.currentTimeMillis() - start)>=30000L){
                            break;
                        }
                        Thread.sleep(50L);
                    }
                    List<String> datas = primaryStringRedisTemplate.opsForList().range(key, Long.MIN_VALUE, Long.MAX_VALUE);
                    if (datas==null || datas.size()==0){
                        baseResult = BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"无数据");
                    }else {
                        baseResult = BaseResultEntity.success(datas.stream().map(d -> JSONObject.parseObject(d, Map.class)).collect(Collectors.toList()));
                    }
                    break;
                }
                if ((System.currentTimeMillis() - start)>=30000L){
                    log.info("Timeout exit traceId:{} jobPipelineStatusStr:{}",traceId,jobPipelineStatusStr);
                    baseResult = BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,"获取数据集信息超时!");
                    break;
                }
                Thread.sleep(50L);
            }
        }catch (Exception e){
            baseResult = BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL,e.getMessage());
            e.printStackTrace();
        }
        return baseResult;
    }


    private Long getRedisKeySize(String key){
        Long size = primaryStringRedisTemplate.opsForList().size(key);
        return size==null?0L:size;
    }
}
