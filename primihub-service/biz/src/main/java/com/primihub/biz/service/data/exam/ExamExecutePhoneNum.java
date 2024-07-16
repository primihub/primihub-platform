package com.primihub.biz.service.data.exam;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.vo.lpy.MobilePsiVo;
import com.primihub.biz.service.data.ExamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ExamExecutePhoneNum implements ExamExecute {
    @Autowired
    private ExamService examService;

    @Override
    public void processExam(DataExamReq req) {
        log.info("process exam future task : phoneNum");

        String resourceName = new StringBuffer().append("预处理生成资源").append(SysConstant.HYPHEN_DELIMITER).append(req.getTaskId()).toString();
        /*
        rawSet
        oldSet, newSet
         */
        Set<String> rawSet = req.getFieldValueSet();
        Set<MobilePsiVo> rawDataMobileSet = rawSet.stream().map(MobilePsiVo::new).collect(Collectors.toSet());

        String jsonArrayStr = JSON.toJSONString(rawDataMobileSet);
        List<Map> maps = JSONObject.parseArray(jsonArrayStr, Map.class);
        DataResource dataResource = examService.generateTargetResource(maps, resourceName);
        if (dataResource == null) {
            req.setTaskState(TaskStateEnum.FAIL.getStateType());
            examService.sendEndExamTask(req);
            log.info("====================== FAIL");
        }
        req.setTaskState(TaskStateEnum.SUCCESS.getStateType());
        req.setTargetResourceId(dataResource.getResourceFusionId());
        examService.sendEndExamTask(req);
        log.info("====================== SUCCESS");
    }
}
