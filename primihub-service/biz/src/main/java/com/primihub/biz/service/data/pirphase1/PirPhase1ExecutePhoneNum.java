package com.primihub.biz.service.data.pirphase1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.po.ScoreModel;
import com.primihub.biz.entity.data.po.lpy.DataMobile;
import com.primihub.biz.entity.data.req.DataPirCopyReq;
import com.primihub.biz.entity.data.vo.RemoteRespVo;
import com.primihub.biz.entity.data.vo.lpy.MobilePirVo;
import com.primihub.biz.repository.primarydb.data.DataMobilePrimarydbRepository;
import com.primihub.biz.repository.secondarydb.data.DataMobileRepository;
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
public class PirPhase1ExecutePhoneNum implements PirPhase1Execute {
    @Autowired
    private DataMobileRepository dataMobileRepository;
    @Autowired
    private DataMobilePrimarydbRepository dataMobilePrimarydbRepository;
    @Autowired
    private PirService pirService;
    @Autowired
    private ExamService examService;
    @Autowired
    private ScoreModelRepository scoreModelRepository;
    @Autowired
    private RemoteClient remoteClient;

    @Override
    public void processPirPhase1(DataPirCopyReq req) {
        log.info("process exam future task : phoneNum");

        Set<String> rawSet = req.getTargetValueSet();
        Set<DataMobile> dataMobileSet = null;
            /*
            rawSet
            old new
            old newExist newNonExist
             */
        Set<DataMobile> oldDataMobileSet = dataMobileRepository.selectMobileWithScore(req.getTargetValueSet(), req.getScoreModelType());
        Set<String> oldSet = oldDataMobileSet.stream().map(DataMobile::getPhoneNum).collect(Collectors.toSet());
        Set<String> newSet = new HashSet<String>(CollectionUtils.subtract(rawSet, oldSet));

        ScoreModel scoreModel = scoreModelRepository.selectScoreModelByScoreTypeValue(req.getScoreModelType());
        List<DataMobile> newMobileSet = new ArrayList<>();
        for (String mobile : newSet) {
            RemoteRespVo respVo = remoteClient.queryFromRemote(mobile, scoreModel.getScoreModelCode());
            if (respVo != null && ("Y").equals(respVo.getHead().getResult())) {
                DataMobile dataMobile = new DataMobile();
                dataMobile.setPhoneNum(mobile);
                dataMobile.setScore(Double.valueOf((String) (respVo.getRespBody().get(scoreModel.getScoreKey()))));
                dataMobile.setScoreModelType(scoreModel.getScoreModelType());
                newMobileSet.add(dataMobile);
            }
        }
        if (CollectionUtils.isNotEmpty(newMobileSet)) {
            dataMobilePrimarydbRepository.saveMobileList(newMobileSet);
        }

        dataMobileSet = dataMobileRepository.selectMobileWithScore(req.getTargetValueSet(), req.getScoreModelType());

        if (CollectionUtils.isEmpty(dataMobileSet)) {
            log.info("==================== FAIL ====================");
            log.info("样本适配度太低，无法执行PIR任务");
            req.setTaskState(TaskStateEnum.FAIL.getStateType());

            pirService.sendFinishPirTask(req);
            return;
        }

        Set<MobilePirVo> collect = dataMobileSet.stream().map(MobilePirVo::new).collect(Collectors.toSet());
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
