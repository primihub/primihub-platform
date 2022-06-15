package com.primihub.biz;

import com.google.protobuf.ByteString;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;

public class WorkerGrpcClientSpringTest {

//    @Test
    public void testRun() {
        Common.ParamValue mBatchSizeParamValue=Common.ParamValue.newBuilder().setValueInt32(128).build();
        Common.ParamValue mIterationsParamValue=Common.ParamValue.newBuilder().setValueInt32(100).build();
        Common.ParamValue testNParamValue=Common.ParamValue.newBuilder().setValueInt32(100).build();
        Common.ParamValue aliceDataParamValue=Common.ParamValue.newBuilder().setValueString("./data/input/matrix.csv").build();
        Common.ParamValue bobDataParamValue=Common.ParamValue.newBuilder().setValueString("./data/input/matrix.csv").build();
        Common.Params params=Common.Params.newBuilder()
                .putParamMap("mBatchSize",mBatchSizeParamValue)
                .putParamMap("mIterations",mIterationsParamValue)
                .putParamMap("testN",testNParamValue)
                .putParamMap("aliceData",aliceDataParamValue)
                .putParamMap("bobData",bobDataParamValue)
                .build();
        primihub.rpc.Common.Task task= Common.Task.newBuilder()
                .setType(Common.TaskType.ACTOR_TASK)
                .setParams(params)
                .setName("testTask")
                .setLanguage(Common.Language.PROTO)
                .setCode("import sys;")
                .setJobId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                .setTaskId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                .build();
        PushTaskRequest request=PushTaskRequest.newBuilder()
                .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                .setTask(task)
                .setSequenceNumber(11)
                .setClientProcessedUpTo(22)
                .build();
        Channel channel= ManagedChannelBuilder
                .forAddress("118.190.39.100",27937)
                .forAddress("localhost",9090)
                .usePlaintext()
                .build();
        PushTaskReply reply = new WorkGrpcClient().run(o -> o.submitTask(request),channel);
        System.out.println("结果："+reply);
    }
}
