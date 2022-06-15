package com.primihub.biz.util.sql.mysql;

import com.alibaba.druid.stat.TableStat;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class AstMysqlSelectProperty {
    private Map<String, String> columnMap;
    private Map<String, String> outerColumnMap;
    private Map<String, String> tableNameMap;
    private Map<TableStat.Name, TableStat> tableMap;
    private Set<TableStat.Column> groupByList;
    private List<TableStat.Column> orderByList;
    private List<TableStat.Condition> conditionList;
    private Set<TableStat.Relationship> relationshipSet;
    private String beautifulSql;
}
