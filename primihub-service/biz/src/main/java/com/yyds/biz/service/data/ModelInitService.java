package com.yyds.biz.service.data;


import com.alibaba.fastjson.JSONObject;
import com.yyds.biz.constant.DataConstant;
import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.data.po.*;
import com.yyds.biz.entity.data.vo.ModelComponentJson;
import com.yyds.biz.entity.data.vo.ModelResourceVo;
import com.yyds.biz.entity.feign.FedlearnerJobApi;
import com.yyds.biz.entity.feign.FedlearnerJobApiResource;
import com.yyds.biz.entity.sys.po.SysFile;
import com.yyds.biz.repository.primarydb.data.DataModelPrRepository;
import com.yyds.biz.repository.primarydb.data.DataResourcePrRepository;
import com.yyds.biz.repository.secondarydb.data.DataModelRepository;
import com.yyds.biz.repository.secondarydb.sys.SysFileSecondarydbRepository;
import com.yyds.biz.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ModelInitService {



    @Autowired
    private DataModelPrRepository dataModelPrRepository;
    @Autowired
    private DataModelRepository dataModelRepository;
    @Autowired
    @LoadBalanced
    private RestTemplate restTemplate;

    @Async
    public void updateDataModel(DataModel dataModel) {
        log.info("进入了");
//        communicationGrpc();
//        try {
//            Thread.sleep(30000L);
//        }catch (Exception e){
//
//        }
        // 模型信息
        initialSampleDataModel(dataModel);
        initialSampleDataModelQuota(dataModel.getModelId());
        dataModel.setLatestTaskStatus(2);
        dataModelPrRepository.updateDataModel(dataModel);
    }
    @Async
    public void runModelTaskFeign(DataModel dataModel){
        log.info("run model task feign modelId:{} modelName:{} start time:{}",dataModel.getModelId(),dataModel.getModelName(),System.currentTimeMillis());
        ModelComponentJson modelComponent = dataModelRepository.queryModelComponenJsonByUserId(null,0,dataModel.getModelId());
        FedlearnerJobApi fedlearnerJobApi = null;
        if (modelComponent!=null) {
            if (StringUtils.isNotBlank(modelComponent.getComponentJson())) {
                fedlearnerJobApi  = JSONObject.parseObject(modelComponent.getComponentJson(), FedlearnerJobApi.class);
            }
        }
        if (fedlearnerJobApi!=null) {
            try {
                List<FedlearnerJobApiResource> resources = new ArrayList();
                List<ModelResourceVo> modelResourceVos = dataModelRepository.queryModelResource(dataModel.getModelId());
                for (ModelResourceVo vo : modelResourceVos) {
                    resources.add(new FedlearnerJobApiResource(vo.getResourceId(),vo.getUrl()));
                }
                fedlearnerJobApi.setResources(resources);
                HttpHeaders headers = new HttpHeaders();
                // 以表单的方式提交
                headers.setContentType(MediaType.APPLICATION_JSON);
                //将请求头部和参数合成一个请求
                String apiJson = JSONObject.toJSONString(fedlearnerJobApi);
                log.info("run model task feign modelId:{} modelName:{} jsonStr:{}",dataModel.getModelId(),dataModel.getModelName(),apiJson);
                HttpEntity<String> requestEntity = new HttpEntity<>(apiJson, headers);
                ResponseEntity<BaseResultEntity> baseResult = restTemplate.postForEntity(DataConstant.FEDLEARNER_JOB_RUN, requestEntity, BaseResultEntity.class);
                if (baseResult!=null&& baseResult.getBody()!=null&&baseResult.getBody().getCode()==0){
                    // 运行中
                    dataModel.setLatestTaskStatus(1);
                }else {
                    // 失败
                    dataModel.setLatestTaskStatus(3);
                }
                dataModelPrRepository.updateDataModel(dataModel);
                log.info("run model task feign modelId:{} modelName:{} responseEntity:{}",dataModel.getModelId(),dataModel.getModelName(),baseResult.getBody());
                updateModelTaskDataAndStatus(dataModel);
            } catch (Exception e) {
                log.info("run model task feign modelId:{} modelName:{} end Exception:{}",dataModel.getModelId(),dataModel.getModelName(),e.getMessage());
            }
        }
        log.info("run model task feign modelId:{} modelName:{} end time:{}",dataModel.getModelId(),dataModel.getModelName(),System.currentTimeMillis());
    }

    public void updateModelTaskDataAndStatus(DataModel dataModel){
        // 设置5分钟超时
        long startTime = System.currentTimeMillis();
        try {
            boolean isUpdate = false;
            List<DataComponent> dataComponents = null;
            while ((System.currentTimeMillis()-startTime)<DataConstant.UPDATE_MODEL_TIMEOUT){
                dataComponents = dataModelRepository.queryModelComponentByParams(dataModel.getModelId(), null);
                if (dataComponents.stream().filter(dc -> dc.getComponentState() != 1).collect(Collectors.toList()).size()==0){
                    log.info("end model task feign modelId:{} modelName:{} time:{}",dataModel.getModelId(),dataModel.getModelName(),System.currentTimeMillis());
                    dataModel.setLatestTaskStatus(2);
                    dataModelPrRepository.updateDataModel(dataModel);
                    supplementModelData(dataModel,dataComponents);
                    isUpdate = true;
                    break;
                }else {
                    Thread.sleep(10000L);
                }
            }
            if (!isUpdate){
                dataModel.setLatestTaskStatus(3);
                dataModelPrRepository.updateDataModel(dataModel);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void supplementModelData(DataModel dataModel,List<DataComponent> dataComponents){
        for (DataComponent dataComponent : dataComponents) {
            switch (dataComponent.getComponentCode()){
                case "dataAlignment":
                    dataModel.setLatestAlignmentCost(dataComponent.getTimeConsuming().intValue());
                    break;
                case "features":
                    dataModel.setLatestFeatureCost(dataComponent.getTimeConsuming().intValue());
                    break;
                case "sample":
                    dataModel.setLatestSampleCost(dataComponent.getTimeConsuming().intValue());
                    break;
                case "exception":
                    dataModel.setLatestExceptionCost(dataComponent.getTimeConsuming().intValue());
                    break;
                case "featureCoding":
//                    dataModel.setLatestFeatureCost(dataComponent.getTimeConsuming().intValue());
                    break;
                case "model":
                    dataModel.setLatestAnalyzeCost(dataComponent.getTimeConsuming().intValue());
                    break;
                case "assessment":
                    break;
            }
        }
        dataModelPrRepository.updateDataModel(dataModel);

    }

    @Async
    public void runModelTask(DataModel dataModel){
        log.info("prepareDataModelTask : {}",dataModel.getModelId());
        DataModelTask modelTask = initialSampleDataTask(dataModel);
        dataModelPrRepository.saveDataModelTask(modelTask);
        dataModel.setLatestTaskId(modelTask.getTaskId());
        dataModel.setLatestCostTime(modelTask.getCostTime());
        taskCopyDataModel(dataModel,modelTask);
        log.info("runModelTask modelid : {}",dataModel.getModelId());
        // 运行
        modelTask.setTaskStatus(1);
        dataModel.setLatestTaskStatus(1);
        dataModelPrRepository.updateDataModelTask(modelTask);
        dataModelPrRepository.updateDataModel(dataModel);
        initialSampleDataModelQuota(dataModel.getModelId());
        initialSampleDataModelComponent(dataModel.getModelId());
        // 运行完成
        modelTask.setTaskStatus(2);
        dataModel.setLatestTaskStatus(2);
        dataModelPrRepository.updateDataModelTask(modelTask);
        dataModelPrRepository.updateDataModel(dataModel);
    }


    public void taskCopyDataModel(DataModel dataModel,DataModelTask dataModelTask){
        dataModel.setLatestAlignmentRatio(dataModelTask.getAlignmentRatio());
        dataModel.setLatestAlignmentCost(dataModelTask.getAlignmentCost());
        dataModel.setLatestAnalyzeRatio(dataModelTask.getAnalyzeRatio());
        dataModel.setLatestAnalyzeCost(dataModelTask.getAnalyzeCost());
        dataModel.setLatestFeatureRatio(dataModelTask.getFeatureRatio());
        dataModel.setLatestFeatureCost(dataModelTask.getFeatureCost());
        dataModel.setLatestSampleRatio(dataModelTask.getSampleRatio());
        dataModel.setLatestSampleCost(dataModelTask.getSampleCost());
        dataModel.setLatestTrainRatio(dataModelTask.getTrainRatio());
        dataModel.setLatestTrainCost(dataModelTask.getTrainCost());
        dataModel.setLatestLackRatio(dataModelTask.getLackRatio());
        dataModel.setLatestLackCost(dataModelTask.getLackCost());
        dataModel.setLatestExceptionRatio(dataModelTask.getExceptionRatio());
        dataModel.setLatestExceptionCost(dataModelTask.getExceptionCost());
        dataModel.setLatestTaskStatus(dataModelTask.getTaskStatus());
    }

    public DataModelTask initialSampleDataTask(DataModel dataModel){
        DataModelTask dataModelTask = new DataModelTask();
        dataModelTask.setTaskStatus(0);
        dataModelTask.setModelId(dataModel.getModelId());
        dataModelTask.setCostTime(new Date());
        dataModelTask.setAlignmentRatio(new BigDecimal(getrandom(100,999)).divide(new BigDecimal(100)));
        dataModelTask.setAlignmentCost(getrandom(1,60));
        dataModelTask.setAnalyzeRatio(new BigDecimal(getrandom(100,999)).divide(new BigDecimal(100)));
        dataModelTask.setAnalyzeCost(getrandom(1,60));
        dataModelTask.setFeatureRatio(new BigDecimal(getrandom(100,999)).divide(new BigDecimal(100)));
        dataModelTask.setFeatureCost(getrandom(1,60));
        dataModelTask.setSampleRatio(new BigDecimal(getrandom(100,999)).divide(new BigDecimal(100)));
        dataModelTask.setSampleCost(getrandom(1,60));
        dataModelTask.setTrainRatio(new BigDecimal(getrandom(100,999)).divide(new BigDecimal(100)));
        dataModelTask.setTrainCost(getrandom(1,60));
        dataModelTask.setLackRatio(new BigDecimal(getrandom(100,999)).divide(new BigDecimal(100)));
        dataModelTask.setLackCost(getrandom(1,60));
        dataModelTask.setExceptionRatio(new BigDecimal(getrandom(100,999)).divide(new BigDecimal(100)));
        dataModelTask.setExceptionCost(getrandom(1,60));
        dataModelTask.setIsDel(0);
        return dataModelTask;
    }

    public void initialSampleDataModel(DataModel dataModel){
        dataModel.setLatestAlignmentRatio(new BigDecimal(getrandom(100,999)).divide(new BigDecimal(100)));
        dataModel.setLatestAlignmentCost(getrandom(1,60));
        dataModel.setLatestAnalyzeRatio(new BigDecimal(getrandom(100,999)).divide(new BigDecimal(100)));
        dataModel.setLatestAnalyzeCost(getrandom(1,60));
        dataModel.setLatestFeatureRatio(new BigDecimal(getrandom(100,999)).divide(new BigDecimal(100)));
        dataModel.setLatestFeatureCost(getrandom(1,60));
        dataModel.setLatestSampleRatio(new BigDecimal(getrandom(100,999)).divide(new BigDecimal(100)));
        dataModel.setLatestSampleCost(getrandom(1,60));
        dataModel.setLatestTrainRatio(new BigDecimal(getrandom(100,999)).divide(new BigDecimal(100)));
        dataModel.setLatestTrainCost(getrandom(1,60));
        dataModel.setLatestLackRatio(new BigDecimal(getrandom(100,999)).divide(new BigDecimal(100)));
        dataModel.setLatestLackCost(getrandom(1,60));
        dataModel.setLatestExceptionRatio(new BigDecimal(getrandom(100,999)).divide(new BigDecimal(100)));
        dataModel.setLatestExceptionCost(getrandom(1,60));
    }

    public void initialSampleDataModelComponent(Long modelId){
        List<DataComponent> dataComponents = dataModelRepository.queryModelComponentByParams(modelId, null);
        for (DataComponent dataComponent : dataComponents) {
            dataComponent.setStartTime(System.currentTimeMillis());
            try {
                Thread.sleep(getrandom(0,6000));
            }catch (Exception e){

            }
            dataComponent.setEndTime(System.currentTimeMillis());
            log.info("modelId:{} -- componentId:{} -- start:{} -- end:{} -- ms:{}",modelId,dataComponent.getComponentId(),dataComponent.getStartTime(),dataComponent.getEndTime(),dataComponent.getEndTime()-dataComponent.getStartTime());
            dataModelPrRepository.updateDataComponent(dataComponent);
        }
    }

    /**
     * 初始模型数据  -- 后期删除
     * @param modelId
     */
    public void initialSampleDataModelQuota(Long modelId){
        // 图1 https://pic3.zhimg.com/v2-a74767abb0f1e830aed11c76978d7789_1440w.jpg?source=172ae18b
        // 图2 http://aandds.com/blog/images/matplotlib_legend_titile.png
        DataModelQuota modelQuota = new DataModelQuota();
        modelQuota.setModelId(modelId);
        modelQuota.setQuotaType(1);
        modelQuota.setQuotaImage("https://pic3.zhimg.com/v2-a74767abb0f1e830aed11c76978d7789_1440w.jpg?source=172ae18b");
        modelQuota.setAuc(new BigDecimal(getrandom(1000,9999)).divide(new BigDecimal(10000)));
        modelQuota.setKs(new BigDecimal(getrandom(1000,9999)).divide(new BigDecimal(10000)));
        modelQuota.setGini(new BigDecimal(getrandom(1000,9999)).divide(new BigDecimal(10000)));
        modelQuota.setPrecision(new BigDecimal(getrandom(1000,9999)).divide(new BigDecimal(10000)));
        modelQuota.setRecall(new BigDecimal(getrandom(1000,9999)).divide(new BigDecimal(10000)));
        modelQuota.setF1Score(new BigDecimal(getrandom(1000,9999)).divide(new BigDecimal(10000)));
        dataModelPrRepository.saveDataModelQuota(modelQuota);
        DataModelQuota modelQuota2 = new DataModelQuota();
        modelQuota2.setModelId(modelId);
        modelQuota2.setQuotaType(2);
        modelQuota2.setQuotaImage("http://aandds.com/blog/images/matplotlib_legend_titile.png");
        modelQuota2.setAuc(new BigDecimal(getrandom(1000,9999)).divide(new BigDecimal(10000)));
        modelQuota2.setKs(new BigDecimal(getrandom(1000,9999)).divide(new BigDecimal(10000)));
        modelQuota2.setGini(new BigDecimal(getrandom(1000,9999)).divide(new BigDecimal(10000)));
        modelQuota2.setPrecision(new BigDecimal(getrandom(1000,9999)).divide(new BigDecimal(10000)));
        modelQuota2.setRecall(new BigDecimal(getrandom(1000,9999)).divide(new BigDecimal(10000)));
        modelQuota2.setF1Score(new BigDecimal(getrandom(1000,9999)).divide(new BigDecimal(10000)));
        dataModelPrRepository.saveDataModelQuota(modelQuota2);
    }
    public static int getrandom(int start,int end) {
        int num=(int) (Math.random()*(end-start+1)+start);
        return num;
    }


}
