package com.primihub.biz.service.data.component.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.constant.RedisKeyConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.ModelTypeEnum;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dto.ModelDerivationDto;
import com.primihub.biz.entity.data.dto.ModelOutputPathDto;
import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectOrgan;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.entity.data.req.DataFResourceReq;
import com.primihub.biz.entity.data.vo.ModelProjectResourceVo;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.service.data.DataTaskMonitorService;
import com.primihub.biz.service.data.OtherBusinessesService;
import com.primihub.biz.service.data.component.ComponentTaskService;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.FreemarkerUtil;
import com.primihub.biz.util.snowflake.SnowflakeId;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import primihub.rpc.Common;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service("modelComponentTaskServiceImpl")
@Slf4j
public class ModelComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private DataProjectRepository dataProjectRepository;
    @Autowired
    private DataTaskMonitorService dataTaskMonitorService;

    @Override
    public BaseResultEntity check(DataComponentReq req,  ComponentTaskReq taskReq) {
        BaseResultEntity baseResultEntity = componentTypeVerification(req, baseConfiguration.getModelComponents(), taskReq);
        if (baseResultEntity.getCode()!=0) {
            return baseResultEntity;
        }
        ModelTypeEnum modelType = ModelTypeEnum.MODEL_TYPE_MAP.get(Integer.valueOf(taskReq.getValueMap().get("modelType")));
        taskReq.getDataModel().setTrainType(modelType.getTrainType());
        taskReq.getDataModel().setModelType(modelType.getType());
        if (modelType.getType().equals(ModelTypeEnum.TRANSVERSE_LR.getType())){
            String arbiterOrgan = taskReq.getValueMap().get("arbiterOrgan");
            log.info(arbiterOrgan);
            if (StringUtils.isBlank(arbiterOrgan)) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"横向LR模型 可信第三方选择不可以为空");
            }
            DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(taskReq.getDataModel().getProjectId(), null);
            List<DataProjectOrgan> dataProjectOrgans = dataProjectRepository.selectDataProjcetOrganByProjectId(dataProject.getProjectId());
            if (dataProjectOrgans.size()<=2) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"项目参与方少于3方");
            }
            List<ModelProjectResourceVo> resourceLists = JSONObject.parseArray(taskReq.getValueMap().get("selectData"), ModelProjectResourceVo.class);
            Set<String> organIdSet = resourceLists.stream().map(ModelProjectResourceVo::getOrganId).collect(Collectors.toSet());
            log.info(organIdSet.toString());
            if (organIdSet.contains(arbiterOrgan)) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"可信第三方不可以和数据集机构重复");
            }
            DataFResourceReq fresourceReq = new DataFResourceReq();
            fresourceReq.setOrganId(arbiterOrgan);
            fresourceReq.setServerAddress(dataProject.getServerAddress());
            BaseResultEntity resourceList = otherBusinessesService.getResourceList(fresourceReq);
            if (resourceList.getCode()!=0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"可信第三方检索失败-代码1");
            }
            LinkedHashMap<String,Object> data = (LinkedHashMap<String,Object>)resourceList.getResult();
            List<LinkedHashMap<String,Object>> resourceDataList = (List<LinkedHashMap<String,Object>>)data.get("data");
            if (resourceDataList==null || resourceDataList.size()==0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"可信第三方检索失败-代码2");
            }
            taskReq.getFreemarkerMap().put(DataConstant.PYTHON_ARBITER_DATASET,resourceDataList.get(0).get("resourceId").toString());
            taskReq.getFusionResourceList().add(resourceDataList.get(0));
        }
        return baseResultEntity;
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        if (Integer.valueOf(taskReq.getValueMap().get("modelType")).equals(ModelTypeEnum.MPC_LR.getType())){
            return mpclr(req,taskReq);
        }
        taskReq.getFreemarkerMap().putAll(getComponentVals(req.getComponentValues()));
        if (taskReq.getNewest()!=null && taskReq.getNewest().size()!=0){
            log.info("newest:{}",JSONObject.toJSONString(taskReq.getNewest()));
            log.info("freemarkerMap1:{}",JSONObject.toJSONString(taskReq.getFreemarkerMap()));
            Map<String, ModelDerivationDto> derivationMap = taskReq.getNewest().stream().collect(Collectors.toMap(ModelDerivationDto::getOriginalResourceId, Function.identity()));
            Iterator<Map.Entry<String, String>> iterator = taskReq.getFreemarkerMap().entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, String> next = iterator.next();
                if (derivationMap.containsKey(next.getValue())){
                    String newDataSetId = derivationMap.get(next.getValue()).getNewResourceId();
                    taskReq.getFreemarkerMap().put(next.getKey(),newDataSetId);
                }
            }
            log.info("freemarkerMap2:{}",JSONObject.toJSONString(taskReq.getFreemarkerMap()));
        }
        taskReq.getFreemarkerMap().put("feature_names","None");
        if (StringUtils.isNotBlank(taskReq.getFreemarkerMap().get(DataConstant.PYTHON_CALCULATION_FIELD))){
            String field = taskReq.getFreemarkerMap().get(DataConstant.PYTHON_CALCULATION_FIELD);
            log.info("field:{}",field);
            taskReq.getFreemarkerMap().put("feature_names",field);
        }
        Integer modelType = Integer.valueOf(taskReq.getValueMap().get("modelType"));
        if (modelType.equals(ModelTypeEnum.V_XGBOOST.getType())){
            return xgb(req,taskReq);
//        }else if (modelType.equals(ModelTypeEnum.TRANSVERSE_LR.getType())||modelType.equals(ModelTypeEnum.HETERO_LR.getType())){
        }else if (modelType.equals(ModelTypeEnum.TRANSVERSE_LR.getType())
                ||modelType.equals(ModelTypeEnum.HETERO_LR.getType())
                ||modelType.equals(ModelTypeEnum.BINARY.getType())
                ||modelType.equals(ModelTypeEnum.BINARY_DPSGD.getType())){
            return lr(req,taskReq,ModelTypeEnum.MODEL_TYPE_MAP.get(modelType));
        }
        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
        taskReq.getDataTask().setTaskErrorMsg("运行失败:无法进行任务执行");
        return BaseResultEntity.success();
    }

    private BaseResultEntity mpclr(DataComponentReq req, ComponentTaskReq taskReq){
        try {
            Set<String> resourceIds = new HashSet<>();
            if (taskReq.getNewest()!=null && taskReq.getNewest().size()!=0){
                resourceIds.addAll(taskReq.getNewest().stream().map(ModelDerivationDto::getNewResourceId).collect(Collectors.toSet()));
            }else {
                resourceIds.addAll(taskReq.getResourceList().stream().map(ModelProjectResourceVo::getResourceId).collect(Collectors.toSet()));
            }
            String jobId = String.valueOf(taskReq.getJob());
            StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix()).append(taskReq.getDataTask().getTaskIdName());
            ModelOutputPathDto outputPathDto = new ModelOutputPathDto(baseSb.toString());
            taskReq.getDataTask().setTaskResultContent(JSONObject.toJSONString(outputPathDto));
            int batchSize = 128;
            int numlters = 5;
            try {
                if (taskReq.getValueMap().containsKey("batchSize")){
                    batchSize = Integer.parseInt(taskReq.getValueMap().get("batchSize"));
                }
                if (taskReq.getValueMap().containsKey("numlters")){
                    numlters = Integer.parseInt(taskReq.getValueMap().get("numlters"));
                }
            }catch (Exception e){
                log.info(e.getMessage());
            }
            Common.ParamValue batchSizeParamValue = Common.ParamValue.newBuilder().setValueInt32(batchSize).build();
            Common.ParamValue numItersParamValue = Common.ParamValue.newBuilder().setValueInt32(numlters).build();
            Common.ParamValue dataFileParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(resourceIds.stream().collect(Collectors.joining(";")).getBytes(StandardCharsets.UTF_8))).build();
            Common.ParamValue modelNameeParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(outputPathDto.getModelFileName().getBytes(StandardCharsets.UTF_8))).build();
            Common.Params params = Common.Params.newBuilder()
                    .putParamMap("BatchSize", batchSizeParamValue)
                    .putParamMap("NumIters", numItersParamValue)
                    .putParamMap("Data_File", dataFileParamValue)
                    .putParamMap("modelName", modelNameeParamValue)
                    .build();
            Common.TaskContext taskBuild = Common.TaskContext.newBuilder().setJobId(jobId).setRequestId(String.valueOf(SnowflakeId.getInstance().nextId())).setTaskId(taskReq.getDataTask().getTaskIdName()).build();
            Common.Task task = Common.Task.newBuilder()
                    .setType(Common.TaskType.ACTOR_TASK)
                    .setParams(params)
                    .setName("logistic_regression")
                    .setLanguage(Common.Language.PROTO)
                    .setCode(ByteString.copyFrom("logistic_regression".getBytes(StandardCharsets.UTF_8)))
                    .setTaskInfo(taskBuild)
                    .addInputDatasets("Data_File")
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
            if (reply.getRetCode()==0){
                dataTaskMonitorService.continuouslyObtainTaskStatus(taskReq.getDataTask(),taskBuild,reply.getPartyCount(),null);
                if (!TaskStateEnum.SUCCESS.getStateType().equals(taskReq.getDataTask().getTaskState())){
                    File sourceFile = new File(baseSb.toString());
                    if (sourceFile.isDirectory()){
                        File[] files = sourceFile.listFiles();
                        if (files!=null){
                            for (File file : files) {
                                if (file.getName().contains("modelFileName")){
                                    taskReq.getDataTask().setTaskState(TaskStateEnum.SUCCESS.getStateType());
                                    taskReq.getDataTask().setTaskErrorMsg("");
                                    break;
                                }
                            }
                        }else {
                            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:目录文件夹中无文件");
                        }
                    }else {
                        taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                        taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:无目录文件夹");
                    }
                }
            }else {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:"+reply.getRetCode());
            }
        } catch (Exception e) {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:"+e.getMessage());
            log.info("grpc Exception:{}", e.getMessage());
            e.printStackTrace();
        }
        return BaseResultEntity.success();
    }
    private BaseResultEntity lr(DataComponentReq req, ComponentTaskReq taskReq,ModelTypeEnum modelType) {
        String freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(modelType.getFtlName(), freeMarkerConfigurer, taskReq.getFreemarkerMap());
        if (freemarkerContent != null) {
            try {
                String jobId = String.valueOf(taskReq.getJob());
                StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix()).append(taskReq.getDataTask().getTaskIdName());
                ModelOutputPathDto outputPathDto = new ModelOutputPathDto(baseSb.toString());
                taskReq.getDataTask().setTaskResultContent(JSONObject.toJSONString(outputPathDto));
                taskReq.getDataModelTask().setPredictFile(outputPathDto.getIndicatorFileName());
                Common.ParamValue predictFileNameParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(outputPathDto.getPredictFileName().getBytes(StandardCharsets.UTF_8))).build();
                Common.ParamValue modelFileNameParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(outputPathDto.getModelFileName().getBytes(StandardCharsets.UTF_8))).build();
                Common.ParamValue indicatorFileNameParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(outputPathDto.getIndicatorFileName().getBytes(StandardCharsets.UTF_8))).build();
                Common.Params params = Common.Params.newBuilder()
                        .putParamMap("predictFileName", predictFileNameParamValue)
                        .putParamMap("modelFileName", modelFileNameParamValue)
                        .putParamMap("indicatorFileName", indicatorFileNameParamValue)
                        .build();
                Common.TaskContext taskBuild = Common.TaskContext.newBuilder().setJobId(jobId).setRequestId(String.valueOf(SnowflakeId.getInstance().nextId())).setTaskId(taskReq.getDataTask().getTaskIdName()).build();
                Common.Task task = Common.Task.newBuilder()
                        .setType(Common.TaskType.ACTOR_TASK)
                        .setParams(params)
                        .setName("modelTask")
                        .setLanguage(Common.Language.PYTHON)
                        .setCode(ByteString.copyFrom(freemarkerContent.getBytes(StandardCharsets.UTF_8)))
                        .setTaskInfo(taskBuild)
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
                if (reply.getRetCode()==0){
                    dataTaskMonitorService.continuouslyObtainTaskStatus(taskReq.getDataTask(),taskBuild,reply.getPartyCount(),null);
                    if (taskReq.getDataTask().getTaskState().equals(TaskStateEnum.SUCCESS.getStateType())){
                        File sourceFile = new File(baseSb.toString());
                        if (sourceFile.isDirectory()){
                            File[] files = sourceFile.listFiles();
                            if (files!=null){
                                for (File file : files) {
                                    if (file.getName().contains("modelFileName")){
                                        taskReq.getDataModelTask().setPredictContent(FileUtil.getFileContent(taskReq.getDataModelTask().getPredictFile()));
                                        taskReq.getDataTask().setTaskState(TaskStateEnum.SUCCESS.getStateType());
                                        taskReq.getDataTask().setTaskErrorMsg("");
                                        break;
                                    }
                                }
                            }else {
                                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:目录文件夹中无文件");
                            }
                        }else {
                            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:无目录文件夹");
                        }
                    }
                }else {
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:"+reply.getRetCode());
                }
            } catch (Exception e) {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:"+e.getMessage());
                log.info("grpc Exception:{}", e.getMessage());
                e.printStackTrace();
            }
        }else {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:创建模板失败null");
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity xgb(DataComponentReq req, ComponentTaskReq taskReq){
        Long[] portNumber = getPortNumber();
        taskReq.getFreemarkerMap().put(DataConstant.PYTHON_LABEL_PORT,portNumber[0].toString());
        taskReq.getFreemarkerMap().put(DataConstant.PYTHON_GUEST_PORT,portNumber[1].toString());
        String freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_EN_PATH, freeMarkerConfigurer, taskReq.getFreemarkerMap());
        if (freemarkerContent != null) {
            try {
                String jobId = String.valueOf(taskReq.getJob());
                StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getRunModelFileUrlDirPrefix()).append(taskReq.getDataTask().getTaskIdName());
                ModelOutputPathDto outputPathDto = new ModelOutputPathDto(baseSb.toString());
                taskReq.getDataTask().setTaskResultContent(JSONObject.toJSONString(outputPathDto));
                taskReq.getDataModelTask().setPredictFile(outputPathDto.getIndicatorFileName());
                Common.ParamValue predictFileNameParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(outputPathDto.getPredictFileName().getBytes(StandardCharsets.UTF_8))).build();
                Common.ParamValue indicatorFileNameParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(outputPathDto.getIndicatorFileName().getBytes(StandardCharsets.UTF_8))).build();
                Common.ParamValue hostLookupTableParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(outputPathDto.getHostLookupTable().getBytes(StandardCharsets.UTF_8))).build();
                Common.ParamValue guestLookupTableParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(outputPathDto.getGuestLookupTable().getBytes(StandardCharsets.UTF_8))).build();
                Common.ParamValue modelFileNameParamValue = Common.ParamValue.newBuilder().setValueString(ByteString.copyFrom(outputPathDto.getModelFileName().getBytes(StandardCharsets.UTF_8))).build();
                Common.Params params = Common.Params.newBuilder()
                        .putParamMap("predictFileName", predictFileNameParamValue)
                        .putParamMap("indicatorFileName", indicatorFileNameParamValue)
                        .putParamMap("hostLookupTable", hostLookupTableParamValue)
                        .putParamMap("guestLookupTable", guestLookupTableParamValue)
                        .putParamMap("modelFileName", modelFileNameParamValue)
                        .build();
                Common.TaskContext taskBuild = Common.TaskContext.newBuilder().setJobId(jobId).setRequestId(String.valueOf(SnowflakeId.getInstance().nextId())).setTaskId(taskReq.getDataTask().getTaskIdName()).build();
                Common.Task task = Common.Task.newBuilder()
                        .setType(Common.TaskType.ACTOR_TASK)
                        .setParams(params)
                        .setName("modelTask")
                        .setLanguage(Common.Language.PYTHON)
                        .setCode(ByteString.copyFrom(freemarkerContent.getBytes(StandardCharsets.UTF_8)))
                        .setTaskInfo(taskBuild)
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
                if (reply.getRetCode()==0){
                    dataTaskMonitorService.continuouslyObtainTaskStatus(taskReq.getDataTask(),taskBuild,reply.getPartyCount(),null);
                    if (taskReq.getDataTask().getTaskState().equals(TaskStateEnum.SUCCESS.getStateType())){
                        File sourceFile = new File(baseSb.toString());
                        if (sourceFile.isDirectory()){
                            File[] files = sourceFile.listFiles();
                            if (files!=null){
                                for (File file : files) {
                                    if (file.getName().contains("modelFileName")){
                                        taskReq.getDataModelTask().setPredictContent(FileUtil.getFileContent(taskReq.getDataModelTask().getPredictFile()));
                                        taskReq.getDataTask().setTaskState(TaskStateEnum.SUCCESS.getStateType());
                                        taskReq.getDataTask().setTaskErrorMsg("");
                                        break;
                                    }
                                }
                            }else {
                                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:目录文件夹中无文件");
                            }
                        }else {
                            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:无目录文件夹");
                        }
                    }
                }else {
                    taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                    taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:"+reply.getRetCode());
                }
            } catch (Exception e) {
                taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
                taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:"+e.getMessage());
                log.info("grpc Exception:{}", e.getMessage());
                e.printStackTrace();
            }
        }else {
            taskReq.getDataTask().setTaskState(TaskStateEnum.FAIL.getStateType());
            taskReq.getDataTask().setTaskErrorMsg(req.getComponentName()+"运行失败:创建模板失败null");
        }
        return BaseResultEntity.success();
    }

    public Long[] getPortNumber(){
        Long[] port = new Long[2];
        String hostKey = RedisKeyConstant.REQUEST_PORT_NUMBER.replace("<square>", "h");
        String guestKey = RedisKeyConstant.REQUEST_PORT_NUMBER.replace("<square>", "g");
        // 递增还是递减
        String squareKey = RedisKeyConstant.REQUEST_PORT_NUMBER.replace("<square>", "s");
        String squareVal = stringRedisTemplate.opsForValue().get(squareKey);
        String hostVal = stringRedisTemplate.opsForValue().get(hostKey);
        if (StringUtils.isBlank(hostVal)){
            stringRedisTemplate.opsForValue().set(squareKey,"0");
            stringRedisTemplate.opsForValue().set(hostKey,DataConstant.HOST_PORT_RANGE[0].toString());
            stringRedisTemplate.opsForValue().set(guestKey,DataConstant.GUEST_PORT_RANGE[0].toString());
        }
        if (StringUtils.isBlank(squareVal) || "0".equals(squareVal)){
            port[0] = stringRedisTemplate.opsForValue().increment(hostKey);
            port[1] = stringRedisTemplate.opsForValue().increment(guestKey);
            if (DataConstant.HOST_PORT_RANGE[1].equals(port[0])) {
                stringRedisTemplate.opsForValue().set(squareKey,"1");
            }
        }else {
            port[0] = stringRedisTemplate.opsForValue().decrement(hostKey);
            port[1] = stringRedisTemplate.opsForValue().decrement(guestKey);
            if (DataConstant.HOST_PORT_RANGE[0].equals(port[0])) {
                stringRedisTemplate.opsForValue().set(squareKey,"0");
            }
        }
        return port;
    }
}
