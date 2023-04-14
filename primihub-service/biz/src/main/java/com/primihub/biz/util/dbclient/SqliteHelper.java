package com.primihub.biz.util.dbclient;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;

@Slf4j
public class SqliteHelper {

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private String dbFilePath;

    /**
     * 构造函数
     * @param dbFilePath sqlite db 文件路径
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public SqliteHelper(String dbFilePath) throws ClassNotFoundException, SQLException {
        this.dbFilePath = dbFilePath;
        connection = getConnection(dbFilePath);
    }

    /**
     * 获取数据库连接
     * @param dbFilePath db文件路径
     * @return 数据库连接
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Connection getConnection(String dbFilePath) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        // 1、加载驱动
        Class.forName("org.sqlite.JDBC");
        // 2、建立连接
        // 注意：此处有巨坑，如果后面的 dbFilePath 路径太深或者名称太长，则建立连接会失败
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        return conn;
    }

    /**
     * 执行sql查询
     * @param sql sql select 语句
     * @return 查询结果
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public ResultSet executeQuery(String sql) throws SQLException, ClassNotFoundException {
        resultSet = getStatement().executeQuery(sql);
        return resultSet;
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        if (null == connection) {
            connection = getConnection(dbFilePath);
        }
        return connection;
    }

    private Statement getStatement() throws SQLException, ClassNotFoundException {
        if (null == statement) {
            statement = getConnection().createStatement();
        }
        return statement;
    }

    /**
     * 数据库资源关闭和释放
     */
    public void destroyed() {
        try {
            if (null != connection) {
                connection.close();
                connection = null;
            }

            if (null != statement) {
                statement.close();
                statement = null;
            }

            if (null != resultSet){
                resultSet.close();
                resultSet = null;
            }
        } catch (SQLException e) {
            log.info("Sqlite数据库[{}]关闭时异常{} ",this.dbFilePath,e);
        }
    }

    /**
     * 是否自动提交事务
     */
    public void setAutoCommit(Boolean status) throws SQLException {
        connection.setAutoCommit(status);
    }
}