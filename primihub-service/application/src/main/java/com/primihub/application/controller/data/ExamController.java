package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.req.DataExamTaskReq;
import com.primihub.biz.entity.data.vo.DataPirTaskDetailVo;
import com.primihub.biz.entity.data.vo.DataPirTaskVo;
import com.primihub.biz.service.data.ExamService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 预审核管理
 */
@RequestMapping("/examine")
@RestController
@Slf4j
public class ExamController {

    @Autowired
    private ExamService examService;
    @GetMapping("/getExamTaskList")
    public BaseResultEntity<PageDataEntity<DataPirTaskVo>> getExamTaskList(DataExamTaskReq req){
        return examService.getExamTaskList(req);
    }

    /**
     * 第一步：发起任务
     * @param dataExamReq
     * @return
     */
    @PostMapping(value = "/submitExamTask", consumes = MediaType.APPLICATION_JSON_VALUE)
    public BaseResultEntity submitExamTask(@RequestBody DataExamReq dataExamReq) {
        if (StringUtils.isBlank(dataExamReq.getResourceId())){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        if (StringUtils.isBlank(dataExamReq.getTaskName())){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskName");
        }
        if (StringUtils.isBlank(dataExamReq.getTargetOrganId())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"targetOrganId");
        }
        return examService.submitExamTask(dataExamReq);
    }

    @GetMapping(value = "/getExamTaskDetail")
    public BaseResultEntity<DataPirTaskDetailVo> getExamTaskDetail(String taskId){
        if (StringUtils.isBlank(taskId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return examService.getExamTaskDetail(taskId);
    }

    /**
     * 第二步：处理任务
     */
    @PostMapping(value = "/processExamTask")
    public BaseResultEntity processExamTask(@RequestBody DataExamReq dataExamReq) {
        return examService.processExamTask(dataExamReq);
    }

    /**
     * 第三步：结束任务
     */
    @PostMapping(value = "/finishExamTask")
    public BaseResultEntity finishExamTask(@RequestBody DataExamReq dataExamReq) {
        return examService.finishExamTask(dataExamReq);
    }

}
