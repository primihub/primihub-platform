package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dto.ModelDerivationDto;
import com.primihub.biz.entity.data.po.DataModelResource;
import com.primihub.biz.entity.data.po.DataPsiTask;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.entity.data.vo.ModelProjectResourceVo;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.FreemarkerUtil;
import com.primihub.biz.util.crypt.DateUtil;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import primihub.rpc.Common;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service("dataAlignComponentTaskServiceImpl")
public class DataAlignComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private DataModelPrRepository dataModelPrRepository;

    @Override
    public BaseResultEntity check(DataComponentReq req, ComponentTaskReq taskReq) {
        return componentTypeVerification(req, baseConfiguration.getModelComponents(), taskReq);
    }


    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        BaseResultEntity baseResultEntity = runPsi(req, taskReq);
        if (baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
            runAssemblyData((Map<String, ModelEntity>)baseResultEntity.getResult(),req,taskReq);
        }else {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(baseResultEntity.getMsg());
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity runAssemblyData(Map<String, ModelEntity> map,DataComponentReq req, ComponentTaskReq taskReq){
        Map<String, String> freemarkerMap = new HashMap<>();
        freemarkerMap.put(DataConstant.PYTHON_LABEL_DATASET,taskReq.getFreemarkerMap().get(DataConstant.PYTHON_LABEL_DATASET));
        freemarkerMap.put(DataConstant.PYTHON_GUEST_DATASET,taskReq.getFreemarkerMap().get(DataConstant.PYTHON_GUEST_DATASET));
        String freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_DATA_ALIGN_PAHT, freeMarkerConfigurer, freemarkerMap);
        log.info(freemarkerContent);
        if (freemarkerContent != null) {
            try {
                log.info("runAssemblyDatamap:{}", JSONObject.toJSONString(map));
                Common.ParamValue detailParamValue = Common.ParamValue.newBuilder().setValueString(JSONObject.toJSONString(map)).build();
                Common.Params params = Common.Params.newBuilder()
                        .putParamMap("detail", detailParamValue)
                        .build();
                Common.Task task = Common.Task.newBuilder()
                        .setType(Common.TaskType.ACTOR_TASK)
                        .setParams(params)
                        .setName("runAssemblyData")
                        .setLanguage(Common.Language.PYTHON)
                        .setCodeBytes(ByteString.copyFrom(freemarkerContent.getBytes(StandardCharsets.UTF_8)))
                        .setJobId(ByteString.copyFrom(taskReq.getDataTask().getTaskIdName().getBytes(StandardCharsets.UTF_8)))
                        .setTaskId(ByteString.copyFrom(taskReq.getDataTask().getTaskIdName().getBytes(StandardCharsets.UTF_8)))
                        .build();
                log.info("grpc Common.Task :\n{}", task.toString());
                PushTaskRequest request = PushTaskRequest.newBuilder()
                        .setIntendedWorkerId(ByteString.copyFrom("1".getBytes(StandardCharsets.UTF_8)))
                        .setTask(task)
                        .setSequenceNumber(11)
                        .setClientProcessedUpTo(22)
                        .build();
                PushTaskReply reply = workGrpcClient.run(o -> o.submitTask(request));
                log.info("grpc结果:{}", reply.toString());
                if (reply.getRetCode() == 2) {
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"组件处理失败");
                } else {
                    List<ModelDerivationDto> derivationList = new ArrayList<>();
                    Iterator<Map.Entry<String, ModelEntity>> iterator = map.entrySet().iterator();
                    while (iterator.hasNext()) {
                        Map.Entry<String, ModelEntity> next = iterator.next();
                        String key = next.getKey();
                        ModelEntity value = next.getValue();
                        derivationList.add(new ModelDerivationDto(key, null, req.getComponentName(), value.getNewDataSetId(),value.getOutputPath()));
                    }
                    taskReq.getDerivationList().addAll(derivationList);
                    taskReq.setNewest(derivationList);
                    // derivation resource datas
                    BaseResultEntity derivationResource = dataResourceService.saveDerivationResource(derivationList, taskReq.getDataTask().getTaskUserId());
                    log.info(JSONObject.toJSONString(derivationResource));
                    if (!derivationResource.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())) {
                        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                        taskReq.getDataTask().setTaskErrorMsg( req.getComponentName()+"组件处理失败:" + derivationResource.getMsg());
                    } else {
                        List<String> resourceIds = (List<String>) derivationResource.getResult();
                        for (String resourceId : resourceIds) {
                            DataModelResource dataModelResource = new DataModelResource(taskReq.getDataModel().getModelId());
                            dataModelResource.setTaskId(taskReq.getDataTask().getTaskId());
                            dataModelResource.setResourceId(resourceId);
                            dataModelPrRepository.saveDataModelResource(dataModelResource);
                            taskReq.getDmrList().add(dataModelResource);
                        }
                    }
                }
            } catch (Exception e) {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"处理组件:" + e.getMessage());
                log.info("grpc Exception:{}", e.getMessage());
                e.printStackTrace();
            }
        }else {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"处理组件:模板异常");
        }
        return BaseResultEntity.success();
    }

    @Data
    public class ModelEntity {
        public ModelEntity(String psiPath, int index) {
            this.newDataSetId= UUID.randomUUID().toString();
            this.psiPath = psiPath + newDataSetId +".csv";
            this.index = index;
        }

        private String newDataSetId;
        private String psiPath;
        private String outputPath;
        private int index;
    }

    public BaseResultEntity runPsi(DataComponentReq req,ComponentTaskReq taskReq){
        List<ModelProjectResourceVo> resourceList = taskReq.getResourceList();
        Map<Integer, List<ModelProjectResourceVo>> resourceMap = resourceList.stream().collect(Collectors.groupingBy(ModelProjectResourceVo::getParticipationIdentity));
        PushTaskReply reply = null;
        Map<String, ModelEntity> map = null;
        try {
            List<ModelProjectResourceVo> projectResource = resourceMap.get(1);
            if (projectResource==null || projectResource.size()==0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐查询不到发起方资源");
            ModelProjectResourceVo clientData = projectResource.get(0);
            projectResource = resourceMap.get(2);
            if (projectResource==null || projectResource.size()==0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐查询不到发起方资源");
            ModelProjectResourceVo serverData = projectResource.get(0);
            Map<String, String> componentVals = getComponentVals(req.getComponentValues());
            String dataAlign = componentVals.get("dataAlign");
            if (StringUtils.isBlank(dataAlign))
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐选择为空");
            int clientIndex = 0;
            int serverIndex = 0;
            if ("1".equals(dataAlign)){
                clientIndex = clientData.getFileHandleField().indexOf("id");
                serverIndex = serverData.getFileHandleField().indexOf("id");
            }else {
                String multipleSelected = componentVals.get("MultipleSelected");
                if (StringUtils.isBlank(multipleSelected))
                    return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐选择特征为空");
                clientIndex = clientData.getFileHandleField().indexOf(multipleSelected);
                serverIndex = serverData.getFileHandleField().indexOf(multipleSelected);
            }
            if (clientIndex<0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐发起方特征未查询到");
            if (serverIndex<0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐协作方特征未查询到");
            StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix()).append(taskReq.getDataTask().getTaskIdName()).append("/");
            ModelEntity clientEntity = new ModelEntity(baseSb.toString(), clientIndex);
            ModelEntity serverEntity = new ModelEntity(baseSb.toString(), serverIndex);
            Common.ParamValue clientDataParamValue=Common.ParamValue.newBuilder().setValueString(clientData.getResourceId()).build();
            Common.ParamValue serverDataParamValue=Common.ParamValue.newBuilder().setValueString(serverData.getResourceId()).build();
            Common.ParamValue psiTypeParamValue=Common.ParamValue.newBuilder().setValueInt32(0).build();
            Common.ParamValue syncResultToServerParamValue=Common.ParamValue.newBuilder().setValueInt32(1).build();
            Common.ParamValue psiTagParamValue=Common.ParamValue.newBuilder().setValueInt32(1).build();
            Common.ParamValue clientIndexParamValue=Common.ParamValue.newBuilder().setValueInt32(clientIndex).build();
            Common.ParamValue serverIndexParamValue=Common.ParamValue.newBuilder().setValueInt32(serverIndex).build();
            Common.ParamValue outputFullFilenameParamValue=Common.ParamValue.newBuilder().setValueString(clientEntity.getPsiPath()).build();
            Common.ParamValue serverOutputFullFilnameParamValue=Common.ParamValue.newBuilder().setValueString(serverEntity.getPsiPath()).build();
            Common.Params params=Common.Params.newBuilder()
                    .putParamMap("clientData",clientDataParamValue)
                    .putParamMap("serverData",serverDataParamValue)
                    .putParamMap("psiType",psiTypeParamValue)
                    .putParamMap("psiTag",psiTagParamValue)
                    .putParamMap("clientIndex",clientIndexParamValue)
                    .putParamMap("serverIndex",serverIndexParamValue)
                    .putParamMap("sync_result_to_server",syncResultToServerParamValue)
                    .putParamMap("outputFullFilename",outputFullFilenameParamValue)
                    .putParamMap("server_outputFullFilname",serverOutputFullFilnameParamValue)
                    .build();
            Common.Task task= Common.Task.newBuilder()
                    .setType(Common.TaskType.PSI_TASK)
                    .setParams(params)
                    .setName("testTask")
                    .setLanguage(Common.Language.PROTO)
                    .setCode("import sys;")
                    .setJobId(ByteString.copyFrom(taskReq.getDataTask().getTaskIdName().getBytes(StandardCharsets.UTF_8)))
                    .setTaskId(ByteString.copyFrom(taskReq.getDataTask().getTaskIdName().getBytes(StandardCharsets.UTF_8)))
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
            reply = workGrpcClient.run(o -> o.submitTask(request));
            log.info("grpc结果:"+reply);
            if (!FileUtil.isFileExists(clientEntity.getPsiPath())){
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐PSI无文件信息");
            }
            map = new HashMap<>();
            map.put(clientData.getResourceId(),clientEntity);
            map.put(serverData.getResourceId(),serverEntity);
            return BaseResultEntity.success(map);
        } catch (Exception e) {
            log.info("grpc Exception:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐PSI 异常:"+e.getMessage());
        }

    }
}
