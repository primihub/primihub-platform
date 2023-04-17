package com.primihub.biz.util.sql.mysql;

import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLName;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.ast.statement.SQLSelectItem;
import com.alibaba.druid.sql.ast.statement.SQLSelectQueryBlock;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Getter
public class AstMySqlSelectVisitor extends SchemaStatVisitor {

    private Map<String, String> columnMap = new TreeMap<>();
    private Map<String, String> outerColumnMap = new TreeMap<>();
    private Map<String, String> tableNameMap = new TreeMap<>();
    private String beautifulSql;

    @Override
    public boolean visit(SQLSelectQueryBlock x) {
        List<SQLSelectItem> selectItemList = x.getSelectList();

        selectItemList.forEach(selectItem -> {
            columnMap.put(SQLUtils.toMySqlString(selectItem.getExpr()), selectItem.getAlias() == null ? "" : selectItem.getAlias());
        });

        if(outerColumnMap.size()==0) {
            selectItemList.forEach(selectItem -> {
                outerColumnMap.put(SQLUtils.toMySqlString(selectItem.getExpr()), selectItem.getAlias() == null ? "" : selectItem.getAlias());
            });
        }

        if(beautifulSql==null) {
            beautifulSql=x.getParent().toString();
        }
        return true;
    }

    @Override
    public boolean visit(SQLExprTableSource x) {
        SQLName table = (SQLName) x.getExpr();
        tableNameMap.put(table.getSimpleName(),x.getAlias());
        return true;
    }

}

