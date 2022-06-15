package com.primihub.biz.util.sql.mysql;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLStatement;
import com.alibaba.druid.sql.dialect.mysql.parser.MySqlStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.alibaba.druid.util.JdbcConstants;

import java.util.ArrayList;
import java.util.List;

public class AstMySqlStatementFactory {

    private AstMySqlStatementFactory() {
    }

    private static AstMysqlSelectProperty getAstMysqlSelectProperty(SQLStatement sqlStatement) {
        AstMySqlSelectVisitor astMySqlSelectVisitor = new AstMySqlSelectVisitor();
        sqlStatement.accept(astMySqlSelectVisitor);
        AstMysqlSelectProperty astMysqlSelectProperty =new AstMysqlSelectProperty(){
            {
                setColumnMap(astMySqlSelectVisitor.getColumnMap());
                setOuterColumnMap(astMySqlSelectVisitor.getOuterColumnMap());
                setTableNameMap(astMySqlSelectVisitor.getTableNameMap());
                setTableMap(astMySqlSelectVisitor.getTables());
                setGroupByList(astMySqlSelectVisitor.getGroupByColumns());
                setOrderByList(astMySqlSelectVisitor.getOrderByColumns());
                setConditionList(astMySqlSelectVisitor.getConditions());
                setRelationshipSet(astMySqlSelectVisitor.getRelationships());
                setBeautifulSql(astMySqlSelectVisitor.getBeautifulSql());
            }
        };
        return astMysqlSelectProperty;
    }

    public static AstMysqlSelectProperty generateAstMysqlSelectProperty(String sql){
        SQLStatementParser parser = new MySqlStatementParser(sql);
        SQLStatement sqlStatement = parser.parseStatement();
        AstMysqlSelectProperty astMysqlSelectProperty = getAstMysqlSelectProperty(sqlStatement);
        return astMysqlSelectProperty;
    }

    public static List<AstMysqlSelectProperty> generateAstMysqlSelectPropertyList(String sql){
        String dbType = JdbcConstants.MYSQL;
        List<SQLStatement> statementList = SQLUtils.parseStatements(sql, dbType);
        List<AstMysqlSelectProperty> result=new ArrayList<>();
        for(SQLStatement sqlStatement:statementList){
            AstMySqlSelectVisitor astMySqlSelectVisitor = new AstMySqlSelectVisitor();
            sqlStatement.accept(astMySqlSelectVisitor);
            AstMysqlSelectProperty astMysqlSelectProperty = getAstMysqlSelectProperty(sqlStatement);
            result.add(astMysqlSelectProperty);
        }
        return result;
    }
}
