package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.ComponentsConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dto.ModelDerivationDto;
import com.primihub.biz.entity.data.po.DataModelResource;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.entity.data.vo.ModelProjectResourceVo;
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import com.primihub.sdk.task.TaskHelper;
import com.primihub.sdk.task.param.TaskComponentParam;
import com.primihub.sdk.task.param.TaskPSIParam;
import com.primihub.sdk.task.param.TaskParam;
import com.primihub.sdk.util.FreemarkerTemplate;
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
    private ComponentsConfiguration componentsConfiguration;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private DataModelPrRepository dataModelPrRepository;
    @Autowired
    private TaskHelper taskHelper;

    @Override
    public BaseResultEntity check(DataComponentReq req, ComponentTaskReq taskReq) {
        return componentTypeVerification(req, componentsConfiguration.getModelComponents(), taskReq);
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
        Map<String, Object> freemarkerMap = new HashMap<>();
        freemarkerMap.put(DataConstant.PYTHON_LABEL_DATASET,taskReq.getFreemarkerMap().get(DataConstant.PYTHON_LABEL_DATASET));
        freemarkerMap.put(DataConstant.PYTHON_GUEST_DATASET,taskReq.getFreemarkerMap().get(DataConstant.PYTHON_GUEST_DATASET));
        freemarkerMap.put("detail",map);
        String freemarkerContent = FreemarkerTemplate.getInstance().generateTemplateStr(freemarkerMap,DataConstant.FREEMARKER_PYTHON_DATA_ALIGN_PATH);
        log.info(freemarkerContent);
        if (freemarkerContent != null) {
            try {
                TaskParam<TaskComponentParam> taskParam = new TaskParam<>(new TaskComponentParam());
                taskParam.setTaskId(taskReq.getDataTask().getTaskIdName());
                taskParam.setJobId(String.valueOf(taskReq.getJob()));
                taskParam.getTaskContentParam().setTemplatesContent(freemarkerContent);
                taskParam.getTaskContentParam().setUntreated(false);
                taskParam.getTaskContentParam().setFreemarkerMap(freemarkerMap);
                taskHelper.submit(taskParam);
                if (taskParam.getSuccess()){
                    List<ModelDerivationDto> derivationList = new ArrayList<>();
                    Iterator<Map.Entry<String, ModelEntity>> iterator = map.entrySet().iterator();
                    Map<String, String> dtoMap = taskReq.getNewest()!=null && taskReq.getNewest().size()!=0?taskReq.getNewest().stream().collect(Collectors.toMap(ModelDerivationDto::getResourceId,ModelDerivationDto::getOriginalResourceId)):null;
                    while (iterator.hasNext()) {
                        Map.Entry<String, ModelEntity> next = iterator.next();
                        String key = next.getKey();
                        ModelEntity value = next.getValue();
                        if (dtoMap!=null && dtoMap.containsKey(key)){
                            derivationList.add(new ModelDerivationDto(key, null, req.getComponentName(), value.getNewDataSetId(),value.getOutputPath(),dtoMap.get(key)));
                        }else {
                            derivationList.add(new ModelDerivationDto(key, null, req.getComponentName(), value.getNewDataSetId(),value.getOutputPath(),key));
                        }
                    }
                    taskReq.getDerivationList().addAll(derivationList);
                    taskReq.setNewest(derivationList);
                    // derivation resource datas
                    log.info(JSONObject.toJSONString(taskReq.getDerivationList()));
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
                            dataModelResource.setTakePartType(1);
                            dataModelPrRepository.saveDataModelResource(dataModelResource);
                            taskReq.getDmrList().add(dataModelResource);
                        }
                    }
                }else {
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"组件处理失败:"+taskParam.getError());
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
    public static class ModelEntity {
        public ModelEntity(String psiPath, List<Integer> index,String resourceId) {
            this.newDataSetId= resourceId.substring(0, 12) +"-"+ UUID.randomUUID().toString();
            this.psiPath = psiPath + newDataSetId +".csv";
            this.index = index;
            this.outputPath = psiPath + UUID.randomUUID().toString() +".csv";
        }

        private String newDataSetId;
        private String psiPath;
        private String outputPath;
        private List<Integer> index;
    }

    public BaseResultEntity runPsi(DataComponentReq req,ComponentTaskReq taskReq){
        List<ModelProjectResourceVo> resourceList = taskReq.getResourceList();
        Map<Integer, List<ModelProjectResourceVo>> resourceMap = resourceList.stream().collect(Collectors.groupingBy(ModelProjectResourceVo::getParticipationIdentity));
        PushTaskReply reply = null;
        Map<String, ModelEntity> map = null;
        try {
            Map<String, String> fusionResourceMap = taskReq.getFusionResourceList().stream().collect(Collectors.toMap(fmap -> fmap.get("resourceId").toString(), fmap -> fmap.getOrDefault("resourceColumnNameList", "").toString()));
            List<ModelProjectResourceVo> projectResource = resourceMap.get(1);
            if (projectResource==null || projectResource.size()==0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐查询不到发起方资源");
            }
            ModelProjectResourceVo clientData = projectResource.get(0);
            if (fusionResourceMap.containsKey(clientData.getResourceId()) && StringUtils.isNotBlank(fusionResourceMap.get(clientData.getResourceId()))){
                clientData.setFileHandleField(Arrays.asList(fusionResourceMap.get(clientData.getResourceId()).split(",")));
            }
            projectResource = resourceMap.get(2);
            if (projectResource==null || projectResource.size()==0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐查询不到发起方资源");
            }
            ModelProjectResourceVo serverData = projectResource.get(0);
            if (fusionResourceMap.containsKey(serverData.getResourceId()) && StringUtils.isNotBlank(fusionResourceMap.get(serverData.getResourceId()))){
                serverData.setFileHandleField(Arrays.asList(fusionResourceMap.get(serverData.getResourceId()).split(",")));
            }
            Map<String, String> componentVals = getComponentVals(req.getComponentValues());
            String dataAlign = componentVals.get("dataAlign");
            if (StringUtils.isBlank(dataAlign)) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐选择为空");
            }
            List<Integer> clientIndex;
            List<Integer> serverIndex;
            clientData.setFileHandleField(clientData.getFileHandleField().stream().map(String::toLowerCase).collect(Collectors.toList()));
            serverData.setFileHandleField(serverData.getFileHandleField().stream().map(String::toLowerCase).collect(Collectors.toList()));
            List<String> fieldList = null;
            if ("1".equals(dataAlign)){
                if (clientData.getFileHandleField().contains("id")&&serverData.getFileHandleField().contains("id")){
                    fieldList = Arrays.stream(new String[]{"id"}).collect(Collectors.toList());
                }else {
                    return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐特征中无ID/id属性");
                }
            }else {
                String multipleSelected = componentVals.get("MultipleSelected");
                if (StringUtils.isBlank(multipleSelected)) {
                    return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐选择特征为空");
                }
//                fieldList = Arrays.stream(multipleSelected.split(",")).collect(Collectors.toList());
                fieldList = JSONArray.parseArray(multipleSelected,String.class);
            }
            log.info("data-align clientDataFileHandleField: {}",JSONObject.toJSONString(clientData.getFileHandleField()));
            log.info("data-align serverDataFileHandleField: {}",JSONObject.toJSONString(serverData.getFileHandleField()));
            log.info("data-align fieldList : {}",JSONObject.toJSONString(fieldList));
            clientIndex = fieldList.stream().map(clientData.getFileHandleField()::indexOf).collect(Collectors.toList());
            serverIndex = fieldList.stream().map(serverData.getFileHandleField()::indexOf).collect(Collectors.toList());
            if (clientIndex.size()<0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐发起方特征未查询到");
            }
            if (serverIndex.size()<0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐协作方特征未查询到");
            }
            StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix()).append(taskReq.getDataTask().getTaskIdName()).append("/");
            ModelEntity clientEntity = new ModelEntity(baseSb.toString(), clientIndex,clientData.getResourceId());
            ModelEntity serverEntity = new ModelEntity(baseSb.toString(), serverIndex,serverData.getResourceId());
            TaskParam<TaskPSIParam> taskParam = new TaskParam<>(new TaskPSIParam());
            taskParam.setTaskId(taskReq.getDataTask().getTaskIdName());
            taskParam.setJobId(String.valueOf(taskReq.getJob()));
            taskParam.getTaskContentParam().setServerData(serverData.getResourceId());
            taskParam.getTaskContentParam().setServerIndex(serverIndex.toArray(new Integer[]{}));
            taskParam.getTaskContentParam().setClientData(clientData.getResourceId());
            taskParam.getTaskContentParam().setClientIndex(clientIndex.toArray(new Integer[]{}));
            taskParam.getTaskContentParam().setPsiTag(1);
            taskParam.getTaskContentParam().setSyncResultToServer(1);
            taskParam.getTaskContentParam().setOutputFullFilename(clientEntity.getPsiPath());
            taskParam.getTaskContentParam().setServerOutputFullFilname(serverEntity.getPsiPath());
            taskHelper.submit(taskParam);
            if (!FileUtil.isFileExists(clientEntity.getPsiPath())){
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐PSI无文件信息");
            }
            map = new HashMap<>();
            map.put(clientData.getResourceId(),clientEntity);
            map.put(serverData.getResourceId(),serverEntity);
            return BaseResultEntity.success(map);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("grpc Exception:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"数据对齐PSI 异常:"+e.getMessage());
        }

    }
}
