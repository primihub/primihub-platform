package com.primihub.biz.util.dbclient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.sql.*;

@Slf4j
public class HiveHelper {

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private String dbUrl;

    /**
     * 构造函数
     * @param dbUrl sqlite db 文件路径
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public HiveHelper(String dbUrl,String userName,String password) throws ClassNotFoundException, SQLException {
        this.dbUrl = dbUrl;
        if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)){
            connection = getConnection(dbUrl);
        }else {
            connection = getConnection(dbUrl,userName,password);
        }
    }

    /**
     * 获取数据库连接
     * @param dbUrl db文件路径
     * @return 数据库连接
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Connection getConnection(String dbUrl) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        // 1、加载驱动
        Class.forName("org.apache.hive.jdbc.HiveDriver");
        // 2、建立连接
        conn = DriverManager.getConnection(dbUrl);
        return conn;
    }

    /**
     * 获取数据库连接
     * @param dbUrl db文件路径
     * @return 数据库连接
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Connection getConnection(String dbUrl,String userName,String password) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        // 1、加载驱动
        Class.forName("org.apache.hive.jdbc.HiveDriver");
        // 2、建立连接
        conn = DriverManager.getConnection(dbUrl,userName,password);
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
            connection = getConnection(dbUrl);
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
            if (null != resultSet){
                resultSet.close();
                resultSet = null;
            }
            if (null != connection) {
                connection.close();
                connection = null;
            }

            if (null != statement) {
                statement.close();
                statement = null;
            }
        } catch (SQLException e) {
            log.info("hive数据库[{}]关闭时异常{} ",this.dbUrl,e);
        }
    }

    /**
     * 是否自动提交事务
     */
    public void setAutoCommit(Boolean status) throws SQLException {
        connection.setAutoCommit(status);
    }
}