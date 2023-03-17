package com.primihub.biz.service.data;

import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.constant.DataConstant;
import com.primihub.biz.constant.RedisKeyConstant;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.util.FileUtil;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import java_data_service.ClientContext;
import java_data_service.NodeEventReply;
import java_data_service.NodeServiceGrpc;
import java_data_service.TaskContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataTaskMonitorService {

    @Autowired
    private BaseConfiguration baseConfiguration;
    @Resource(name="primaryStringRedisTemplate")
    private StringRedisTemplate primaryStringRedisTemplate;

    @PostConstruct
    public void init(){
        log.info("open server node event");
        if (baseConfiguration.getGrpcClient()!=null && baseConfiguration.getGrpcClient().getGrpcServerPorts()!=null){
            Integer[] serverPorts = baseConfiguration.getGrpcClient().getGrpcServerPorts();
            if (serverPorts!=null){
                for (Integer serverPort : serverPorts) {
                    initGrpcServerChannel(serverPort);
                }
            }
        }
    }

    public void initGrpcServerChannel(Integer serverPort){
        String grpcClientAddress = baseConfiguration.getGrpcClient().getGrpcClientAddress();
        Integer grpcClientPort = baseConfiguration.getGrpcClient().getGrpcClientPort();
        Channel channel= ManagedChannelBuilder
                .forAddress(grpcClientAddress,serverPort)
                .usePlaintext()
                .build();
        ClientContext context= ClientContext.newBuilder().setClientId(grpcClientPort.toString()).build();
        NodeServiceGrpc.NodeServiceStub stub=NodeServiceGrpc.newStub(channel).withDeadlineAfter(30,TimeUnit.MINUTES);
        stub.subscribeNodeEvent(context, new StreamObserver<NodeEventReply>(){
            @Override
            public void onNext(NodeEventReply nodeEventReply) {
                log.info("on Next:{}-{}:{}",grpcClientAddress,serverPort,nodeEventReply);
                if (nodeEventReply!=null && nodeEventReply.getTaskStatus()!=null && nodeEventReply.getTaskResult().getTaskContext()!=null){
                    String status = nodeEventReply.getTaskStatus().getStatus();
                    TaskContext taskContext = nodeEventReply.getTaskStatus().getTaskContext();
                    String taskId = taskContext.getTaskId();
                    if (StringUtils.isNotBlank(taskId)){
                        String jobId = taskContext.getJobId();
                        String key = RedisKeyConstant.TASK_STATUS_KEY.replace("<taskId>",taskId).replace("<jobId>",jobId);
                        primaryStringRedisTemplate.opsForList().rightPush(key,status);
                        primaryStringRedisTemplate.expire(key,12, TimeUnit.HOURS);
                    }
                }
            }

            @Override
            public void onError(Throwable throwable) {
                log.info("on Error:{}-{}",grpcClientAddress,serverPort);
                initGrpcServerChannel(serverPort);
            }

            @Override
            public void onCompleted() {
                log.info("complete:{}-{}",grpcClientAddress,serverPort);
            }
        });
    }

    /**
     * 阻塞获取任务状态
     * @param taskId        任务ID
     * @param jobId         JOBID
     * @param num           几个成功
     * @param timeout       超时时间
     * @return
     */
    public boolean blockingAccessToTaskStatus(String taskId,String jobId,int num,Long timeout,String path){
        long start = System.currentTimeMillis();
        long timeConsuming = 0L;
//        int judgmentFile = 0;
        while (timeConsuming<=timeout){
            try {
                if (StringUtils.isNotBlank(path)){
                    timeConsuming = System.currentTimeMillis() - start;
                    // 五分钟判断一下文件是否存在
//                    if (judgmentFile!=(timeConsuming/DataConstant.GRPC_FILE_TIMEOUT)){
//                        judgmentFile = Long.valueOf(timeConsuming/DataConstant.GRPC_FILE_TIMEOUT).intValue();
                    if (FileUtil.isFileExists(path)){
                        return true;
                    }
//                    }
                }
                int numberOfSuccessfulTasks = getNumberOfSuccessfulTasks(taskId, jobId);
                log.info("get into wait for start:{} - timeout:{} - timeConsuming:{} - taskId:{} - jobId:{} - numberOfSuccessfulTasks:{}",start,timeout,timeConsuming,taskId,jobId,numberOfSuccessfulTasks);
                if (numberOfSuccessfulTasks<0){
                    return false;
                }else {
                    if (num <= numberOfSuccessfulTasks){
                        return true;
                    }
                }

                Thread.sleep(5000L);

            }catch (Exception e){
                e.printStackTrace();
            }
        }
        log.info("Automatically exit after exceeding the maximum timeout start:{} end:{} consuming:{} taskId:{} - jobId:{} ",start,System.currentTimeMillis(),timeout,taskId,jobId);
        return false;
    }


    /**
     * 获取任务的成功数量
     * @param taskId
     * @param jobId
     * @return
     */
    public int getNumberOfSuccessfulTasks(String taskId,String jobId){
        String key = RedisKeyConstant.TASK_STATUS_KEY.replace("<taskId>",taskId).replace("<jobId>",jobId);
        Long count = primaryStringRedisTemplate.opsForList().size(key);
        if (count==null || count==0L)
            return 0;
        List<String> range = primaryStringRedisTemplate.opsForList().range(key, 0L, count);
        Map<String, Long> statusMap = range.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        if (statusMap.containsKey("FAILED") || statusMap.containsKey("FAIL"))
            return -1;
        if (statusMap.containsKey("SUCCESS")){
            return statusMap.get("SUCCESS").intValue();
        }
        return 0;
    }


    public void verifyWhetherTheTaskIsSuccessfulAgain(DataTask dataTask,String jobId, int num, String path){
        blockingAccessToTaskStatus(dataTask.getTaskIdName(), jobId, num, DataConstant.GRPC_SERVER_TIMEOUT,path);
        if (StringUtils.isNotBlank(path)){
            if (FileUtil.isFileExists(path)){
                log.info("path:{} 存在",path);
                dataTask.setTaskState(TaskStateEnum.SUCCESS.getStateType());
            }else {
                log.info("path:{} 不存在",path);
                dataTask.setTaskState(TaskStateEnum.FAIL.getStateType());
                dataTask.setTaskErrorMsg("运行失败:无文件信息");
            }
        }


    }


}
