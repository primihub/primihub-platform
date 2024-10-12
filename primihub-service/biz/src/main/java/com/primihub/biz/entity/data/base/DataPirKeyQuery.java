package com.primihub.biz.entity.data.base;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@ApiModel("查询条件")
@Data
public class DataPirKeyQuery {
    @ApiModelProperty(value = "查询key数组", required = true, example = "['name','age']")
    private String[] key;
    @ApiModelProperty(value = "查询内容数组", required = true, example = "[['zhangsan','20'], ['lisi','21']]")
    private List<String[]> query;
}
