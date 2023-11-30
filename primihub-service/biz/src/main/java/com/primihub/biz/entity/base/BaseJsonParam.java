package com.primihub.biz.entity.base;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Data
@ApiModel("Post 提交实体,Swagger来向忽略")
public class BaseJsonParam<T> {
    private String timestamp;
    private String nonce;
    private String token;
    private String sign;
    private T param;
}
