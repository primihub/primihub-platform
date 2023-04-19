package com.primihub.biz;

import com.google.protobuf.ByteString;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.util.snowflake.SnowflakeId;
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
        Common.ParamValue aliceDataParamValue=Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom("./data/input/matrix.csv".getBytes(StandardCharsets.UTF_8))).build();
        Common.ParamValue bobDataParamValue=Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom("./data/input/matrix.csv".getBytes(StandardCharsets.UTF_8))).build();
        Common.Params params=Common.Params.newBuilder()
                .putParamMap("mBatchSize",mBatchSizeParamValue)
                .putParamMap("mIterations",mIterationsParamValue)
                .putParamMap("testN",testNParamValue)
                .putParamMap("aliceData",aliceDataParamValue)
                .putParamMap("bobData",bobDataParamValue)
                .build();
        Common.TaskContext taskBuild = Common.TaskContext.newBuilder().setJobId("1").setRequestId(String.valueOf(SnowflakeId.getInstance().nextId())).setTaskId("11212").build();
        primihub.rpc.Common.Task task= Common.Task.newBuilder()
                .setType(Common.TaskType.ACTOR_TASK)
                .setParams(params)
                .setName("testTask")
                .setLanguage(Common.Language.PROTO)
                .setCode(ByteString.copyFrom("import sys;".getBytes(StandardCharsets.UTF_8)))
                .setTaskInfo(taskBuild)
                .build();
        PushTaskRequest request=PushTaskRequest.newBuilder()
                .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                .setTask(task)
                .setSequenceNumber(11)
                .setClientProcessedUpTo(22)
                .build();
        Channel channel= ManagedChannelBuilder
                .forAddress("localhost",9090)
                .usePlaintext()
                .build();
        PushTaskReply reply = new WorkGrpcClient().run(o -> o.submitTask(request),channel);
        System.out.println("结果："+reply);
    }
}
