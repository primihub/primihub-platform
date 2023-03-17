package com.primihub.biz.entity.data.req;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;

@Data
public class DataVisitingUsersReq {
    private String key;
    private String value;

    public String getKeyValLowerCase(){
        if ("age".equals(key)) return key;
        return (key+value).toLowerCase();
    }

    public boolean isNull(){
        return StringUtils.isBlank(key)||StringUtils.isBlank(value);
    }
}
