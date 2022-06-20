package com.primihub.biz.service.data;

import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.po.DataPsi;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.repository.primarydb.data.DataPsiPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataPsiRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.entity.data.po.DataPsiTask;
import com.primihub.biz.util.crypt.DateUtil;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * psi 异步调用实现
 */
@Service
@Slf4j
public class PsiAsyncService {
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private DataPsiPrRepository dataPsiPrRepository;
    @Autowired
    private DataPsiRepository dataPsiRepository;
    @Autowired
    private DataPsiService dataPsiService;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private OrganConfiguration organConfiguration;

    @Async
    public void psiGrpcRun(DataPsiTask psiTask, DataPsi dataPsi){
        DataResource ownDataResource = dataResourceRepository.queryDataResourceById(dataPsi.getOwnResourceId());
        String resourceId,resourceColumnNameList;
        if (dataPsi.getOtherOrganId().equals(organConfiguration.getSysLocalOrganId())){
            DataResource otherDataResource = dataResourceRepository.queryDataResourceById(Long.parseLong(dataPsi.getOtherResourceId()));
            resourceId = StringUtils.isNotBlank(otherDataResource.getResourceFusionId())?otherDataResource.getResourceFusionId():otherDataResource.getResourceId().toString();
            resourceColumnNameList = otherDataResource.getFileHandleField();
        }else {
            BaseResultEntity dataResource = fusionResourceService.getDataResource(dataPsi.getServerAddress(), dataPsi.getOtherResourceId());
            if (dataResource.getCode()!=0)
                return;
            Map<String, Object> otherDataResource = (LinkedHashMap)dataResource.getResult();
            resourceId = otherDataResource.getOrDefault("resourceId","").toString();
            resourceColumnNameList = otherDataResource.getOrDefault("resourceColumnNameList","").toString();
        }

        Date date=new Date();
        StringBuilder sb=new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(DateUtil.formatDate(date,DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat())).append("/").append(psiTask.getTaskId()).append(".csv");
        psiTask.setFilePath(sb.toString());
        PushTaskReply reply = null;
        try {
            log.info("grpc run dataPsiId:{} - psiTaskId:{} - outputFilePath{} - time:{}",dataPsi.getId(),psiTask.getId(),psiTask.getFilePath(),System.currentTimeMillis());
            Common.ParamValue clientDataParamValue=Common.ParamValue.newBuilder().setValueString(ownDataResource.getResourceFusionId()).build();
            Common.ParamValue serverDataParamValue=Common.ParamValue.newBuilder().setValueString(resourceId).build();
            Common.ParamValue psiTypeParamValue=Common.ParamValue.newBuilder().setValueInt32(dataPsi.getOutputContent()).build();
            int clientIndex = Arrays.asList(ownDataResource.getFileHandleField().split(",")).indexOf(dataPsi.getOwnKeyword());
            Common.ParamValue clientIndexParamValue=Common.ParamValue.newBuilder().setValueInt32(clientIndex).build();
            int serverIndex = Arrays.asList(resourceColumnNameList.split(",")).indexOf(dataPsi.getOtherKeyword());
            Common.ParamValue serverIndexParamValue=Common.ParamValue.newBuilder().setValueInt32(serverIndex).build();
            Common.ParamValue outputFullFilenameParamValue=Common.ParamValue.newBuilder().setValueString(psiTask.getFilePath()).build();
            Common.Params params=Common.Params.newBuilder()
                    .putParamMap("clientData",clientDataParamValue)
                    .putParamMap("serverData",serverDataParamValue)
                    .putParamMap("psiType",psiTypeParamValue)
                    .putParamMap("clientIndex",clientIndexParamValue)
                    .putParamMap("serverIndex",serverIndexParamValue)
                    .putParamMap("outputFullFilename",outputFullFilenameParamValue)
                    .build();
            Common.Task task= Common.Task.newBuilder()
                    .setType(Common.TaskType.PSI_TASK)
                    .setParams(params)
                    .setName("testTask")
                    .setLanguage(Common.Language.PROTO)
                    .setCode("import sys;")
                    .setJobId(ByteString.copyFrom(dataPsi.getId().toString().getBytes(StandardCharsets.UTF_8)))
                    .setTaskId(ByteString.copyFrom(psiTask.getId().toString().getBytes(StandardCharsets.UTF_8)))
                    .addInputDatasets("clientData")
                    .addInputDatasets("serverData")
                    .build();
            log.info("grpc Common.Task : \n{}",task.toString());
            PushTaskRequest request=PushTaskRequest.newBuilder()
                    .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                    .setTask(task)
                    .setSequenceNumber(11)
                    .setClientProcessedUpTo(22)
                    .build();
            psiTask.setTaskState(2);
            dataPsiPrRepository.updateDataPsiTask(psiTask);
            reply = workGrpcClient.run(o -> o.submitTask(request));
            log.info("grpc结果:"+reply);
            DataPsiTask task1 = dataPsiRepository.selectPsiTaskById(psiTask.getId());
            if (task1.getTaskState()!=4){
                psiTask.setTaskState(1);
                dataPsiPrRepository.updateDataPsiTask(psiTask);
                dataPsiService.psiTaskOutputFileHandle(psiTask);
            }
        } catch (Exception e) {
            psiTask.setTaskState(3);
            dataPsiPrRepository.updateDataPsiTask(psiTask);
            log.info("grpc Exception:{}",e.getMessage());
        }
        log.info("grpc end dataPsiId:{} - psiTaskId:{} - outputFilePath{} - time:{}",dataPsi.getId(),psiTask.getId(),psiTask.getFilePath(),System.currentTimeMillis());
    }

    public static void main(String[] args) {
        System.out.println(DateUtil.formatDate(new Date(),DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat()));
    }
}
