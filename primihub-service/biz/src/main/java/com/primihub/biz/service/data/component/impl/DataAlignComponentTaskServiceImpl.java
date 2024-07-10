package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.ComponentsConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.constant.CommonConstant;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseFunctionHandleEntity;
import com.primihub.biz.entity.base.BaseFunctionHandleEnum;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.PSIEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dto.ModelDerivationDto;
import com.primihub.biz.entity.data.po.DataModelResource;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.entity.data.req.DataComponentValue;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.data.DataResourceService;
import com.primihub.biz.service.data.OtherBusinessesService;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.service.feign.FusionResourceService;
import com.primihub.biz.util.FileUtil;
import com.primihub.sdk.task.TaskHelper;
import com.primihub.sdk.task.param.TaskComponentParam;
import com.primihub.sdk.task.param.TaskPSIParam;
import com.primihub.sdk.task.param.TaskParam;
import com.primihub.sdk.util.FreemarkerTemplate;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据对齐
 */
@Slf4j
@Service("dataAlignComponentTaskServiceImpl")
public class DataAlignComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
    @Autowired
    private ComponentsConfiguration componentsConfiguration;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private DataModelPrRepository dataModelPrRepository;
    @Autowired
    private TaskHelper taskHelper;
    @Autowired
    private FusionResourceService fusionResourceService;

    @Override
    public BaseResultEntity check(DataComponentReq req, ComponentTaskReq taskReq) {
        return componentTypeVerification(req, componentsConfiguration.getModelComponents(), taskReq);
    }


    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        // 调整 数据对齐的顺序 先判断有没有新的数据集，然后再进行数据对齐操作
        BaseResultEntity baseResultEntity = runPsi(req, taskReq);
        if (baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())) {
            runAssemblyData((Map<String, ModelEntity>) baseResultEntity.getResult(), req, taskReq);
        } else {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(baseResultEntity.getMsg());
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity runAssemblyData(Map<String, ModelEntity> map, DataComponentReq req, ComponentTaskReq taskReq) {
        Map<String, Object> dataAlignFreemarkerMap = new HashMap<>();
        dataAlignFreemarkerMap.put(DataConstant.PYTHON_LABEL_DATASET, taskReq.getFreemarkerMap().get(DataConstant.PYTHON_LABEL_DATASET));
        dataAlignFreemarkerMap.put(DataConstant.PYTHON_GUEST_DATASET, taskReq.getFreemarkerMap().get(DataConstant.PYTHON_GUEST_DATASET));
        dataAlignFreemarkerMap.put("detail", map);
        log.info("dataAlignFreemarkerMap - 1: \n{}", JSONObject.toJSONString(dataAlignFreemarkerMap));
        String dataAlignFreemarkerContent = FreemarkerTemplate.getInstance().generateTemplateStr(dataAlignFreemarkerMap, DataConstant.FREEMARKER_PYTHON_DATA_ALIGN_PATH);
        log.info("dataAlign param after template render: \n{}", dataAlignFreemarkerContent);
        if (dataAlignFreemarkerContent != null) {
            try {
                TaskParam<TaskComponentParam> taskParam = new TaskParam<>(new TaskComponentParam());
                taskParam.setTaskId(taskReq.getDataTask().getTaskIdName());
                taskParam.setJobId(String.valueOf(taskReq.getJob()));
                taskParam.getTaskContentParam().setTemplatesContent(dataAlignFreemarkerContent);
                taskParam.getTaskContentParam().setUntreated(false);
                taskParam.getTaskContentParam().setFreemarkerMap(dataAlignFreemarkerMap);
                taskHelper.submit(taskParam);
                if (taskParam.getSuccess()) {
                    // 处理生成的衍生数据源和最新数据源
                    List<ModelDerivationDto> derivationList = new ArrayList<>();
                    // map中是PSI使用的数据源
                    for (Map.Entry<String, ModelEntity> next : map.entrySet()) {
                        String resourceId = next.getKey();
                        ModelEntity value = next.getValue();

                        String originResourceId = null;
                        if (CollectionUtils.isNotEmpty(taskReq.getNewest())) {
                            originResourceId = taskReq.getOriginResourceIdMap().get(resourceId);
                        } else {
                            originResourceId = resourceId;
                        }
                        derivationList.add(new ModelDerivationDto(
                                resourceId,
                                ModelDerivationDto.ModelDerivationTypeEnum.DATA_ALIGN.getCode(),
                                ModelDerivationDto.ModelDerivationTypeEnum.DATA_ALIGN.getDesc(),
                                value.getNewDataSetId(),
                                value.getOutputPath(),
                                originResourceId
                        ));
                        taskReq.getOriginResourceIdMap().put(value.getNewDataSetId(), originResourceId);
                    }
                    taskReq.getDerivationList().addAll(derivationList);
                    taskReq.setNewest(derivationList);

                    // 替换 freemarker 中的发起方，协作方资源
                    Map<String, String> derivationResourceIdMap = derivationList.stream().collect(Collectors.toMap(
                            ModelDerivationDto::getResourceId, ModelDerivationDto::getNewResourceId
                    ));
                    String labelDatasetId = (String) taskReq.getFreemarkerMap().get(DataConstant.PYTHON_LABEL_DATASET);
                    String guestDatasetId = (String) taskReq.getFreemarkerMap().get(DataConstant.PYTHON_GUEST_DATASET);
                    if (derivationResourceIdMap.containsKey(labelDatasetId)) {
                        taskReq.getFreemarkerMap().put(DataConstant.PYTHON_LABEL_DATASET, derivationResourceIdMap.get(labelDatasetId));
                    }
                    if (derivationResourceIdMap.containsKey(guestDatasetId)) {
                        taskReq.getFreemarkerMap().put(DataConstant.PYTHON_GUEST_DATASET, derivationResourceIdMap.get(guestDatasetId));
                    }

                    // derivation resource data
                    log.info("after dataAlign taskReq derivationList: \n{}", JSONObject.toJSONString(taskReq.getDerivationList()));
                    BaseResultEntity derivationResource = dataResourceService.saveDerivationResource(derivationList, taskReq.getDataTask().getTaskUserId());
                    log.info("dataAlign derivationList save result: \n{}", JSONObject.toJSONString(derivationResource));
                    if (!derivationResource.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())) {
                        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                        taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "组件处理失败:" + derivationResource.getMsg());
                    } else {
                        /*HashSet dids = new HashSet();
                        dids.add(derivationResourceIdMap.get(labelDatasetId));
                        dids.add(derivationResourceIdMap.get(guestDatasetId));
                        while (true){
                            // 休眠一秒等待数据集同步
                            BaseResultEntity dataSets = fusionResourceService.getDataSets(dids);
                            log.info(JSONObject.toJSONString(dataSets));
                            if (dataSets.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
                                List<Object> objectList = (List<Object>) dataSets.getResult();
                                if (objectList.size() == dids.size()){
                                    break;
                                }
                            }
                            Thread.sleep(100L);
                        }*/

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
                } else {
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "组件处理失败:" + taskParam.getError());
                }
            } catch (Exception e) {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "处理组件:" + e.getMessage());
                log.info("grpc Exception:{}", e.getMessage());
                e.printStackTrace();
            }
        } else {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "处理组件:模板异常");
        }
        return BaseResultEntity.success();
    }

    @Data
    public static class ModelEntity {
        public ModelEntity(String psiPath, List<Integer> index, String resourceId) {
            this.newDataSetId = resourceId.substring(0, 12) + "-" + UUID.randomUUID().toString();
            this.psiPath = psiPath + newDataSetId + ".csv";
            this.index = index;
            this.outputPath = psiPath + UUID.randomUUID().toString() + ".csv";
        }

        private String newDataSetId;
        private String psiPath;
        private String outputPath;
        private List<Integer> index;
    }

    /*public BaseResultEntity runPsi(DataComponentReq req, ComponentTaskReq taskReq) {
        List<ModelProjectResourceVo> resourceList = taskReq.getResourceList();
        List<ModelDerivationDto> newest = taskReq.getNewest();
        if (newest != null && !newest.isEmpty()) {
            List<String> ids = new ArrayList<>();
            for (ModelDerivationDto modelDerivationDto : newest) {
                ids.add(modelDerivationDto.getNewResourceId());
            }
            log.info("newIds: {}", ids);
            resourceList = resourceList.stream().filter(modelProjectResourceVo -> ids.contains(modelProjectResourceVo.getResourceId())).collect(Collectors.toList());
        }

        Map<Integer, List<ModelProjectResourceVo>> resourceMap = resourceList
                .stream().collect(Collectors.groupingBy(ModelProjectResourceVo::getParticipationIdentity));
        Map<String, ModelEntity> map = null;
        try {
            Map<String, String> fusionResourceMap = taskReq.getFusionResourceList()
                    .stream().collect(Collectors.toMap(fmap -> fmap.get("resourceId").toString(), fmap -> fmap.getOrDefault("resourceColumnNameList", "").toString()));
            List<ModelProjectResourceVo> projectResource = resourceMap.get(ModelProjectPartyRoleEnum.HOST.getCode());
            if (projectResource == null || projectResource.size() == 0) {
                log.error("{} --- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到发起方资源");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到发起方资源");
            }
            if (projectResource.size() != 1) {
                log.error("{} --- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到发起方资源个数只能是一个");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到发起方资源个数只能是一个");
            }
            ModelProjectResourceVo clientData = projectResource.get(0);
            if (fusionResourceMap.containsKey(clientData.getResourceId()) && StringUtils.isNotBlank(fusionResourceMap.get(clientData.getResourceId()))) {
                clientData.setFileHandleField(Arrays.asList(fusionResourceMap.get(clientData.getResourceId()).split(",")));
            }
            projectResource = resourceMap.get((ModelProjectPartyRoleEnum.GUEST.getCode()));
            if (projectResource == null || projectResource.size() == 0) {
                log.error("{} --- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到协作方资源");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到协作方资源");
            }
            if (projectResource.size() != 1) {
                log.error("{} --- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到协作方资源个数只能是一个");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到协作方资源个数只能是一个");
            }
            ModelProjectResourceVo serverData = projectResource.get(0);
            if (fusionResourceMap.containsKey(serverData.getResourceId()) && StringUtils.isNotBlank(fusionResourceMap.get(serverData.getResourceId()))) {
                serverData.setFileHandleField(Arrays.asList(fusionResourceMap.get(serverData.getResourceId()).split(",")));
            }
            Map<String, String> componentVals = getComponentVals(req.getComponentValues());
            String dataAlign = componentVals.get(DataComponentValue.DataComponetValueKeyEnum.DATA_ALIGN.getCode());
            if (StringUtils.isBlank(dataAlign)) {
                log.error("{} ---- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐选择为空");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐选择为空");
            }
            List<Integer> clientIndex;
            List<Integer> serverIndex;
            clientData.setFileHandleField(clientData.getFileHandleField().stream().map(String::toLowerCase).collect(Collectors.toList()));
            serverData.setFileHandleField(serverData.getFileHandleField().stream().map(String::toLowerCase).collect(Collectors.toList()));
            List<String> fieldList = null;
            // 1是以id单列任务特征
            if ("1".equals(dataAlign)) {
                if (clientData.getFileHandleField().contains("id") && serverData.getFileHandleField().contains("id")) {
                    fieldList = Arrays.stream(new String[]{"id"}).collect(Collectors.toList());
                } else {
                    log.error("{} ---- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐特征中无ID/id属性");
                    return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐特征中无ID/id属性");
                }
            } else {
                String multipleSelected = componentVals.get(DataComponentValue.DataComponetValueKeyEnum.MULTIPLE_SELECTED.getCode());
                if (StringUtils.isBlank(multipleSelected)) {
                    log.error("{} ---- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐选择特征为空");
                    return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐选择特征为空");
                }
                fieldList = JSONArray.parseArray(multipleSelected, String.class);
            }
            log.info("data-align clientDataFileHandleField: \n{}", JSONObject.toJSONString(clientData.getFileHandleField()));
            log.info("data-align serverDataFileHandleField: \n{}", JSONObject.toJSONString(serverData.getFileHandleField()));
            log.info("data-align fieldList : \n{}", JSONObject.toJSONString(fieldList));
            clientIndex = fieldList.stream().map(clientData.getFileHandleField()::indexOf).collect(Collectors.toList());
            serverIndex = fieldList.stream().map(serverData.getFileHandleField()::indexOf).collect(Collectors.toList());
            if (clientIndex.stream().anyMatch(integer -> integer < 0)) {
                log.error("{} ---- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐发起方特征未查询到");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐发起方特征未查询到");
            }
            if (serverIndex.stream().anyMatch(integer -> integer < 0)) {
                log.error("{} ---- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐协作方特征未查询到");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐协作方特征未查询到");
            }
            StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix()).append(taskReq.getDataTask().getTaskIdName()).append("/");
            ModelEntity clientEntity = new ModelEntity(baseSb.toString(), clientIndex, clientData.getResourceId());
            ModelEntity serverEntity = new ModelEntity(baseSb.toString(), serverIndex, serverData.getResourceId());
            TaskParam<TaskPSIParam> taskParam = new TaskParam<>(new TaskPSIParam());
            taskParam.setTaskId(taskReq.getDataTask().getTaskIdName());
            taskParam.setJobId(String.valueOf(taskReq.getJob()));
            taskParam.getTaskContentParam().setServerData(serverData.getResourceId());
            taskParam.getTaskContentParam().setServerIndex(serverIndex.toArray(new Integer[]{}));
            taskParam.getTaskContentParam().setClientData(clientData.getResourceId());
            taskParam.getTaskContentParam().setClientIndex(clientIndex.toArray(new Integer[]{}));
            taskParam.getTaskContentParam().setPsiTag(PSIEnum.TAG_KKRT.getCode());
            taskParam.getTaskContentParam().setSyncResultToServer(PSIEnum.SYNC_ON.getCode());
            taskParam.getTaskContentParam().setOutputFullFilename(clientEntity.getPsiPath());
            taskParam.getTaskContentParam().setServerOutputFullFilname(serverEntity.getPsiPath());
            taskHelper.submit(taskParam);
            if (!FileUtil.isFileExists(clientEntity.getPsiPath())) {
                log.error("{} ---- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐PSI无文件信息");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐PSI无文件信息");
            }
            map = new HashMap<>();
            map.put(clientData.getResourceId(), clientEntity);
            map.put(serverData.getResourceId(), serverEntity);
            return BaseResultEntity.success(map);
        } catch (Exception e) {
            e.printStackTrace();
            log.info("数据对齐PSI 异常 grpc Exception: {}", e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐PSI 异常:" + e.getMessage());
        }

    }*/

    public BaseResultEntity runPsi(DataComponentReq req, ComponentTaskReq taskReq) {
        Map<String, String> fusionResourceMap = taskReq.getFusionResourceList()
                .stream().collect(Collectors.toMap(fMap -> fMap.get("resourceId").toString(), fMap -> fMap.getOrDefault("resourceColumnNameList", "").toString()));

        String labelResourceId = (String) taskReq.getFreemarkerMap().getOrDefault(DataConstant.PYTHON_LABEL_DATASET, StringUtils.EMPTY);
        String guestResourceId = (String) taskReq.getFreemarkerMap().getOrDefault(DataConstant.PYTHON_GUEST_DATASET, StringUtils.EMPTY);
        if (StringUtils.isBlank(labelResourceId)) {
            log.error("{} --- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到发起方资源");
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到发起方资源");
        }
        if (StringUtils.isBlank(guestResourceId)) {
            log.error("{} --- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到协作方资源");
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到协作方资源");
        }

        String originLabelResourceId = null;
        String originGuestResourceId = null;
        // 从 IdMap 中取出 originId
        if (CollectionUtils.isNotEmpty(taskReq.getNewest())) {
            originLabelResourceId = taskReq.getOriginResourceIdMap().get(labelResourceId);
            originGuestResourceId = taskReq.getOriginResourceIdMap().get(guestResourceId);
        } else {
            originLabelResourceId = labelResourceId;
            originGuestResourceId = guestResourceId;
        }

        log.info("[模型任务][数据对齐] 找到衍生资源的原始资源 [originLabelResourceId: {}, originGuestResourceId: {}]",
                originLabelResourceId, originGuestResourceId);
        String clientDataFileFieldStr = fusionResourceMap.get(originLabelResourceId);
        String guestDataFileFieldStr = fusionResourceMap.get(originGuestResourceId);

        if (StringUtils.isBlank(clientDataFileFieldStr)) {
            log.error("{} --- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到发起方资源");
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到发起方资源");
        }
        if (StringUtils.isBlank(guestDataFileFieldStr)) {
            log.error("{} --- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到协作方资源");
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐查询不到协作方资源");
        }
        Map<String, ModelEntity> map = null;
        try {
            String clientLowerCase = clientDataFileFieldStr.toLowerCase();
            List<String> clientDataFileField = Arrays.stream(clientLowerCase.split(CommonConstant.COMMA_SEPARATOR)).map(String::toLowerCase).collect(Collectors.toList());
            log.info("data-align clientDataFileHandleField: \n{}", JSONObject.toJSONString(clientDataFileField));

            String guestLowerCase = guestDataFileFieldStr.toLowerCase();
            List<String> guestDataFileField = Arrays.stream(guestLowerCase.split(CommonConstant.COMMA_SEPARATOR)).map(String::toLowerCase).collect(Collectors.toList());
            log.info("data-align serverDataFileHandleField: \n{}", JSONObject.toJSONString(guestDataFileField));

            Map<String, String> componentVals = getComponentVals(req.getComponentValues());
            String guestIndexField = componentVals.get("clientIndex");
            log.info("data-align guestIndexField: \n{}", guestIndexField);
            String clientIndexField = componentVals.get("serverIndex");
            log.info("data-align clientIndexField: \n{}", clientIndexField);

            /*String dataAlign = componentVals.get(DataComponentValue.DataComponetValueKeyEnum.DATA_ALIGN.getCode());
            if (StringUtils.isBlank(dataAlign)) {
                log.error("{} ---- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐选择为空");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐选择为空");
            }*/
            List<Integer> clientIndex;
            List<Integer> serverIndex;
            //List<String> fieldList = null;
            // 1是以id单列任务特征
            /*if ("1".equals(dataAlign)) {
                if (clientDataFileField.contains("id") && guestDataFileField.contains("id")) {
                    fieldList = Arrays.stream(new String[]{"id"}).collect(Collectors.toList());
                } else {
                    log.error("{} ---- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐特征中无ID/id属性");
                    return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐特征中无ID/id属性");
                }
            } else {
                String multipleSelected = componentVals.get(DataComponentValue.DataComponetValueKeyEnum.MULTIPLE_SELECTED.getCode());
                if (StringUtils.isBlank(multipleSelected)) {
                    log.error("{} ---- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐选择特征为空");
                    return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐选择特征为空");
                }
                fieldList = JSONArray.parseArray(multipleSelected, String.class);
            }*/
            //log.info("data-align fieldList : \n{}", JSONObject.toJSONString(fieldList));
            String clientFieldLowerCase = clientIndexField.toLowerCase();
            String guestFieldLowerCase = guestIndexField.toLowerCase();
            clientIndex = clientDataFileField.stream().filter(clientField ->clientField.equals(clientFieldLowerCase)).map(clientDataFileField::indexOf).collect(Collectors.toList());
            serverIndex = guestDataFileField.stream().filter(clientField ->clientField.equals(guestFieldLowerCase)).map(guestDataFileField::indexOf).collect(Collectors.toList());
            //clientIndex = fieldList.stream().map(clientDataFileField::indexOf).collect(Collectors.toList());
            //serverIndex = fieldList.stream().map(guestDataFileField::indexOf).collect(Collectors.toList());
            if (clientIndex.stream().anyMatch(integer -> integer < 0)) {
                log.error("{} ---- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐发起方特征未查询到");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐发起方特征未查询到");
            }
            if (serverIndex.stream().anyMatch(integer -> integer < 0)) {
                log.error("{} ---- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐协作方特征未查询到");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐协作方特征未查询到");
            }
            StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix())
                    .append(taskReq.getDataTask().getTaskIdName()).append("/");
            ModelEntity clientEntity = new ModelEntity(baseSb.toString(), clientIndex, labelResourceId);
            ModelEntity serverEntity = new ModelEntity(baseSb.toString(), serverIndex, guestResourceId);
            TaskParam<TaskPSIParam> taskParam = new TaskParam<>(new TaskPSIParam());
            taskParam.setTaskId(taskReq.getDataTask().getTaskIdName());
            taskParam.setJobId(String.valueOf(taskReq.getJob()));
            taskParam.getTaskContentParam().setServerData(guestResourceId);
            taskParam.getTaskContentParam().setServerIndex(serverIndex.toArray(new Integer[]{}));
            taskParam.getTaskContentParam().setClientData(labelResourceId);
            taskParam.getTaskContentParam().setClientIndex(clientIndex.toArray(new Integer[]{}));
            taskParam.getTaskContentParam().setPsiTag(PSIEnum.TAG_KKRT.getCode());
            taskParam.getTaskContentParam().setSyncResultToServer(PSIEnum.SYNC_ON.getCode());
            taskParam.getTaskContentParam().setOutputFullFilename(clientEntity.getPsiPath());
            taskParam.getTaskContentParam().setServerOutputFullFilname(serverEntity.getPsiPath());
            taskHelper.submit(taskParam);
            if (!FileUtil.isFileExists(clientEntity.getPsiPath())) {
                log.error("{} ---- {}", BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐PSI无文件信息");
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐PSI无文件信息");
            }
            map = new HashMap<>();
            map.put(labelResourceId, clientEntity);
            map.put(guestResourceId, serverEntity);
            return BaseResultEntity.success(map);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("[模型任务][数据对齐] PSI异常 [ grpc Exception: \n{}]", e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "数据对齐PSI 异常:" + e.getMessage());
        }

    }
}
