package com.primihub.biz.entity.data.req;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
@ApiModel("提交来访用户信息参数")
public class DataVisitingUsersReq {
    @ApiModelProperty(value = "key",required = true)
    private String key;
    @ApiModelProperty(value = "value",required = true)
    private String value;

    public String getKeyValLowerCase(){
        if ("age".equals(key)) {
            return key;
        }
        return (key+value).toLowerCase();
    }

    public boolean isNull(){
        return StringUtils.isBlank(key)||StringUtils.isBlank(value);
    }
}