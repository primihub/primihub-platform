package com.primihub.application.controller.data;

import com.alibaba.fastjson.JSON;
import com.primihub.biz.constant.SysConstant;
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
@RequestMapping
@RestController
@Slf4j
public class ExamController {

    @Autowired
    private ExamService examService;

    @GetMapping("/examine/getExamTaskList")
    public BaseResultEntity<PageDataEntity<DataPirTaskVo>> getExamTaskList(DataExamTaskReq req) {
        return examService.getExamTaskList(req);
    }

    /**
     * for selection
     * @param req
     * @return
     */
    @GetMapping("/examine/examTaskList")
    public BaseResultEntity examTaskList(DataExamTaskReq req) {
        return examService.examTaskList(req);
    }


    @GetMapping(value = "/examine/getExamTaskDetail")
    public BaseResultEntity<DataPirTaskDetailVo> getExamTaskDetail(String taskId) {
        if (StringUtils.isBlank(taskId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "taskId");
        }
        return examService.getExamTaskDetail(taskId);
    }

    /**
     * 三：处理任务
     * 这里要 mock 两个数据源
     */
    @PostMapping(value = "/shareData/processExamTask")
    public BaseResultEntity processExamTask(@RequestBody DataExamReq dataExamReq) {
        log.info("process exam task");
        log.info(JSON.toJSONString(dataExamReq));
        return examService.processExamTask(dataExamReq);
    }

    /**
     * 第四步：结束任务
     */
    @PostMapping(value = "/shareData/finishExamTask")
    public BaseResultEntity finishExamTask(@RequestBody DataExamReq dataExamReq) {
        log.info(JSON.toJSONString(dataExamReq));
        return examService.finishExamTask(dataExamReq);
    }

}
