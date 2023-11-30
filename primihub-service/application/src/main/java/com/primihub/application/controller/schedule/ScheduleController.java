package com.primihub.application.controller.schedule;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.service.schedule.ScheduleService;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Api(value = "调度接口",tags = "调度接口")
@EnableScheduling
@RequestMapping("schedule")
@RestController
public class ScheduleController {

    @Autowired
    private ScheduleService scheduleService;

    @Scheduled(cron = "0 */10 * * * ?")
    @GetMapping("recallNotFinishedTask")
    public BaseResultEntity recallNotFinishedTask(){
        return scheduleService.recallNotFinishedTask();
    }

}
