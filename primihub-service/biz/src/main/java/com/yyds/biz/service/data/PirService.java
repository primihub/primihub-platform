package com.yyds.biz.service.data;


import com.google.protobuf.ByteString;
import com.yyds.biz.config.base.BaseConfiguration;
import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.base.BaseResultEnum;
import com.yyds.biz.entity.data.po.DataResource;
import com.yyds.biz.grpc.client.WorkGrpcClient;
import com.yyds.biz.repository.secondarydb.data.DataResourceRepository;
import com.yyds.biz.util.crypt.DateUtil;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class PirService {
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private WorkGrpcClient workGrpcClient;

    public String getResultFilePath(String taskId,String taskDate){
        return new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(taskDate).append("/").append(taskId).append(".csv").toString();
    }

    public BaseResultEntity pirSubmitTask(Long resourceId, String pirParam) {
        DataResource dataResource = dataResourceRepository.queryDataResourceById(resourceId);
        if (dataResource==null|| StringUtils.isBlank(dataResource.getUrl()))
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        Date date = new Date();
        Map<String, Object> map = new HashMap<>();
        try {
            String uuId = UUID.randomUUID().toString();
            String formatDate = DateUtil.formatDate(date, DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat());
            map.put("taskId",uuId);
            map.put("taskDate",formatDate);
            StringBuilder sb = new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(formatDate).append("/").append(uuId).append(".csv");
            PushTaskReply reply = null;
            log.info("grpc run pirSubmitTask:{} - resourceId:{} - indices:{} - time:{}", sb.toString(), resourceId, pirParam, System.currentTimeMillis());
            Common.ParamValue indicesParamValue = Common.ParamValue.newBuilder().setValueString(pirParam).build();
            Common.ParamValue serverFullFilenameParamValue = Common.ParamValue.newBuilder().setValueString(dataResource.getUrl()).build();
            Common.ParamValue outputFullFilenameParamValue = Common.ParamValue.newBuilder().setValueString(sb.toString()).build();
            Common.Params params = Common.Params.newBuilder()
                    .putParamMap("indices", indicesParamValue)
                    .putParamMap("server_full_filename", serverFullFilenameParamValue)
                    .putParamMap("output_full_filename", outputFullFilenameParamValue)
                    .build();
            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.PIR_TASK)
                    .setParams(params)
                    .setName("testTask")
                    .setLanguage(Common.Language.JAVA)
                    .setCode("import sys;")
                    .setJobId(ByteString.copyFrom(resourceId.toString().getBytes(StandardCharsets.UTF_8)))
                    .setTaskId(ByteString.copyFrom(resourceId.toString().getBytes(StandardCharsets.UTF_8)))
                    .build();
            PushTaskRequest request = PushTaskRequest.newBuilder()
                    .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                    .setTask(task)
                    .setSequenceNumber(11)
                    .setClientProcessedUpTo(22)
                    .build();
            reply = workGrpcClient.run(o -> o.submitTask(request));
            log.info("grpc end pirSubmitTask:{} - resourceId:{} - indices:{} - time:{} - reply:{}", sb.toString(), resourceId, pirParam, System.currentTimeMillis(),reply.toString());
        } catch (Exception e) {
            log.info("grpc pirSubmitTask Exception:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL);
        }
        return BaseResultEntity.success(map);
    }
}
