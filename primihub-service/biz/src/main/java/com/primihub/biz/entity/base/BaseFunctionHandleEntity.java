package com.primihub.biz.entity.base;

import com.alibaba.fastjson.JSON;
import lombok.Data;

@Data
public class BaseFunctionHandleEntity {
    private String handleType;
    private String paramStr;

    public BaseFunctionHandleEntity(String handleType, String paramStr) {
        this.handleType = handleType;
        this.paramStr = paramStr;
    }

    public BaseFunctionHandleEntity(String handleType, Object paramObject) {
        this.handleType = handleType;
        this.paramStr = JSON.toJSONString(paramObject);
    }

    public BaseFunctionHandleEntity() {
    }
}
