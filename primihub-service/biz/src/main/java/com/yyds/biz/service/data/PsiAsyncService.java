package com.yyds.biz.service.data;

import com.google.protobuf.ByteString;
import com.yyds.biz.config.base.BaseConfiguration;
import com.yyds.biz.entity.data.po.DataPsi;
import com.yyds.biz.entity.data.po.DataPsiTask;
import com.yyds.biz.entity.data.po.DataResource;
import com.yyds.biz.grpc.client.WorkGrpcClient;
import com.yyds.biz.repository.primarydb.data.DataPsiPrRepository;
import com.yyds.biz.repository.secondarydb.data.DataPsiRepository;
import com.yyds.biz.repository.secondarydb.data.DataResourceRepository;
import com.yyds.biz.util.crypt.DateUtil;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.Date;

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

    @Async
    public void psiGrpcRun(DataPsiTask psiTask, DataPsi dataPsi){
        DataResource ownDataResource = dataResourceRepository.queryDataResourceById(dataPsi.getOwnResourceId());
        DataResource otherDataResource = dataResourceRepository.queryDataResourceById(dataPsi.getOtherResourceId());
        // out
        Date date=new Date();
        StringBuilder sb=new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(DateUtil.formatDate(date,DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat())).append("/").append(psiTask.getTaskId()).append(".csv");
//        StringBuilder sb=new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(psiTask.getTaskId()).append(".csv");
        psiTask.setFilePath(sb.toString());
        PushTaskReply reply = null;
        try {
            log.info("grpc run dataPsiId:{} - psiTaskId:{} - outputFilePathL{} - time:{}",dataPsi.getId(),psiTask.getId(),psiTask.getFilePath(),System.currentTimeMillis());
            Common.ParamValue revealIntersectionParamValue=Common.ParamValue.newBuilder().setValueInt32(1).build();
            Common.ParamValue psiTypeParamValue=Common.ParamValue.newBuilder().setValueInt32(dataPsi.getOutputContent()).build();
            Common.ParamValue clientFullFilenameParamValue=Common.ParamValue.newBuilder().setValueString(ownDataResource.getUrl()).build();
            Integer colParamIndex = getValIndexByKey(ownDataResource.getFileHandleField().split(","), dataPsi.getOwnKeyword());
            Common.ParamValue colParamValue=Common.ParamValue.newBuilder().setValueInt32(colParamIndex).build();
            Common.ParamValue serverFullFilenameParamValue=Common.ParamValue.newBuilder().setValueString(otherDataResource.getUrl()).build();
            Integer serverColParamIndex = getValIndexByKey(otherDataResource.getFileHandleField().split(","), dataPsi.getOtherKeyword());
            Common.ParamValue serverColParamValue=Common.ParamValue.newBuilder().setValueInt32(serverColParamIndex).build();
            Common.ParamValue clientStartRowParamValue=Common.ParamValue.newBuilder().setValueInt32(2).build();
            Common.ParamValue serverStartRowParamValue=Common.ParamValue.newBuilder().setValueInt32(2).build();
            Common.ParamValue outputFullFilenameParamValue=Common.ParamValue.newBuilder().setValueString(psiTask.getFilePath()).build();
            Common.Params params=Common.Params.newBuilder()
                    .putParamMap("reveal_intersection",revealIntersectionParamValue)
                    .putParamMap("psi_type",psiTypeParamValue)
                    .putParamMap("client_full_filename",clientFullFilenameParamValue)
                    .putParamMap("client_col",colParamValue)
                    .putParamMap("client_start_row",clientStartRowParamValue)
                    .putParamMap("server_full_filename",serverFullFilenameParamValue)
                    .putParamMap("server_col",serverColParamValue)
                    .putParamMap("server_start_row",serverStartRowParamValue)
                    .putParamMap("output_full_filename",outputFullFilenameParamValue)
                    .build();
            Common.Task task= Common.Task.newBuilder()
                    .setType(Common.TaskType.PSI_TASK)
                    .setParams(params)
                    .setName("testTask")
                    .setLanguage(Common.Language.JAVA)
                    .setCode("import sys;")
                    .setJobId(ByteString.copyFrom(dataPsi.getId().toString().getBytes(StandardCharsets.UTF_8)))
                    .setTaskId(ByteString.copyFrom(psiTask.getId().toString().getBytes(StandardCharsets.UTF_8)))
                    .build();
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
        log.info("grpc end dataPsiId:{} - psiTaskId:{} - outputFilePathL{} - time:{}",dataPsi.getId(),psiTask.getId(),psiTask.getFilePath(),System.currentTimeMillis());
    }

    public Integer getValIndexByKey(String[] arr,String key){
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(key)){
                return i+1;
            }
        }
        return 1;
    }
}
