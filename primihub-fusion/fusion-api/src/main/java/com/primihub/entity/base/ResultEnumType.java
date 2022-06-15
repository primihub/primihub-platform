package com.primihub.entity.base;

public interface ResultEnumType<T, P> {
    T getReturnCode();
    P getMessage();
}
