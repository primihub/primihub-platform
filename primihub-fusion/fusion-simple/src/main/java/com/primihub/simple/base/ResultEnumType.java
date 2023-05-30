package com.primihub.simple.base;

public interface ResultEnumType<T, P> {
    T getReturnCode();
    P getMessage();
}
