package com.primihub.biz.entity.data.req;

import lombok.Data;

@Data
public class DataSourceReq {
    private Long id;

    private Integer dbType;

    private String dbDriver;

    private String dbUrl;

    private String dbName;

    private String dbTableName;

    private String dbUsername;

    private String dbPassword;
}
