package com.primihub.biz.entity.data.req;

import lombok.Data;

/**
 * 数据库连接请求参数
 */
@Data
public class DataSourceReq {
    /** 数据库ID */
    private Long id;
    /** 数据库类型,枚举从1开始依次对应 */
    private Integer dbType;
    /**
     * 数据库驱动
     * <a href="https://github.com/primihub/primihub-platform/blob/develop/primihub-service/biz/src/main/java/com/primihub/biz/service/data/db/impl/dbenum/OtherEunm.java">...</a>
     */
    private String dbDriver;
    /** 数据库连接串 */
    private String dbUrl;
    /** 数据库名称 */
    private String dbName;
    /** 表名 */
    private String dbTableName;
    /** 用户名 */
    private String dbUsername;
    /** 密码 */
    private String dbPassword;
}
