package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.constant.RedisKeyConstant;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.grpc.client.WorkGrpcClient;
import com.primihub.biz.util.FileUtil;
import java_worker.TaskStatus;
import java_worker.TaskStatusReply;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import primihub.rpc.Common;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataTaskMonitorService {

    @Resource(name="primaryStringRedisTemplate")
    private StringRedisTemplate primaryStringRedisTemplate;
    @Autowired
    private WorkGrpcClient workGrpcClient;

    public void continuouslyObtainTaskStatus(DataTask dataTask,Common.TaskContext taskBuild,int num,String path){
        boolean isContinue = true;
        primaryStringRedisTemplate.opsForSet().add(RedisKeyConstant.TASK_STATUS_LIST_KEY,String.format("%s-%s-%s",taskBuild.getTaskId(),taskBuild.getJobId(),taskBuild.getRequestId()));
        try {
            while (isContinue){
                TaskStatusReply taskStatusReply = workGrpcClient.run(o -> o.fetchTaskStatus(taskBuild));
                log.info(taskStatusReply.toString());
                if (taskStatusReply!=null && taskStatusReply.getTaskStatusList()!=null&&!taskStatusReply.getTaskStatusList().isEmpty()){
                    List<String> taskStatus = taskStatusReply.getTaskStatusList().stream().map(TaskStatus::getStatus).map(Enum::name).collect(Collectors.toList());
                    log.info("taskId:{} - num:{} - {}",taskBuild.getTaskId(),num,JSONObject.toJSONString(taskStatus));
                    if (taskStatus.contains(TaskStatus.StatusCode.FAIL.name())){
                        dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                        List<TaskStatus> taskStatusFails = taskStatusReply.getTaskStatusList().stream().filter(t -> t.getStatus() == TaskStatus.StatusCode.FAIL).collect(Collectors.toList());
                        StringBuilder sb = new StringBuilder();
                        for (TaskStatus taskStatusFail : taskStatusFails) {
                            log.info("taskid:{}-fail:{}",taskBuild.getTaskId(),taskStatusFail.toString());
                            sb.append(taskStatusFail.getParty()).append(":").append(taskStatusFail.getMessage()).append("\n");
                        }
                        dataTask.setTaskErrorMsg(sb.toString());
                        isContinue = false;
                    }else {
                        long success = taskStatus.stream().filter("SUCCESS"::equals).count();
                        log.info("num:{} - success:{}",num,success);
                        if (num <= success){
                            dataTask.setTaskState(TaskStateEnum.SUCCESS.getStateType());
                            if (StringUtils.isNotBlank(path)){
                                if (!FileUtil.isFileExists(path)){
                                    log.info("path:{} 不存在",path);
                                    dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                                    dataTask.setTaskErrorMsg("运行失败:无文件信息");
                                }
                            }
                            isContinue = false;
                        }
                    }
                    if (StringUtils.isNotBlank(path) && FileUtil.isFileExists(path)){
                        dataTask.setTaskState(TaskStateEnum.SUCCESS.getStateType());
                        isContinue = false;
                    }

                }
                Thread.sleep(1000L);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        primaryStringRedisTemplate.opsForSet().remove(RedisKeyConstant.TASK_STATUS_LIST_KEY,String.format("%s-%s-%s",taskBuild.getTaskId(),taskBuild.getJobId(),taskBuild.getRequestId()));
    }


}
