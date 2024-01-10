package com.primihub.application.controller.stream;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.service.data.DataTaskService;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;



@Api(value = "流传输接口",tags = "流传输接口")
@RequestMapping("stream")
@RestController
@Slf4j
public class StreamController {

    @Autowired
    private DataTaskService dataTaskService;

    /**
     * 用于创建连接
     */
    @GetMapping(value = "sseTaskConnect/{taskId}",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter connect(@PathVariable String taskId) {
//        return dataTaskService.connectSseTask1(taskId);
        return dataTaskService.connectSseTask(taskId);
    }

//    @GetMapping(value = "send/{taskId}")
//    public String send(@PathVariable String taskId){
//        dataTaskService.send(taskId);
//        return "su";
//    }

    /**
     * 关闭连接
     */
    @GetMapping("sseTaskClose/{taskId}")
    public BaseResultEntity close(@PathVariable("taskId") String taskId) {
        if (StringUtils.isBlank(taskId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        dataTaskService.removeSseTask(taskId);
        return BaseResultEntity.success();
    }
}
