package com.primihub.biz.entity.data.req;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

/**
 * 提交来访用户信息参数
 */
@Data
public class DataVisitingUsersReq {
    /** key */
    private String key;
    /** value */
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