package com.primihub.simple.initial;

import com.primihub.simple.service.AsyncService;
import com.primihub.simple.util.SqliteHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class SqlLite3Initial {
    private final static String QUERY_TABLES_SQL = "select name from sqlite_master where type='table' order by name desc";
    @Value("${spring.datasource.url}")
    private String jdbcUrl;
    @Value("${db.path}")
    private String dbPath;
    @Autowired
    private AsyncService asyncService;

    @PostConstruct
    public void init() throws SQLException, ClassNotFoundException {
        if ("".equals(dbPath)){
            return;
        }
        File file = new File(dbPath);
        if (!file.exists()){
            file.mkdirs();
        }
        if (!"".equals(jdbcUrl)){
            String path = jdbcUrl.substring(jdbcUrl.lastIndexOf(":")+1);
            file = new File(path);
            if (!file.isFile()){
                SqliteHelper sqliteHelper = new SqliteHelper(path);
                ResultSet resultSet = sqliteHelper.executeQuery(QUERY_TABLES_SQL);
                List<String> tableNameList = new ArrayList<>();
                while (resultSet.next()){
                    tableNameList.add(resultSet.getString("name"));
                }
                if (!tableNameList.contains("data_set")){
                    sqliteHelper.executeUpdate(getDataSetCreateTableDDL());
                }
                sqliteHelper.destroyed();
            }else {
                asyncService.startSync();
            }
        }
    }

    public String getDataSetCreateTableDDL(){
        return "CREATE TABLE \"data_set\" (\n" +
                "  \"id\" text NOT NULL,\n" +
                "  \"access_info\" text,\n" +
                "  \"driver\" TEXT,\n" +
                "  \"address\" TEXT NOT NULL,\n" +
                "  \"vibility\" TEXT,\n" +
                "  \"available\" TEXT,\n" +
                "  \"holder\" TEXT,\n" +
                "  \"fields\" TEXT,\n" +
                "  PRIMARY KEY (\"id\")\n" +
                ");";
    }
}
