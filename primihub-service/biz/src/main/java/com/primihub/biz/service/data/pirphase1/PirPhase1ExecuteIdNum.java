package com.primihub.biz.service.data.pirphase1;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.lpy.DataCore;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.po.ScoreModel;
import com.primihub.biz.entity.data.req.DataPirCopyReq;
import com.primihub.biz.entity.data.vo.lpy.DataCoreVo;
import com.primihub.biz.entity.data.vo.RemoteRespVo;
import com.primihub.biz.repository.primarydb.data.DataCorePrimarydbRepository;
import com.primihub.biz.repository.secondarydb.data.DataCoreRepository;
import com.primihub.biz.repository.secondarydb.data.ScoreModelRepository;
import com.primihub.biz.service.data.ExamService;
import com.primihub.biz.service.data.PirService;
import com.primihub.biz.service.data.RemoteClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
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

    @Override
    public void processPirPhase1(DataPirCopyReq req) {
        log.info("processPirPhase1: {}", req.getTargetField());
        log.info(JSON.toJSONString(req));

        Set<String> targetValueSet = req.getTargetValueSet();
        // 在这里得区分
        Set<DataCore> withPhone = dataCoreRepository.selectExistentDataCore(targetValueSet);
        // 在这里不用管
        Set<DataCore> withScore = dataCoreRepository.selectDataCoreFromIdNum(targetValueSet, req.getScoreModelType());

        Map<String, List<DataCore>> withPhoneGroup = withPhone.stream()
                .collect(Collectors.groupingBy(DataCore::getIdNum));

        Map<String, DataCore> withScoreMap = withScore.stream().collect(Collectors.toMap(DataCore::getIdNum, Function.identity()));

        String scoreModelType = req.getScoreModelType();
        ScoreModel scoreModel = scoreModelRepository.selectScoreModelByScoreTypeValue(scoreModelType);

        withPhoneGroup.forEach((key, value) -> {
            if (withScoreMap.containsKey(key)) {
                return;
            }
            RemoteRespVo respVo = remoteClient.queryFromRemote(value.get(0).getPhoneNum(), scoreModel);
            if (respVo != null && ("Y").equals(respVo.getHead().getResult())) {
                DataCore dataCore = new DataCore();
                dataCore.setIdNum(value.get(0).getIdNum());
                dataCore.setPhoneNum(value.get(0).getPhoneNum());
                dataCore.setScoreModelType(scoreModelType);
                dataCore.setScore(Double.valueOf((String) (respVo.getRespBody().get(scoreModel.getScoreKey()))));
                dataCore.setY(value.get(0).getY());
                dataCorePrimarydbRepository.saveDataCore(dataCore);
            }
        });

        Set<DataCoreVo> voSet = dataCoreRepository.selectDataCoreWithScore(scoreModelType);
        if (CollectionUtils.isEmpty(voSet)) {
            log.info("==================== FAIL ====================");
            log.info("样本适配度太低，无法执行PIR任务");
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

        log.info("==================== SUCCESS ====================");
        req.setTargetResourceId(dataResource.getResourceFusionId());
        req.setTaskState(TaskStateEnum.READY.getStateType());
        pirService.sendFinishPirTask(req);

    }
}
