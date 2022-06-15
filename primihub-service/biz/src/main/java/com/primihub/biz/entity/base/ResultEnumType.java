package com.primihub.biz.entity.base;

public interface ResultEnumType<T, P> {
    T getReturnCode();
    P getMessage();
}
