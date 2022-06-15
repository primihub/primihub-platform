package com.yyds.biz.grpc.client;

public interface Functional<Arg,Result> {
    Result run(Arg arg);
}
