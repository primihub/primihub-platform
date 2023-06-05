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
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataTaskMonitorService {

    @Resource(name="primaryStringRedisTemplate")
    private StringRedisTemplate primaryStringRedisTemplate;
    @Autowired
    private WorkGrpcClient workGrpcClient;

    private final static Long FILE_VERIFICATION_TIME = 60000L;

    public void continuouslyObtainTaskStatus(DataTask dataTask,Common.TaskContext taskBuild,int num,String path){
        boolean isContinue = true;
        primaryStringRedisTemplate.opsForSet().add(RedisKeyConstant.TASK_STATUS_LIST_KEY,String.format("%s-%s-%s",taskBuild.getTaskId(),taskBuild.getJobId(),taskBuild.getRequestId()));
        try {
            String key = RedisKeyConstant.TASK_STATUS_KEY.replace("<taskId>",taskBuild.getTaskId()).replace("<jobId>",taskBuild.getJobId());
            while (isContinue){
                TaskStatusReply taskStatusReply = workGrpcClient.run(o -> o.fetchTaskStatus(taskBuild));
//                log.info(taskStatusReply.toString());
                if (taskStatusReply!=null && taskStatusReply.getTaskStatusList()!=null&&!taskStatusReply.getTaskStatusList().isEmpty()){
                    List<String> taskStatus = taskStatusReply.getTaskStatusList().stream().filter(t->StringUtils.isNotBlank(t.getParty())).map(TaskStatus::getStatus).map(Enum::name).collect(Collectors.toList());
                    if (!taskStatus.isEmpty()){
//                        log.info("taskId:{} - num:{} - {}",taskBuild.getTaskId(),num,JSONObject.toJSONString(taskStatus));
                        primaryStringRedisTemplate.opsForList().rightPushAll(key,taskStatus);
                        primaryStringRedisTemplate.expire(key,12, TimeUnit.HOURS);
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
                            long success = getNumberOfSuccessfulTasks(key);
                            log.info("num:{} - success:{}",num,success);
                            if (num <= success){
                                dataTask.setTaskState(TaskStateEnum.SUCCESS.getStateType());
                                // TODO 循环1分钟 因在k8s中文件同步存在一定的延迟性。物理机可以注释【一】打开【二】
                                // ------------一---------------------
                                long start = System.currentTimeMillis();
                                while (true){
                                    if (StringUtils.isNotBlank(path)){
                                        if (FileUtil.isFileExists(path)){
                                            log.info("{} - 存在了-退出",path);
                                            break;
                                        }
                                    }
                                    if ((System.currentTimeMillis() - start)>FILE_VERIFICATION_TIME){
                                        log.info("path:{} 不存在",path);
                                        dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                                        dataTask.setTaskErrorMsg("运行失败:无文件信息");
                                        break;
                                    }
                                }
                                // ------------二---------------------
//                                if (StringUtils.isNotBlank(path)){
//                                    if (!FileUtil.isFileExists(path)){
//                                        log.info("path:{} 不存在",path);
//                                        dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
//                                        dataTask.setTaskErrorMsg("运行失败:无文件信息");
//                                    }
//                                }
                                isContinue = false;
                            }
                        }
                        if (StringUtils.isNotBlank(path) && FileUtil.isFileExists(path)){
                            dataTask.setTaskState(TaskStateEnum.SUCCESS.getStateType());
                            isContinue = false;
                        }
                    }
                }
                Thread.sleep(1000L);
            }
            primaryStringRedisTemplate.delete(key);
        }catch (Exception e){
            e.printStackTrace();
        }
        primaryStringRedisTemplate.opsForSet().remove(RedisKeyConstant.TASK_STATUS_LIST_KEY,String.format("%s-%s-%s",taskBuild.getTaskId(),taskBuild.getJobId(),taskBuild.getRequestId()));
    }

    public long getNumberOfSuccessfulTasks(String key){
        Long count = primaryStringRedisTemplate.opsForList().size(key);
        if (count==null || count==0L){
            return 0L;
        }
        List<String> range = primaryStringRedisTemplate.opsForList().range(key, 0L, count);
        Map<String, Long> statusMap = range.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        if (statusMap.containsKey("SUCCESS")){
            return statusMap.get("SUCCESS");
        }
        return 0L;
    }


}
