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
            return "hive.ftl";
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
