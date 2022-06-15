package com.yyds.biz.entity.base;

public interface ResultEnumType<T, P> {
    T getReturnCode();
    P getMessage();
}
