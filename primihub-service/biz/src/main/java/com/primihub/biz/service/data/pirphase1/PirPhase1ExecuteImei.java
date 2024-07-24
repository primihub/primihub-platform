package com.primihub.biz.service.data.pirphase1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.po.ScoreModel;
import com.primihub.biz.entity.data.po.lpy.DataImei;
import com.primihub.biz.entity.data.req.DataPirCopyReq;
import com.primihub.biz.entity.data.vo.RemoteRespVo;
import com.primihub.biz.repository.primarydb.data.DataImeiPrimarydbRepository;
import com.primihub.biz.repository.secondarydb.data.DataImeiRepository;
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

@Service
@Slf4j
public class PirPhase1ExecuteImei implements PirPhase1Execute {
    @Autowired
    private DataImeiRepository dataImeiRepository;
    @Autowired
    private DataImeiPrimarydbRepository dataImeiPrimarydbRepository;
    @Autowired
    private ScoreModelRepository scoreModelRepository;
    @Autowired
    private PirService pirService;
    @Autowired
    private ExamService examService;
    @Autowired
    private RemoteClient remoteClient;
    @Autowired
    private DataImeiPrimarydbRepository imeiPrimaryDbRepository;

    @Override
    public void processPirPhase1(DataPirCopyReq req) {
        log.info("process pir phase1 future task : imei");
        log.info(JSON.toJSONString(req));

        String scoreModelType = req.getScoreModelType();

        Set<DataImei> dataImeiSet = null;
        if ("yhhhwd_score".equals(req.getScoreModelType())) {
            dataImeiSet = dataImeiRepository.selectImei(req.getTargetValueSet());
        } else {
            /*
            liDong non
            liDongOld liDongNew non
             */
            Set<DataImei> liDongImeiSet = dataImeiRepository.selectImei(req.getTargetValueSet());
            Set<String> liDongSet = liDongImeiSet.stream().map(DataImei::getImei).collect(Collectors.toSet());
            Set<DataImei> liDongOldImeiSet = dataImeiRepository.selectImeiWithScore(liDongSet, req.getScoreModelType());
            List<String> liDongOldSet = liDongOldImeiSet.stream().map(DataImei::getImei).collect(Collectors.toList());
            Collection<String> liDongNewSet = CollectionUtils.subtract(liDongSet, liDongOldSet);

            // todo 这里要对接真实的接口
//            Set<String> strings = phoneClientService.filterHashSet(new HashSet<>(liDongNewSet), 1.0);
//            Map<String, Double> stringDoubleMap = phoneClientService.generateScoreForKeySet(strings);

            ScoreModel scoreModel = scoreModelRepository.selectScoreModelByScoreTypeValue(scoreModelType);
            List<DataImei> liDongNewImeiList = new ArrayList<>();
            for (String imei : liDongNewSet) {
                RemoteRespVo respVo = remoteClient.queryFromRemote(imei, scoreModel.getScoreModelCode());
                if (respVo != null && ("Y").equals(respVo.getHead().getResult())) {
                    DataImei dataImei = new DataImei();
                    dataImei.setImei(imei);
                    dataImei.setScore(Double.valueOf((String) (respVo.getRespBody().get(scoreModel.getScoreKey()))));
                    dataImei.setScoreModelType(scoreModelType);
                    imeiPrimaryDbRepository.saveImei(dataImei);
                    liDongNewImeiList.add(dataImei);
                }
            }

            dataImeiPrimarydbRepository.saveImeiSet(liDongNewImeiList);

            dataImeiSet = dataImeiRepository.selectImeiWithScore(req.getTargetValueSet(), req.getScoreModelType());
        }

        if (CollectionUtils.isEmpty(dataImeiSet)) {
            log.info("==================== FAIL ====================");
            log.info("样本适配度太低，无法执行PIR任务");
            req.setTaskState(TaskStateEnum.FAIL.getStateType());

            pirService.sendFinishPirTask(req);
            return;
        }
        // 成功后开始生成文件
        String jsonArrayStr = JSON.toJSONString(dataImeiSet);
        List<Map> maps = JSONObject.parseArray(jsonArrayStr, Map.class);
        // 生成数据源
        String resourceName = new StringBuffer()
                .append("PIR处理资源")
                .append(SysConstant.HYPHEN_DELIMITER)
                .append(req.getTaskName())
                .toString();
        DataResource dataResource = examService.generateTargetResource(maps, resourceName);

        log.info("==================== SUCCESS ====================");
        req.setTargetResourceId(dataResource.getResourceFusionId());
        req.setTaskState(TaskStateEnum.READY.getStateType());
        pirService.sendFinishPirTask(req);
    }
}
