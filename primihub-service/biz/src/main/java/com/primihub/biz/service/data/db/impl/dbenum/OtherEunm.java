package com.primihub.biz.service.data.db.impl.dbenum;

import java.util.HashMap;
import java.util.Map;

public enum OtherEunm implements OtherTemplate {

    HIVE{
        @Override
        public String tablesSql() {
            return "SHOW TABLES";
        }

        @Override
        public String tablesColumnsSql() {
            return "DESCRIBE <tableName>";
        }

        @Override
        public String tablesCountSql() {
            return "select count(*) total from <tableName>";
        }

        @Override
        public String tablesCountYSql() {
            return "select count(*) ytotal from <tableName>";
        }

        @Override
        public String tablesDetailsSql() {
            return "select * from <tableName> limit 50";
        }

        @Override
        public String driver() {
            return "org.apache.hive.jdbc.HiveDriver";
        }

        @Override
        public String template() {
            return "seatunnel_base.ftl";
        }
    },

    DM{
        @Override
        public String tablesSql() {
            return "SELECT TABLE_NAME FROM ALL_TABLES WHERE OWNER = '<database>'";
        }

        @Override
        public String tablesColumnsSql() {
            return "SELECT COLUMN_NAME FROM ALL_TAB_COLUMNS WHERE OWNER = '<database>' AND TABLE_NAME = '<tableName>'";
        }

        @Override
        public String tablesCountSql() {
            return "select count(*) total from \\\"<database>\\\".\\\"<tableName>\\\"";
        }

        @Override
        public String tablesCountYSql() {
            return "select count(*) ytotal from \\\"<database>\\\".\\\"<tableName>\\\"";
        }

        @Override
        public String tablesDetailsSql() {
            return "select TOP 50 * from \\\"<database>\\\".\\\"<tableName>\\\"";
        }

        @Override
        public String driver() {
            return "dm.jdbc.driver.DmDriver";
        }

        @Override
        public String template() {
            return "seatunnel_base.ftl";
        }
    },
    SQLSERVER{
        @Override
        public String tablesSql() {
            return "SELECT name FROM sysobjects WHERE xtype='U'";
        }

        @Override
        public String tablesColumnsSql() {
            return "SELECT name FROM SysColumns WHERE id=Object_Id('<tableName>')";
        }

        @Override
        public String tablesCountSql() {
            return "select count(*) total from <tableName>";
        }

        @Override
        public String tablesCountYSql() {
            return "select count(*) ytotal from <tableName>";
        }

        @Override
        public String tablesDetailsSql() {
            return "select TOP 50 * from <tableName>";
        }

        @Override
        public String driver() {
            return "com.microsoft.sqlserver.jdbc.SQLServerDriver";
        }

        @Override
        public String template() {
            return "seatunnel_base.ftl";
        }
    },
    ORACLE{
        @Override
        public String tablesSql() {
            return "SELECT owner||'.'||table_name FROM all_tables where tablespace_name = 'USERS'";
        }

        @Override
        public String tablesColumnsSql() {
            return null;
        }

        @Override
        public String tablesCountSql() {
            return "select count(*) total from <tableName>";
        }

        @Override
        public String tablesCountYSql() {
            return "select count(*) ytotal from <tableName>";
        }

        @Override
        public String tablesDetailsSql() {
            return "SELECT * FROM (SELECT t.* FROM <tableName> t WHERE ROWNUM <= 50) a";
        }

        @Override
        public String driver() {
            return "oracle.jdbc.OracleDriver";
        }

        @Override
        public String template() {
            return "seatunnel_base.ftl";
        }
    }

    ;
    public static Map<String, OtherEunm> DB_DRIVER_MAP=new HashMap(){
        {
            for (OtherEunm e:OtherEunm.values()){
                put(e.driver(),e);
            }
        }
    };


}
