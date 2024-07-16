package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.ComponentsConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dto.ModelDerivationDto;
import com.primihub.biz.entity.data.dto.ModelOutputPathDto;
import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectOrgan;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.entity.data.req.DataFResourceReq;
import com.primihub.biz.entity.data.vo.ModelProjectResourceVo;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.service.data.OtherBusinessesService;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.util.FileUtil;
import com.primihub.sdk.task.TaskHelper;
import com.primihub.sdk.task.dataenum.ModelTypeEnum;
import com.primihub.sdk.task.param.TaskComponentParam;
import com.primihub.sdk.task.param.TaskMPCParam;
import com.primihub.sdk.task.param.TaskParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 模型组件任务
 */
@Service("modelComponentTaskServiceImpl")
@Slf4j
public class ModelComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
    @Autowired
    private ComponentsConfiguration componentsConfiguration;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private DataProjectRepository dataProjectRepository;
    @Autowired
    private TaskHelper taskHelper;

    @Override
    public BaseResultEntity check(DataComponentReq req, ComponentTaskReq taskReq) {
        BaseResultEntity baseResultEntity = componentTypeVerification(req, componentsConfiguration.getModelComponents(), taskReq);
        if (baseResultEntity.getCode() != 0) {
            return baseResultEntity;
        }
        ModelTypeEnum modelType = ModelTypeEnum.MODEL_TYPE_MAP.get(Integer.valueOf(taskReq.getValueMap().get("modelType")));
        taskReq.getDataModel().setTrainType(modelType.getTrainType());
        taskReq.getDataModel().setModelType(modelType.getType());
        if (taskReq.getValueMap().containsKey("arbiterOrgan")) {
            String arbiterOrgan = taskReq.getValueMap().get("arbiterOrgan");
            log.info(arbiterOrgan);
            if (StringUtils.isBlank(arbiterOrgan)) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "可信第三方选择不可以为空");
            }
            DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(taskReq.getDataModel().getProjectId(), null);
            List<DataProjectOrgan> dataProjectOrgans = dataProjectRepository.selectDataProjectOrganByProjectId(dataProject.getProjectId());
            if (dataProjectOrgans.size() <= 2) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "项目参与方少于3方");
            }
            List<ModelProjectResourceVo> resourceLists = JSONObject.parseArray(taskReq.getValueMap().get("selectData"), ModelProjectResourceVo.class);
            Set<String> organIdSet = resourceLists.stream().map(ModelProjectResourceVo::getOrganId).collect(Collectors.toSet());
            log.info(organIdSet.toString());
            if (organIdSet.contains(arbiterOrgan)) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "可信第三方不可以和数据集机构重复");
            }
            DataFResourceReq fresourceReq = new DataFResourceReq();
            fresourceReq.setOrganId(arbiterOrgan);
            BaseResultEntity resourceList = otherBusinessesService.getResourceList(fresourceReq);
            if (resourceList.getCode() != 0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "可信第三方检索失败-代码1");
            }
            LinkedHashMap<String, Object> data = (LinkedHashMap<String, Object>) resourceList.getResult();
            List<LinkedHashMap<String, Object>> resourceDataList = (List<LinkedHashMap<String, Object>>) data.get("data");
            if (resourceDataList == null || resourceDataList.size() == 0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "可信第三方检索失败-代码2");
            }
            taskReq.getFreemarkerMap().put(DataConstant.PYTHON_ARBITER_DATASET, resourceDataList.get(0).get("resourceId").toString());
            taskReq.getFusionResourceList().add(resourceDataList.get(0));
        }
        return baseResultEntity;
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        Integer modelType = Integer.valueOf(taskReq.getValueMap().get("modelType"));
        log.info("[模型任务][模型组件] --- [ modelType: {} ]", modelType);
        ModelTypeEnum modelTypeEnum = ModelTypeEnum.MODEL_TYPE_MAP.get(modelType);
        if (modelTypeEnum == null) {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg("运行失败:无法进行任务执行-任务类型未匹配");
            log.error("[模型任务][模型组件] 运行失败:无法进行任务执行-任务类型未匹配 ");
            return BaseResultEntity.success();
        }
        // MPC_LR 任务
        if (modelTypeEnum == ModelTypeEnum.MPC_LR) {
            String selectData = taskReq.getValueMap().get("selectData");
            if(StringUtils.isNotBlank(selectData)){
                List<ModelProjectResourceVo> modelProjectResourceVos = JSONObject.parseArray(selectData, ModelProjectResourceVo.class);
                if (modelProjectResourceVos.size() != 3){
                    log.error("[模型任务][模型组件] 运行失败:参与数据集不正确 ");
                    return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL, "需要三方数据集参与");
                }
            }
            return mpclr(req, taskReq);
        }

        taskReq.getFreemarkerMap().putAll(getComponentVals(req.getComponentValues()));
        // 将旧的资源id替换成新的衍生资源的id，这个已经在生成的时候完成
        /*if (taskReq.getNewest() != null && !taskReq.getNewest().isEmpty()) {
            log.info("newest: \n{}", JSONObject.toJSONString(taskReq.getNewest()));
            log.info("freemarkerMap1: \n{}", JSONObject.toJSONString(taskReq.getFreemarkerMap()));
            // 源resourceId map
            Map<String, ModelDerivationDto> derivationMap = taskReq.getNewest().stream().collect(Collectors.toMap(ModelDerivationDto::getResourceId, Function.identity()));
            for (Map.Entry<String, Object> entry : taskReq.getFreemarkerMap().entrySet()) {
                if (derivationMap.containsKey(entry.getValue())) {
                    String newDataSetId = derivationMap.get(entry.getValue()).getNewResourceId();
                    taskReq.getFreemarkerMap().put(entry.getKey(), newDataSetId);
                }
            }
            log.info("freemarkerMap2: \n{}", JSONObject.toJSONString(taskReq.getFreemarkerMap()));
        }*/
        taskReq.getFreemarkerMap().put("feature_names", "null");
        if (StringUtils.isNotBlank((String) taskReq.getFreemarkerMap().get(DataConstant.PYTHON_CALCULATION_FIELD))) {
            String field = (String) taskReq.getFreemarkerMap().get(DataConstant.PYTHON_CALCULATION_FIELD);
            log.info("field: {}", field);
            taskReq.getFreemarkerMap().put("feature_names", field);
        }
        return runTaskModel(req, taskReq, modelTypeEnum);
    }

    private BaseResultEntity runTaskModel(DataComponentReq req, ComponentTaskReq taskReq, ModelTypeEnum modelType) {
        log.info("------- 执行任务, modelType:{}", modelType);
        StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix()).append(taskReq.getDataTask().getTaskIdName());
        ModelOutputPathDto outputPathDto = new ModelOutputPathDto(baseSb.toString());
        putPath(outputPathDto, taskReq);
        taskReq.getDataTask().setTaskResultContent(JSONObject.toJSONString(outputPathDto));
        taskReq.getDataModelTask().setPredictFile(outputPathDto.getIndicatorFileName());
        TaskParam<TaskComponentParam> taskParam = new TaskParam(new TaskComponentParam());
        taskParam.setTaskId(taskReq.getDataTask().getTaskIdName());
        taskParam.setJobId(String.valueOf(taskReq.getJob()));
        taskParam.getTaskContentParam().setModelType(modelType);
        taskParam.getTaskContentParam().setFreemarkerMap(taskReq.getFreemarkerMap());
        taskHelper.submit(taskParam);
        if (taskParam.getSuccess()) {
            taskReq.getDataTask().setTaskState(TaskStateEnum.SUCCESS.getStateType());
            File sourceFile = new File(baseSb.toString());
            if (sourceFile.isDirectory()) {
                File[] files = sourceFile.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().contains("modelFileName")) {
                            taskReq.getDataModelTask().setPredictContent(FileUtil.getFileContent(taskReq.getDataModelTask().getPredictFile()));
                            taskReq.getDataTask().setTaskState(TaskStateEnum.SUCCESS.getStateType());
                            taskReq.getDataTask().setTaskErrorMsg("");
                            break;
                        }
                    }
                } else {
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "运行失败:目录文件夹中无文件");
                }
            } else {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "运行失败:目录文件夹中无文件");
            }
        } else {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "运行失败:" + taskParam.getError());
        }
        return BaseResultEntity.success();
    }

    private BaseResultEntity mpclr(DataComponentReq req, ComponentTaskReq taskReq) {
        try {
            List<String> resourceIds = new ArrayList<>();
            if (taskReq.getNewest() != null && taskReq.getNewest().size() != 0) {
                resourceIds.addAll(taskReq.getNewest().stream().map(ModelDerivationDto::getNewResourceId).collect(Collectors.toSet()));
            } else {
                resourceIds.addAll(taskReq.getResourceList().stream().map(ModelProjectResourceVo::getResourceId).collect(Collectors.toSet()));
            }
            String jobId = String.valueOf(taskReq.getJob());
            StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix()).append(taskReq.getDataTask().getTaskIdName());
            ModelOutputPathDto outputPathDto = new ModelOutputPathDto(baseSb.toString());
            taskReq.getDataTask().setTaskResultContent(JSONObject.toJSONString(outputPathDto));
            int batchSize = 128;
            int numlters = 5;
            List<String> columnsExclude = null;
            try {
                if (taskReq.getValueMap().containsKey("batchSize")) {
                    batchSize = Integer.parseInt(taskReq.getValueMap().get("batchSize"));
                }
                if (taskReq.getValueMap().containsKey("maxIter")) {
                    numlters = Integer.parseInt(taskReq.getValueMap().get("maxIter"));
                }
                if (taskReq.getValueMap().containsKey("ColumnsExclude")) {
                    try {
                        String[] split = StringUtils.split(taskReq.getValueMap().get("ColumnsExclude"), ",");
                        columnsExclude = Arrays.asList(split);
                    }catch (Exception e){
                        log.info("ColumnsExclude convert json exception:{}",e.getMessage());
                        e.printStackTrace();
                    }

                }
            } catch (Exception e) {
                log.info(e.getMessage());
            }
            TaskParam<TaskMPCParam> taskParam = new TaskParam();
            taskParam.setTaskId(taskReq.getDataTask().getTaskIdName());
            taskParam.setJobId(jobId);
            TaskMPCParam mpcParam = new TaskMPCParam();
            mpcParam.setTaskName("logistic_regression");
            mpcParam.setTaskCode("logistic_regression");
            mpcParam.setResourceIds(resourceIds);
            mpcParam.setParamMap(new HashMap<>());
            mpcParam.getParamMap().put("BatchSize", batchSize);
            mpcParam.getParamMap().put("NumIters", numlters);
            mpcParam.getParamMap().put("ColumnsExclude", columnsExclude);
            mpcParam.getParamMap().put("modelName", outputPathDto.getHostModelFileName());
            taskParam.setTaskContentParam(mpcParam);
            taskHelper.submit(taskParam);
            if (taskParam.getSuccess()) {
                taskReq.getDataTask().setTaskState(TaskStateEnum.SUCCESS.getStateType());
                File sourceFile = new File(baseSb.toString());
                if (sourceFile.isDirectory()) {
                    File[] files = sourceFile.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.getName().contains("modelFileName")) {
                                taskReq.getDataTask().setTaskState(TaskStateEnum.SUCCESS.getStateType());
                                taskReq.getDataTask().setTaskErrorMsg("");
                                break;
                            }
                        }
                    } else {
                        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                        taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "运行失败:目录文件夹中无文件");
                    }
                } else {
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "运行失败:目录文件夹中无文件");
                }
            } else {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "运行失败:" + taskParam.getError());
            }
        } catch (Exception e) {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName() + "运行失败:" + e.getMessage());
            log.error("grpc Exception:{}", e.getMessage());
            e.printStackTrace();
        }
        return BaseResultEntity.success();
    }

    public void putPath(ModelOutputPathDto outputPathDto, ComponentTaskReq taskReq) {
        taskReq.getDataModelTask().setPredictFile(outputPathDto.getIndicatorFileName());
        taskReq.getFreemarkerMap().put("predictFileName", outputPathDto.getPredictFileName());
        taskReq.getFreemarkerMap().put("indicatorFileName", outputPathDto.getIndicatorFileName());
        taskReq.getFreemarkerMap().put("hostLookupTable", outputPathDto.getHostLookupTable());
        taskReq.getFreemarkerMap().put("guestLookupTable", outputPathDto.getGuestLookupTable());
        taskReq.getFreemarkerMap().put("hostModelFileName", outputPathDto.getHostModelFileName());
        taskReq.getFreemarkerMap().put("guestModelFileName", outputPathDto.getGuestModelFileName());
    }

}
