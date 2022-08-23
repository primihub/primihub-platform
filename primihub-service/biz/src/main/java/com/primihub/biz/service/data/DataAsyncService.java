package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentRelationReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.entity.data.req.DataModelAndComponentReq;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.repository.primarydb.data.DataPsiPrRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataPsiRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.service.data.component.impl.StartComponentTaskServiceImpl;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.crypt.DateUtil;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * psi 异步调用实现
 */
@Service
@Slf4j
public class DataAsyncService implements ApplicationContextAware {

    private static ApplicationContext context;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context=applicationContext;
    }
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private DataPsiPrRepository dataPsiPrRepository;
    @Autowired
    private DataPsiRepository dataPsiRepository;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;
    @Autowired
    private DataModelPrRepository dataModelPrRepository;

    public BaseResultEntity executeBeanMethod(boolean isCheck,DataComponentReq req, ComponentTaskReq taskReq){
        String baenName = req.getComponentCode()+ DataConstant.COMPONENT_BEAN_NAME_SUFFIX;
        log.info("execute : {}",baenName);
        ComponentTaskService taskService = (ComponentTaskService)context.getBean(baenName);
        if (taskService==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,req.getComponentName()+"组件无实现方法");
        try {
            return isCheck?taskService.check(req,taskReq):taskService.runTask(req,taskReq);
        }catch (Exception e){
            log.info("ComponentCode:{} -- e:{}",req.getComponentCode(),e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,req.getComponentName()+"组件执行异常");
        }
    }



    @Async
    public void runModelTask(ComponentTaskReq req){
        for (DataComponent dataComponent : req.getDataComponents()) {
            dataComponent.setModelId(req.getDataModelTask().getModelId());
            dataComponent.setTaskId(req.getDataModelTask().getTaskId());
            dataModelPrRepository.saveDataComponent(dataComponent);
        }
        Map<String, DataComponent> dataComponentMap = req.getDataComponents().stream().collect(Collectors.toMap(DataComponent::getComponentCode, Function.identity()));
        for (DataModelComponent dataModelComponent : req.getDataModelComponents()) {
            dataModelComponent.setModelId(req.getDataModelTask().getModelId());
            dataModelComponent.setTaskId(req.getDataModelTask().getTaskId());
            dataModelComponent.setInputComponentId(dataModelComponent.getInputComponentCode() == null ? null : dataComponentMap.get(dataModelComponent.getInputComponentCode()) == null ? null : dataComponentMap.get(dataModelComponent.getInputComponentCode()).getComponentId());
            dataModelComponent.setOutputComponentId(dataModelComponent.getInputComponentCode() == null ? null : dataComponentMap.get(dataModelComponent.getOutputComponentCode()) == null ? null : dataComponentMap.get(dataModelComponent.getOutputComponentCode()).getComponentId());
            dataModelPrRepository.saveDataModelComponent(dataModelComponent);
        }
        // 重新组装json
        req.getDataModel().setComponentJson(formatModelComponentJson(req.getModelComponentReq(), dataComponentMap));
        req.getDataModel().setIsDraft(1);
        req.getDataTask().setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        dataTaskPrRepository.updateDataTask(req.getDataTask());
        Map<String, DataComponentReq> dataComponentReqMap = req.getModelComponentReq().getModelComponents().stream().collect(Collectors.toMap(DataComponentReq::getComponentCode, Function.identity()));
        req.getDataModelTask().setComponentJson(JSONObject.toJSONString(req.getDataComponents()));
        dataModelPrRepository.updateDataModelTask(req.getDataModelTask());
        for (DataComponent dataComponent : req.getDataComponents()) {
            if (req.getDataTask().getTaskState().equals(TaskStateEnum.FAIL.getStateType()))
                break;
            dataComponent.setStartTime(System.currentTimeMillis());
            dataComponent.setComponentState(2);
            req.getDataModelTask().setComponentJson(JSONObject.toJSONString(req.getDataComponents()));
            dataModelPrRepository.updateDataModelTask(req.getDataModelTask());
            dataComponent.setComponentState(1);
            executeBeanMethod(false, dataComponentReqMap.get(dataComponent.getComponentCode()), req);
            if(req.getDataTask().getTaskState().equals(TaskStateEnum.FAIL.getStateType()))
                dataComponent.setComponentState(3);
            dataComponent.setEndTime(System.currentTimeMillis());
            req.getDataModelTask().setComponentJson(JSONObject.toJSONString(req.getDataComponents()));
            dataModelPrRepository.updateDataModelTask(req.getDataModelTask());
        }
        req.getDataTask().setTaskEndTime(System.currentTimeMillis());
        dataTaskPrRepository.updateDataTask(req.getDataTask());
    }

    private String formatModelComponentJson(DataModelAndComponentReq params, Map<String, DataComponent> dataComponentMap){
        for (DataComponentReq modelComponent : params.getModelComponents()) {
            modelComponent.setComponentId(dataComponentMap.get(modelComponent.getComponentCode()).getComponentId().toString());
            for (DataComponentRelationReq req : modelComponent.getInput()) {
                req.setComponentId(dataComponentMap.get(req.getComponentCode()).getComponentId().toString());
            }
            for (DataComponentRelationReq req : modelComponent.getOutput()) {
                req.setComponentId(dataComponentMap.get(req.getComponentCode()).getComponentId().toString());
            }
        }
        return JSONObject.toJSONString(params);
    }


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
            Common.ParamValue psiTagParamValue=Common.ParamValue.newBuilder().setValueInt32(dataPsi.getTag()).build();
            int clientIndex = Arrays.asList(ownDataResource.getFileHandleField().split(",")).indexOf(dataPsi.getOwnKeyword());
            Common.ParamValue clientIndexParamValue=Common.ParamValue.newBuilder().setValueInt32(clientIndex).build();
            int serverIndex = Arrays.asList(resourceColumnNameList.split(",")).indexOf(dataPsi.getOtherKeyword());
            Common.ParamValue serverIndexParamValue=Common.ParamValue.newBuilder().setValueInt32(serverIndex).build();
            Common.ParamValue outputFullFilenameParamValue=Common.ParamValue.newBuilder().setValueString(psiTask.getFilePath()).build();
            Common.Params params=Common.Params.newBuilder()
                    .putParamMap("clientData",clientDataParamValue)
                    .putParamMap("serverData",serverDataParamValue)
                    .putParamMap("psiType",psiTypeParamValue)
                    .putParamMap("psiTag",psiTagParamValue)
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
                psiTaskOutputFileHandle(psiTask);
            }
        } catch (Exception e) {
            psiTask.setTaskState(3);
            dataPsiPrRepository.updateDataPsiTask(psiTask);
            log.info("grpc Exception:{}",e.getMessage());
        }
        log.info("grpc end dataPsiId:{} - psiTaskId:{} - outputFilePath{} - time:{}",dataPsi.getId(),psiTask.getId(),psiTask.getFilePath(),System.currentTimeMillis());
    }
    public void psiTaskOutputFileHandle(DataPsiTask task){
        if (task.getTaskState()!=1)
            return;
        List<String> fileContent = FileUtil.getFileContent(task.getFilePath(), null);
        StringBuilder sb = new StringBuilder();
        for (String line : fileContent) {
            sb.append(line).append("\r\n");
        }
        task.setFileContent(sb.toString());
        task.setFileRows(fileContent.size());
        dataPsiPrRepository.updateDataPsiTask(task);
    }

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
