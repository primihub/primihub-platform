package com.primihub.biz.service.data;


import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.ByteString;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dto.ModelEvaluationDto;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.vo.ModelProjectResourceVo;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.repository.primarydb.data.DataModelPrRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataModelRepository;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.entity.data.req.DataComponentValue;
import com.primihub.biz.entity.data.vo.ModelComponentJson;
import com.primihub.biz.entity.data.vo.ModelResourceVo;
import com.primihub.biz.entity.feign.FedlearnerJobApi;
import com.primihub.biz.entity.sys.po.SysFile;
import com.primihub.biz.util.FileUtil;
import com.primihub.biz.util.FreemarkerUtil;
import com.primihub.biz.util.crypt.DateUtil;
import java_worker.PushTaskReply;
import java_worker.PushTaskRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import primihub.rpc.Common;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ModelInitService {



    @Autowired
    private DataModelPrRepository dataModelPrRepository;
    @Autowired
    private DataModelRepository dataModelRepository;
    @Autowired
    private BaseConfiguration baseConfiguration;
    @Autowired
    private WorkGrpcClient workGrpcClient;
    @Autowired
    FreeMarkerConfigurer freeMarkerConfigurer;
    @Autowired
    private DataProjectRepository dataProjectRepository;
    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;


    @Async
    public void runModelTaskFeign(DataModel dataModel,DataTask dataTask){
        log.info("run model task grpc modelId:{} modelName:{} start time:{}",dataModel.getModelId(),dataModel.getModelName(),System.currentTimeMillis());
        ModelComponentJson modelComponent = dataModelRepository.queryModelComponenJsonByUserId(null,0,dataModel.getModelId());
        FedlearnerJobApi fedlearnerJobApi = null;
        if (modelComponent!=null) {
            if (StringUtils.isNotBlank(modelComponent.getComponentJson())) {
                fedlearnerJobApi  = JSONObject.parseObject(modelComponent.getComponentJson(), FedlearnerJobApi.class);
            }
        }
        dataTask.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        dataTaskPrRepository.updateDataTask(dataTask);
        DataModelTask modelTask = new DataModelTask();
        modelTask.setTaskId(dataTask.getTaskId());
        modelTask.setModelId(dataModel.getModelId());
        List<DataComponentReq> modelComponents = fedlearnerJobApi.getModelComponents();
        Map<String, DataComponentReq> reqMap = modelComponents.stream().collect(Collectors.toMap(DataComponentReq::getComponentCode, Function.identity()));
        List<DataComponent> dataComponents = dataModelRepository.queryModelComponentByParams(dataModel.getModelId(), null);
        modelTask.setComponentJson(JSONObject.toJSONString(dataComponents));
        dataModelPrRepository.saveDataModelTask(modelTask);
        log.info("检索model组件 数量:{}",modelComponents.size());
        for (DataComponent dataComponent : dataComponents) {
            dataComponent.setStartTime(System.currentTimeMillis());
            dataComponent.setComponentState(2);
            dataModelPrRepository.updateDataComponent(dataComponent);
            if ("model".equals(dataComponent.getComponentCode())){
                List<DataComponentValue> modelType = reqMap.get(dataComponent.getComponentCode()).getComponentValues().stream().filter(cv -> cv.getKey().equals("modelType")).collect(Collectors.toList());
                log.info("检索model组件->modelType 数量:{}",modelType.size());
                if (!modelType.isEmpty()){
                    DataComponentValue dataComponentValue = modelType.get(0);
                    log.info("检索model组件->modelType value:{}",dataComponentValue.toString());
                    if (dataComponentValue.getVal().equals("2")){
                        List<DataComponentValue> dataAlignment = reqMap.get("dataAlignment").getComponentValues();
                        if (dataAlignment!=null&&dataAlignment.size()!=0&&dataAlignment.get(0)!=null&&StringUtils.isNotBlank(dataAlignment.get(0).getVal())){
                            List<ModelProjectResourceVo> resourceList = JSONObject.parseArray(dataAlignment.get(0).getVal(), ModelProjectResourceVo.class);
                            log.info("查询数据数量:{}",resourceList.size());
                            if (resourceList.size()==2) {
                                Map<String, String> map = new HashMap<>();
                                List<DataModelResource> dmrList = new ArrayList<>();
                                for (ModelProjectResourceVo modelProjectResourceVo : resourceList) {
                                    map.put(modelProjectResourceVo.getParticipationIdentity()==1?DataConstant.PYTHON_LABEL_DATASET:DataConstant.PYTHON_GUEST_DATASET, modelProjectResourceVo.getResourceId());
                                    DataModelResource dataModelResource = new DataModelResource(dataModel.getModelId());
                                    dataModelResource.setTaskId(dataTask.getTaskId());
                                    dataModelResource.setResourceId(modelProjectResourceVo.getResourceId());
                                    dataModelResource.setAlignmentNum(getrandom(1, 10000));
                                    dataModelResource.setPrimitiveParamNum(getrandom(1, 100));
                                    dataModelResource.setModelParamNum(getrandom(1, 100));
                                    dmrList.add(dataModelResource);
                                }
                                map.put(DataConstant.PYTHON_TEST_DATASET, DataConstant.PYTHON_TEST_DATASET);
                                log.info("map:{}",map);
                                dataModelPrRepository.saveDataModelResource(dmrList);
                                String freemarkerContent = FreemarkerUtil.configurerCreateFreemarkerContent(DataConstant.FREEMARKER_PYTHON_EN_PAHT, freeMarkerConfigurer, map);
                                if (freemarkerContent != null) {
                                    try {
                                        Date date = new Date();
                                        StringBuilder baseSb = new StringBuilder().append(baseConfiguration.getResultUrlDirPrefix()).append(DateUtil.formatDate(date, DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat())).append("/");
                                        StringBuilder predictFileName = new StringBuilder(baseSb.toString()).append(UUID.randomUUID()).append(".csv");
                                        dataTask.setTaskResultPath(predictFileName.toString());
                                        StringBuilder indicatorFileName = new StringBuilder(baseSb.toString()).append(UUID.randomUUID()).append(".json");
                                        modelTask.setPredictFile(indicatorFileName.toString());
                                        Common.ParamValue predictFileNameParamValue = Common.ParamValue.newBuilder().setValueString(predictFileName.toString()).build();
                                        Common.ParamValue indicatorFileNameParamValue = Common.ParamValue.newBuilder().setValueString(indicatorFileName.toString()).build();
                                        Common.Params params = Common.Params.newBuilder()
                                                .putParamMap("predictFileName", predictFileNameParamValue)
                                                .putParamMap("indicatorFileName", indicatorFileNameParamValue)
                                                .build();
                                        Common.Task task = Common.Task.newBuilder()
                                                .setType(Common.TaskType.ACTOR_TASK)
                                                .setParams(params)
                                                .setName("modelTask")
                                                .setLanguage(Common.Language.PYTHON)
                                                .setCodeBytes(ByteString.copyFrom(freemarkerContent.getBytes(StandardCharsets.UTF_8)))
                                                .setJobId(ByteString.copyFrom(dataTask.getTaskId().toString().getBytes(StandardCharsets.UTF_8)))
                                                .setTaskId(ByteString.copyFrom(dataTask.getTaskId().toString().getBytes(StandardCharsets.UTF_8)))
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
                                        dataTask.setTaskResultContent(FileUtil.getFileContent(dataTask.getTaskResultPath()));
                                        dataTask.setTaskState(TaskStateEnum.SUCCESS.getStateType());
                                        modelTask.setPredictContent(FileUtil.getFileContent(modelTask.getPredictFile()));
                                    } catch (Exception e) {
                                        dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                                        dataTask.setTaskErrorMsg(e.getMessage());
                                        log.info("grpc Exception:{}", e.getMessage());
                                    }
                                }
                            }else {
                                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                                dataTask.setTaskErrorMsg("数据集错误:获取数据集数量未达标");
                                log.info("数据集错误:获取数据集数量未达标");
                            }
                        }else {
                            dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                            dataTask.setTaskErrorMsg("选择数据集为空");
                            log.info("选择数据集为空");
                        }
                    }
                }
            }
            dataComponent.setEndTime(System.currentTimeMillis());
            dataComponent.setComponentState(1);
            modelTask.setComponentJson(JSONObject.toJSONString(dataComponents));
            dataModelPrRepository.updateDataModelTask(modelTask);
        }
        dataTask.setTaskEndTime(System.currentTimeMillis());
        dataTaskPrRepository.updateDataTask(dataTask);
        log.info("end model task grpc modelId:{} modelName:{} end time:{}",dataModel.getModelId(),dataModel.getModelName(),System.currentTimeMillis());
    }

    public static int getrandom(int start,int end) {
        int num=(int) (Math.random()*(end-start+1)+start);
        return num;
    }

    public static void main(String[] args) {
        String path = "/Users/1/2.json";
        String fileContent = FileUtil.getFileContent(path);
        ModelEvaluationDto modelEvaluationDto = JSONObject.parseObject(fileContent, ModelEvaluationDto.class);
        System.out.println();
    }


}
