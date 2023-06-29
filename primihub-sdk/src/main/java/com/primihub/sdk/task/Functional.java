package com.primihub.sdk.task;

public interface Functional<Arg,Result> {
    Result run(Arg arg);
}
