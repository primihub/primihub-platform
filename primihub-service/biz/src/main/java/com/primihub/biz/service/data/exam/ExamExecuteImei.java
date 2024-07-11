package com.primihub.biz.service.data.exam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.po.lpy.DataImei;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.vo.lpy.ImeiPsiVo;
import com.primihub.biz.repository.primarydb.data.DataImeiPrimarydbRepository;
import com.primihub.biz.repository.secondarydb.data.DataImeiRepository;
import com.primihub.biz.service.PhoneClientService;
import com.primihub.biz.service.data.ExamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExamExecuteImei implements ExamExecute {
    @Autowired
    private DataImeiRepository imeiRepository;
    @Autowired
    private DataImeiPrimarydbRepository imeiPrimaryDbRepository;
    @Autowired
    private PhoneClientService phoneClientService;
    @Autowired
    private ExamService examService;

    @Override
    public void processExam(DataExamReq req) {
        log.info("process exam future task : imei");

        // rawSet
        Set<String> rawSet = req.getFieldValueSet();

        /*
        rawSet
        oldSet, newSet => query
        oldSet, newExistSet, newNoExistSet
         */
        // 已经存在的数据
        Set<DataImei> dataImeiSet = imeiRepository.selectImei(rawSet);
        Set<String> oldSet = dataImeiSet.stream().map(DataImei::getImei).collect(Collectors.toSet());
        Collection<String> newSet = CollectionUtils.subtract(rawSet, oldSet);

        // 先过滤出存在手机号的数据
        log.info("process exam query imei, count: {}", newSet.size());

        Set<String> newExistSet = phoneClientService.filterHashSet(new HashSet<>(newSet), 0.8);
        Map<String, Double> scoreMap = phoneClientService.generateScoreForKeySet(newExistSet);

        List<DataImei> newExistDataSet = scoreMap.entrySet().stream().map(entry -> {
            DataImei po = new DataImei();
            po.setImei(entry.getKey());
            po.setScore(entry.getValue());
            po.setScoreModelType("truth_score");
            return po;
        }).collect(Collectors.toList());

        imeiPrimaryDbRepository.saveImeiSet(new ArrayList<>(newExistDataSet));

        oldSet.addAll(newExistSet);
        Set<ImeiPsiVo> existResult = oldSet.stream().map(ImeiPsiVo::new).collect(Collectors.toSet());

        String jsonArrayStr = JSON.toJSONString(existResult);
        List<Map> maps = JSONObject.parseArray(jsonArrayStr, Map.class);
        // 生成数据源
        String resourceName = "预处理生成资源" + SysConstant.HYPHEN_DELIMITER + req.getTaskId();
        DataResource dataResource = examService.generateTargetResource(maps, resourceName);
        if (dataResource == null) {
            req.setTaskState(TaskStateEnum.FAIL.getStateType());
            examService.sendEndExamTask(req);
            log.info("====================== FAIL");
        } else {
            req.setTaskState(TaskStateEnum.SUCCESS.getStateType());
            req.setTargetResourceId(dataResource.getResourceFusionId());
            examService.sendEndExamTask(req);
            log.info("====================== SUCCESS");
        }
    }
}
