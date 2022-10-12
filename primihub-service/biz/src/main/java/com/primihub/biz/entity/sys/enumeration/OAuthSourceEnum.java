package com.primihub.biz.entity.sys.enumeration;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum OAuthSourceEnum {

    WEIXIN(1,"weixin"),
    GITHUB(2,"github"),
    ;

    private Integer sourceCode;
    private String sourceName;

    OAuthSourceEnum(Integer sourceCode, String sourceName) {
        this.sourceCode = sourceCode;
        this.sourceName = sourceName;
    }

    public static Map<String, OAuthSourceEnum> AUTH_MAP=new HashMap(){
        {
            for (OAuthSourceEnum e:OAuthSourceEnum.values()){
                put(e.getSourceName(),e);
            }
        }
    };

}
