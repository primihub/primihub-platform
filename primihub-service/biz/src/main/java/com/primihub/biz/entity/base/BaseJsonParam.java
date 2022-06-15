package com.primihub.biz.entity.base;

import lombok.Data;

@Data
public class BaseJsonParam<T> {
    private String timestamp;
    private String nonce;
    private String token;
    private String sign;
    private T param;
}
