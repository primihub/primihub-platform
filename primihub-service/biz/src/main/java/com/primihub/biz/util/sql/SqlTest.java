package com.primihub.biz.util.sql;

import com.primihub.biz.util.sql.mysql.AstMySqlStatementFactory;
import com.primihub.biz.util.sql.mysql.AstMysqlSelectProperty;

public class SqlTest {
    public static void main(String[] args) {
        String sql = "select a.xxx xyz,a.id,t.id from time t left join another a on t.id=a.id where a.id=1 and a.name='ming' group by t.uid limit 1,200 order by a.ctime";
//        sql = "select id from (select name,id from x where id=1) t";
        AstMysqlSelectProperty astMysqlSelectProperty = AstMySqlStatementFactory.generateAstMysqlSelectProperty(sql);
        System.out.println("my column map:"+ astMysqlSelectProperty.getColumnMap());
        System.out.println("my outer column map:"+ astMysqlSelectProperty.getOuterColumnMap());
        System.out.println("my table name map:"+ astMysqlSelectProperty.getTableNameMap());
        System.out.println("my table map:"+ astMysqlSelectProperty.getTableMap());
        System.out.println("my group by list:"+ astMysqlSelectProperty.getGroupByList());
        System.out.println("my order by list:"+ astMysqlSelectProperty.getOrderByList());
        System.out.println("my condition list:"+ astMysqlSelectProperty.getConditionList());
        System.out.println("my relationship list:"+ astMysqlSelectProperty.getRelationshipSet());
        System.out.println("beautiful sql:");
        System.out.println(astMysqlSelectProperty.getBeautifulSql());
    }
}


