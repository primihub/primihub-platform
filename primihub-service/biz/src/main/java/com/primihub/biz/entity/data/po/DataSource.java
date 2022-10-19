package com.primihub.biz.entity.data.po;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.Date;

@Data
public class DataSource {
    private Long id;

    private Integer dbType;

    private String dbDriver;

    private String dbUrl;

    private String dbName;

    private String dbTableName;

    private String dbUsername;

    private String dbPassword;
    /**
     * 是否删除
     */
    @JsonIgnore
    private Integer isDel;

    /**
     * 创建时间
     */
    @JsonIgnore
    private Date createDate;

    /**
     * 修改时间
     */
    @JsonIgnore
    private Date updateDate;
}
