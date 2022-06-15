package com.primihub.biz;

import com.primihub.biz.grpc.client.TestGrpcClient;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import java_test.Request;
import java_test.Result;

public class TestGrpcClientSpringTest {


//    @Test
    public void testRun() {
        Request request = Request.newBuilder().setRequest1("test1").setRequest2("test2").build();
        Channel channel= ManagedChannelBuilder
                .forAddress("localhost",9090)
                .usePlaintext()
                .build();
        Result result = new TestGrpcClient().run(o -> o.method(request),channel);
        System.out.println(result);
    }
}
