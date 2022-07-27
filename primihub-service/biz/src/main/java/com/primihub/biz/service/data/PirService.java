package com.primihub.biz.service.data;


import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.util.crypt.CryptUtil;
import com.primihub.biz.util.crypt.DateUtil;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
@Slf4j
public class PirService {
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private OrganConfiguration organConfiguration;

    public String getResultFilePath(String taskId,String taskDate){
        return new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(taskDate).append("/").append(taskId).append(".csv").toString();
    }

    public BaseResultEntity pirSubmitTask(String resourceId, String pirParam) {
        Date date = new Date();
        Map<String, Object> map = new HashMap<>();
        try {
            String serverAddress = organConfiguration.getSysLocalOrganInfo().getFusionList().get(0).getServerAddress();
            BaseResultEntity dataResource = fusionResourceService.getDataResource(serverAddress, resourceId);
            if (dataResource.getCode()!=0)
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"资源查询失败");
            Map<String, Object> pirDataResource = (LinkedHashMap)dataResource.getResult();
            Object resourceRowsCountObj = pirDataResource.get("resourceRowsCount");
            if (resourceRowsCountObj==null)
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"资源行数获取错误");
            Integer resourceRowsCount = (Integer) resourceRowsCountObj - 1;
            String uuId = UUID.randomUUID().toString();
            String formatDate = DateUtil.formatDate(date, DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat());
            map.put("taskId",uuId);
            map.put("taskDate",formatDate);
            StringBuilder sb = new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(formatDate).append("/").append(uuId).append(".csv");
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
            log.info("grpc end pirSubmitTask:{} - resourceId_fileId:{} - queryIndeies:{} - time:{} - reply:{}", sb.toString(), resourceId, pirParam, System.currentTimeMillis(),reply.toString());
        } catch (Exception e) {
            log.info("grpc pirSubmitTask Exception:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL);
        }
        return BaseResultEntity.success(map);
    }
}
