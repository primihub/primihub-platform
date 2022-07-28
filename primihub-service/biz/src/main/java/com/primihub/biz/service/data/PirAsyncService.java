package com.primihub.biz.service.data;

import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.crypt.DateUtil;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
@Slf4j
public class PirAsyncService {
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;
    @Autowired
    private BaseConfiguration baseConfiguration;

    @Async
    public void pirGrpcTask(DataTask dataTask, String resourceId, String pirParam, Integer resourceRowsCount) {
        Date date = new Date();
        try {
            String formatDate = DateUtil.formatDate(date, DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat());
            StringBuilder sb = new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(formatDate).append("/").append(dataTask.getTaskIdName()).append(".csv");
            dataTask.setTaskResultPath(sb.toString());
            PushTaskReply reply = null;
            log.info("grpc run pirSubmitTask:{} - resourceId_fileId:{} - queryIndeies:{} - time:{}", sb.toString(), resourceId, pirParam, System.currentTimeMillis());
            Common.ParamValue queryIndeiesParamValue = Common.ParamValue.newBuilder().setValueString(pirParam).build();
            Common.ParamValue serverDataParamValue = Common.ParamValue.newBuilder().setValueString(resourceId).build();
            Common.ParamValue databaseSizeParamValue = Common.ParamValue.newBuilder().setValueString(resourceRowsCount.toString()).build();
            Common.ParamValue outputFullFilenameParamValue = Common.ParamValue.newBuilder().setValueString(sb.toString()).build();
            Common.Params params = Common.Params.newBuilder()
                    .putParamMap("queryIndeies", queryIndeiesParamValue)
                    .putParamMap("serverData", serverDataParamValue)
                    .putParamMap("databaseSize", databaseSizeParamValue)
                    .putParamMap("outputFullFilename", outputFullFilenameParamValue)
                    .build();
            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.PIR_TASK)
                    .setParams(params)
                    .setName("testTask")
                    .setLanguage(Common.Language.PROTO)
                    .setCode("import sys;")
                    .setJobId(ByteString.copyFrom(resourceId.toString().getBytes(StandardCharsets.UTF_8)))
                    .setTaskId(ByteString.copyFrom(resourceId.toString().getBytes(StandardCharsets.UTF_8)))
                    .addInputDatasets("serverData")
                    .build();
            log.info("grpc Common.Task :\n{}",task.toString());
            PushTaskRequest request = PushTaskRequest.newBuilder()
                    .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                    .setTask(task)
                    .setSequenceNumber(11)
                    .setClientProcessedUpTo(22)
                    .build();
            reply = workGrpcClient.run(o -> o.submitTask(request));
            if (reply.getRetCode()==0){
                dataTask.setTaskState(TaskStateEnum.SUCCESS.getStateType());
                dataTask.setTaskResultContent(FileUtil.getFileContent(dataTask.getTaskResultPath()));
            }else {
                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataTask.setTaskErrorMsg("运行失败:"+reply.getRetCode());
            }
            log.info("grpc end pirSubmitTask:{} - resourceId_fileId:{} - queryIndeies:{} - time:{} - reply:{}", sb.toString(), resourceId, pirParam, System.currentTimeMillis(),reply.toString());
        } catch (Exception e) {
            dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
            dataTask.setTaskErrorMsg(e.getMessage());
            log.info("grpc pirSubmitTask Exception:{}",e.getMessage());
        }
        dataTaskPrRepository.updateDataTask(dataTask);
    }
}
