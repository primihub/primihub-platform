package com.primihub.biz.entity.base;

import lombok.Data;

/**
 * Post请求体 提交实体
 */
@Data
public class BaseJsonParam<T> {
    private String timestamp;
    private String nonce;
    private String token;
    private String sign;
    private T param;
}
