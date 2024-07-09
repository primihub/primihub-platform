package com.primihub.biz.service.data.exam;

import com.primihub.biz.entity.data.req.DataExamReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExamExecutePhoneNum implements ExamExecute {
    @Override
    public void processExam(DataExamReq req) {
        log.info("process exam future task : phoneNum");

        /*String resourceName = new StringBuffer().append("预处理生成资源").append(SysConstant.HYPHEN_DELIMITER).append(req.getTaskId()).toString();
        DataResource dataResource = generateTargetResource(maps, resourceName);
        if (dataResource == null) {
            req.setTaskState(TaskStateEnum.FAIL.getStateType());
            sendEndExamTask(req);
            log.info("====================== FAIL");
        }
        req.setTaskState(TaskStateEnum.SUCCESS.getStateType());
        req.setTargetResourceId(dataResource.getResourceFusionId());
        sendEndExamTask(req);
        log.info("====================== SUCCESS");*/
    }
}
