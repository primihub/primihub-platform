package com.primihub.biz.entity.data.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("数据库连接参数")
public class DataSourceReq {
    @ApiModelProperty(value = "数据库ID")
    private Long id;
    @ApiModelProperty(value = "数据库类型,枚举从1开始依次对应",required = true,dataType = "com.primihub.biz.entity.data.dataenum.SourceEnum",example = "1")
    private Integer dbType;
    @ApiModelProperty(value = "数据库驱动器(https://github.com/primihub/primihub-platform/blob/develop/primihub-service/biz/src/main/java/com/primihub/biz/service/data/db/impl/dbenum/OtherEunm.java)",required = true)
    private String dbDriver;
    @ApiModelProperty(value = "数据库连接串",required = true)
    private String dbUrl;
    @ApiModelProperty(value = "数据库名称")
    private String dbName;
    @ApiModelProperty(value = "表名")
    private String dbTableName;
    @ApiModelProperty(value = "用户名")
    private String dbUsername;
    @ApiModelProperty(value = "密码")
    private String dbPassword;
}
