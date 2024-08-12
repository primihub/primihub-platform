package com.primihub.biz.service.data.exam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.po.lpy.DataMobile;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.vo.RemoteRespVo;
import com.primihub.biz.entity.data.vo.lpy.MobilePsiVo;
import com.primihub.biz.repository.primarydb.data.DataMobilePrimarydbRepository;
import com.primihub.biz.repository.secondarydb.data.DataMobileRepository;
import com.primihub.biz.service.data.ExamService;
import com.primihub.biz.service.data.RemoteClient;
import com.primihub.biz.util.crypt.SM3Util;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExamExecutePhoneNum implements ExamExecute {
    @Autowired
    private ExamService examService;
    @Autowired
    private DataMobileRepository mobileRepository;
    @Autowired
    private RemoteClient remoteClient;
    @Autowired
    private DataMobilePrimarydbRepository mobilePrimaryDbRepository;

    @Override
    public void processExam(DataExamReq req) {
        log.info("process exam future task : phoneNum");

        Set<String> rawSet = req.getFieldValueSet();
        /*
        rawSet
        oldSet, newSet
        oldSet, newExistSet, noExistSet
         */
        Set<DataMobile> dataMobileSet = mobileRepository.selectMobile(rawSet);
        Set<String> oldSet = dataMobileSet.stream().map(DataMobile::getPhoneNum).collect(Collectors.toSet());
        Collection<String> newSet = CollectionUtils.subtract(rawSet, oldSet);

        // 先过滤出存在手机号的数据
        log.info("process exam query mobile size, count: {}", newSet.size());

        // 预处理使用模型分
        // 预处理使用模型分
        List<DataMobile> newExistDataSet = new ArrayList<>();
        for (String mobile : newSet) {
            RemoteRespVo respVo = remoteClient.queryFromRemote(mobile, "AME000818");
            if (respVo != null && ("Y").equals(respVo.getHead().getResult())) {
                DataMobile dataMobile = new DataMobile();
                dataMobile.setPhoneNum(mobile);
                dataMobile.setScore(Double.valueOf((String) (respVo.getRespBody().get("yhhhwd_score"))));
                dataMobile.setY(null);
                dataMobile.setScoreModelType("yhhhwd_score");
                newExistDataSet.add(dataMobile);
            }
        }
        Set<String> newExistSet = newExistDataSet.stream().map(DataMobile::getPhoneNum).collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(newExistDataSet)) {
            mobilePrimaryDbRepository.saveMobileList(newExistDataSet);
            oldSet.addAll(newExistSet);
        }

        Set<MobilePsiVo> existResult = oldSet.stream().map(MobilePsiVo::new).collect(Collectors.toSet());

        if (CollectionUtils.isEmpty(existResult)) {
//            req.setTaskState(TaskStateEnum.FAIL.getStateType());
//            examService.sendEndExamTask(req);
//            log.info("====================== FAIL ======================");
            log.error("samples size after exam is zero!");
            existResult.add(new MobilePsiVo(SM3Util.encrypt(UUID.randomUUID().toString().replace("-", ""))));
        }
        String jsonArrayStr = JSON.toJSONString(existResult);
        List<Map> maps = JSONObject.parseArray(jsonArrayStr, Map.class);
        String resourceName = "预处理生成资源" + SysConstant.HYPHEN_DELIMITER + req.getTaskId();
        DataResource dataResource = examService.generateTargetResource(maps, resourceName);
        if (dataResource == null) {
            req.setTaskState(TaskStateEnum.FAIL.getStateType());
            examService.sendEndExamTask(req);
            log.info("====================== FAIL ======================");
            log.error("generate target resource failed!");
        } else {
            req.setTaskState(TaskStateEnum.SUCCESS.getStateType());
            req.setTargetResourceId(dataResource.getResourceFusionId());
            examService.sendEndExamTask(req);
            log.info("====================== SUCCESS ======================");
        }

    }
}
