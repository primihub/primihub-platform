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
import com.primihub.biz.entity.data.vo.lpy.ImeiPirVo;
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

import static com.primihub.biz.constant.RemoteConstant.UNDEFILED;

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

    @Override
    public void processPirPhase1(DataPirCopyReq req) {
        log.info("process pir phase1 future task : imei");
        log.info(JSON.toJSONString(req));

        String scoreModelType = req.getScoreModelType();

        Set<DataImei> dataImeiSet = null;
        if ("yhhhwd_score".equals(req.getScoreModelType())) {
            dataImeiSet = dataImeiRepository.selectImeiWithScore(req.getTargetValueSet(), scoreModelType);
        } else {
            /*
            liDong non
            liDongOld liDongNew non
             */
            Set<DataImei> liDongImeiSet = dataImeiRepository.selectImeiWithScore(req.getTargetValueSet(), "yhhhwd_score");
            Set<String> liDongSet = liDongImeiSet.stream().map(DataImei::getImei).collect(Collectors.toSet());
            Set<DataImei> liDongOldImeiSet = dataImeiRepository.selectImeiWithScore(liDongSet, req.getScoreModelType());
//            List<String> liDongOldSet = liDongOldImeiSet.stream().map(DataImei::getImei).collect(Collectors.toList());
//            Collection<String> liDongNewSet = CollectionUtils.subtract(liDongSet, liDongOldSet);
            Collection<DataImei> loDongNewDataImeiSet = CollectionUtils.subtract(liDongImeiSet, liDongOldImeiSet);

            ScoreModel scoreModel = scoreModelRepository.selectScoreModelByScoreTypeValue(scoreModelType);
            List<DataImei> liDongNewImeiList = new ArrayList<>();
            for (DataImei imei : loDongNewDataImeiSet) {
                if (Objects.equals(imei.getPhoneNum(), "yhhhwd_score")) {
                    // water
                    DataImei dataImei = new DataImei();
                    dataImei.setImei(imei.getImei());
                    dataImei.setScore(Double.valueOf(RemoteClient.getRandomScore()));
                    dataImei.setScoreModelType(scoreModelType);
                    dataImei.setPhoneNum(UNDEFILED);
                    liDongNewImeiList.add(dataImei);
                } else {
                    // query
                    RemoteRespVo respVo = remoteClient.queryFromRemote(imei.getImei(), scoreModel.getScoreModelCode());
                    if (respVo != null && ("Y").equals(respVo.getHead().getResult())) {
                        DataImei dataImei = new DataImei();
                        dataImei.setImei(imei.getImei());
                        dataImei.setScore(Double.valueOf((String) (respVo.getRespBody().get(scoreModel.getScoreKey()))));
                        dataImei.setScoreModelType(scoreModelType);
                        liDongNewImeiList.add(dataImei);
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(liDongNewImeiList)) {
                dataImeiPrimarydbRepository.saveImeiSet(liDongNewImeiList);
            }
            dataImeiSet = dataImeiRepository.selectImeiWithScore(req.getTargetValueSet(), scoreModelType);
        }

        if (CollectionUtils.isEmpty(dataImeiSet)) {
            log.info("==================== FAIL ====================");
            log.info("样本适配度太低，无法执行PIR任务");
            req.setTaskState(TaskStateEnum.FAIL.getStateType());

            pirService.sendFinishPirTask(req);
            return;
        }

        Set<ImeiPirVo> collect = dataImeiSet.stream().map(ImeiPirVo::new).collect(Collectors.toSet());

        // 成功后开始生成文件
        String jsonArrayStr = JSON.toJSONString(collect);
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
