package com.primihub.biz.service.data.pirphase1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.RemoteConstant;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.po.ScoreModel;
import com.primihub.biz.entity.data.po.lpy.DataCore;
import com.primihub.biz.entity.data.po.lpy.DataMap;
import com.primihub.biz.entity.data.req.DataPirCopyReq;
import com.primihub.biz.entity.data.vo.RemoteRespVo;
import com.primihub.biz.entity.data.vo.lpy.DataCoreVo;
import com.primihub.biz.repository.primarydb.data.DataCorePrimarydbRepository;
import com.primihub.biz.repository.secondarydb.data.DataCoreRepository;
import com.primihub.biz.repository.secondarydb.data.DataMapRepository;
import com.primihub.biz.repository.secondarydb.data.ScoreModelRepository;
import com.primihub.biz.service.data.ExamService;
import com.primihub.biz.service.data.PirService;
import com.primihub.biz.service.data.RemoteClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PirPhase1ExecuteIdNum implements PirPhase1Execute {
    @Autowired
    private DataCoreRepository dataCoreRepository;
    @Autowired
    private RemoteClient remoteClient;
    @Autowired
    private ScoreModelRepository scoreModelRepository;
    @Autowired
    private DataCorePrimarydbRepository dataCorePrimarydbRepository;
    @Autowired
    private ExamService examService;
    @Autowired
    private PirService pirService;
    @Autowired
    private DataMapRepository dataMapRepository;
    @Autowired
    private BaseConfiguration baseConfiguration;

    @Override
    public void processPirPhase1(DataPirCopyReq req) {
        log.info("processPirPhase1: {}", req.getTargetField());
        log.info(JSON.toJSONString(req));

        /*
        rawSet: PSI的结果
        oldScore, noScore
        oldScore, newScore, fail
         */

        Set<String> targetValueSet = req.getTargetValueSet();
        String scoreModelType = req.getScoreModelType();

        // 查询idNum存在且有分值的记录
        Set<DataCore> oldScoreDataCoreSet = dataCoreRepository.selectDataCoreFromIdNum(targetValueSet, scoreModelType);
        Set<String> oldScoreSet = oldScoreDataCoreSet.stream().map(DataCore::getIdNum).collect(Collectors.toSet());

        // 没有模型对应分值记录需要重新查询
        Collection noScore = CollectionUtils.subtract(targetValueSet, oldScoreSet);
        Set<DataMap> noScoreDataMapSet = dataMapRepository.selectDataMap(new HashSet<String>(noScore));

        List<DataCore> newScoreDataScoreList = new ArrayList<>();
        ScoreModel scoreModel = scoreModelRepository.selectScoreModelByScoreTypeValue(scoreModelType);
        double score;
        if (baseConfiguration.getWaterSwitch()) {
            // water
            for (DataMap dataMap : noScoreDataMapSet) {
                if (Objects.equals(dataMap.getPhoneNum(), RemoteConstant.UNDEFINED)) {
                    score = Double.parseDouble(RemoteClient.getRandomScore());
                } else {
                    RemoteRespVo respVo = remoteClient.queryFromRemote(dataMap.getPhoneNum(), scoreModel.getScoreModelCode());
                    if (respVo != null && ("Y").equals(respVo.getHead().getResult())) {
                        score = Double.parseDouble((String) (respVo.getRespBody().get(scoreModel.getScoreKey())));
                    } else {
                        continue;
                    }
                }
                DataCore dataCore = new DataCore();
                dataCore.setIdNum(dataMap.getIdNum());
                dataCore.setPhoneNum(dataMap.getPhoneNum());
                dataCore.setScoreModelType(scoreModelType);
                dataCore.setScore(score);
                newScoreDataScoreList.add(dataCore);
            }
            if (CollectionUtils.isNotEmpty(newScoreDataScoreList)) {
                dataCorePrimarydbRepository.saveDataCoreSet(newScoreDataScoreList);
            }
        } else {
            // noWater
            for (DataMap dataMap : noScoreDataMapSet) {
                if (Objects.equals(dataMap.getPhoneNum(), RemoteConstant.UNDEFINED)) {
                    continue;
                } else {
                    RemoteRespVo respVo = remoteClient.queryFromRemote(dataMap.getPhoneNum(), scoreModel.getScoreModelCode());
                    if (respVo != null && ("Y").equals(respVo.getHead().getResult())) {
                        score = Double.parseDouble((String) (respVo.getRespBody().get(scoreModel.getScoreKey())));
                    } else {
                        continue;
                    }
                }
                DataCore dataCore = new DataCore();
                dataCore.setIdNum(dataMap.getIdNum());
                dataCore.setPhoneNum(dataMap.getPhoneNum());
                dataCore.setScoreModelType(scoreModelType);
                dataCore.setScore(score);
                newScoreDataScoreList.add(dataCore);
            }
            if (CollectionUtils.isNotEmpty(newScoreDataScoreList)) {
                dataCorePrimarydbRepository.saveDataCoreSet(newScoreDataScoreList);
            }
        }

        Set<DataCoreVo> voSet = dataCoreRepository.selectDataCoreWithScore(scoreModelType);
        if (CollectionUtils.isEmpty(voSet)) {
            log.info("==================== FAIL ====================");
            log.error("样本适配度太低，无法执行PIR任务");
            req.setTaskState(TaskStateEnum.FAIL.getStateType());
            pirService.sendFinishPirTask(req);
            return;
        }
        // 成功后开始生成文件
        String jsonArrayStr = JSON.toJSONString(voSet);
        List<Map> maps = JSONObject.parseArray(jsonArrayStr, Map.class);
        // 生成数据源
        String resourceName = new StringBuffer()
                .append("PIR处理资源")
                .append(SysConstant.HYPHEN_DELIMITER)
                .append(req.getTaskName())
                .toString();
        DataResource dataResource = examService.generateTargetResource(maps, resourceName);

        if (dataResource == null) {
            req.setTaskState(TaskStateEnum.FAIL.getStateType());
            pirService.sendFinishPirTask(req);
            log.info("====================== FAIL ======================");
            log.error("generate target resource failed!");
        } else {
            req.setTargetResourceId(dataResource.getResourceFusionId());
            req.setTaskState(TaskStateEnum.READY.getStateType());
            pirService.sendFinishPirTask(req);
            log.info("==================== SUCCESS ====================");
        }
    }
}
