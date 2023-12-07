package com.primihub.sdk.task.factory;

import com.primihub.sdk.constant.TaskConstant;
import com.primihub.sdk.task.Functional;
import com.primihub.sdk.task.cache.CacheService;
import com.primihub.sdk.task.dataenum.ModelTypeEnum;
import com.primihub.sdk.task.param.TaskParam;
import io.grpc.Channel;
import java_data_service.DataSetServiceGrpc;
import java_worker.TaskStatus;
import java_worker.TaskStatusReply;
import java_worker.VMNodeGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import primihub.rpc.Common;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractGRPCExecuteFactory {
    private final static Logger log = LoggerFactory.getLogger(AbstractGRPCExecuteFactory.class);
    public final static String TASK_STATUS_KEY = "ts:<taskId>:<jobId>";


    public Common.TaskContext assembleTaskContext(TaskParam taskParam){
        return Common.TaskContext.newBuilder()
                .setJobId(taskParam.getJobId())
                .setRequestId(taskParam.getRequestId())
                .setTaskId(taskParam.getTaskId())
                .build();
    }

    public Map<String, Common.Dataset> assembleModelDatasets(Map<String, Object> map, ModelTypeEnum modelType){
        Map<String, Common.Dataset> values = new HashMap<>();
        if (map.containsKey(TaskConstant.FTL_KEY_LABEL_DATASET)){
            values.put(TaskConstant.TASK_MODEL_LABEL_DATASET,Common.Dataset.newBuilder().putData(TaskConstant.TASK_MODEL_PARTY_DATASETS_KEY,map.get(TaskConstant.FTL_KEY_LABEL_DATASET).toString()).build());
        }
        if (map.containsKey(TaskConstant.FTL_KEY_GUEST_DATASET)){
            values.put(TaskConstant.TASK_MODEL_GUEST_DATASET,Common.Dataset.newBuilder().putData(TaskConstant.TASK_MODEL_PARTY_DATASETS_KEY,map.get(TaskConstant.FTL_KEY_GUEST_DATASET).toString()).build());
        }
        if (map.containsKey(TaskConstant.FTL_KEY_ARBITER_DATASET)){
            values.put(TaskConstant.TASK_MODEL_ARBITER_DATASET,Common.Dataset.newBuilder().putData(TaskConstant.TASK_MODEL_PARTY_DATASETS_KEY,map.get(TaskConstant.FTL_KEY_ARBITER_DATASET).toString()).build());
        }
        if (ModelTypeEnum.HETERO_LR.equals(modelType) && map.containsKey("method") && map.get("method").equals("CKKS")){
            values.put(TaskConstant.TASK_MODEL_DAVID_DATASET,Common.Dataset.newBuilder().putData(TaskConstant.TASK_MODEL_DAVID_DATASET,map.get(TaskConstant.FTL_KEY_ARBITER_DATASET).toString()).build());
        }
        return values;
    }

    public Map<String, Common.Dataset> assembleModelMpcDatasets(List<String> resourceIds){
        Map<String, Common.Dataset> values = new HashMap<>();
        for (int i = 0; i < resourceIds.size(); i++) {
            values.put(TaskConstant.TASK_MODEL_MPC_DATASET+i,Common.Dataset.newBuilder().putData(TaskConstant.TASK_MPC_PARTY_DATASETS_KEY,resourceIds.get(i)).build());
        }
        return values;
    }

    public <Result> Result runVMNodeGrpc(Functional<VMNodeGrpc.VMNodeBlockingStub,Result> functional, Channel channel){
        Result result = null;
        int i = 0;
        while(true){
            i++;
            try {
                VMNodeGrpc.VMNodeBlockingStub vMNodeBlockingStub =VMNodeGrpc.newBlockingStub(channel);
                result = functional.run(vMNodeBlockingStub);
                break;
            }catch (Exception e){
                e.printStackTrace();
                if (i > 3) {
                    log.info("Over three anomalies thrown");
                    throw e;
                }
            }
        }
        return result;
    }
    public <Result> Result runDataServiceGrpc(Functional<DataSetServiceGrpc.DataSetServiceBlockingStub,Result> functional, Channel channel){
        Result result = null;
        int i = 0;
        while (true){
            i++;
            try {
                DataSetServiceGrpc.DataSetServiceBlockingStub dataServiceBlockingStub = DataSetServiceGrpc.newBlockingStub(channel).withDeadlineAfter(3, TimeUnit.SECONDS);
                result = functional.run(dataServiceBlockingStub);
                break;
            }catch (Exception e){
                e.printStackTrace();
                if (i>3) {
                    log.info("Over three anomalies thrown");
                    throw e;
                }
            }
        }
        return result;

    }

    public void continuouslyObtainTaskStatus(Channel channel,Common.TaskContext taskBuild, TaskParam param, int partyCount) {
        boolean isContinue = true;
        String key = TASK_STATUS_KEY.replace("<taskId>",taskBuild.getTaskId()).replace("<jobId>",taskBuild.getJobId());
        CacheService cacheService = getCacheService();
        try {
            while (isContinue){
                TaskStatusReply taskStatusReply  = runVMNodeGrpc(o -> o.fetchTaskStatus(taskBuild),channel);
                if (taskStatusReply!=null && taskStatusReply.getTaskStatusList()!=null&&!taskStatusReply.getTaskStatusList().isEmpty()){
                    List<String> taskStatus = taskStatusReply.getTaskStatusList().stream().filter(t->t.getParty()!=null && !"".equals(t.getParty())).map(TaskStatus::getStatus).map(Enum::name).collect(Collectors.toList());
                    if (!taskStatus.isEmpty()){
                        log.info(taskStatusReply.toString());
                        List<String> getList = cacheService.get(key);
                        getList.addAll(taskStatus);
                        cacheService.put(key,getList);
                        if (taskStatus.contains(TaskStatus.StatusCode.FAIL.name())||taskStatus.contains(TaskStatus.StatusCode.NONEXIST.name())){
                            param.setSuccess(false);
                            List<TaskStatus> taskStatusFails = taskStatusReply.getTaskStatusList().stream().filter(t -> t.getStatus() == TaskStatus.StatusCode.FAIL).collect(Collectors.toList());
                            StringBuilder sb = new StringBuilder();
                            for (TaskStatus taskStatusFail : taskStatusFails) {
                                log.info("taskid:{} - requestId:{} -fail:{}",param.getTaskId(),param.getRequestId(),taskStatusFail.toString());
                                sb.append(taskStatusFail.getParty()).append(":").append(taskStatusFail.getMessage()).append("\n");
                            }
                            param.setError(sb.toString());
                            isContinue = false;
                        }
                    }
                    long success = getNumberOfSuccessfulTasks(key,cacheService);
                    log.info("taskid:{} - requestId:{} - num:{} - success:{}",param.getTaskId(),param.getRequestId(),partyCount,success);
                    if (partyCount <= success){
                        isContinue = false;
                    }
                }
                Thread.sleep(1000L);
            }
        }catch (Exception e){
            param.setSuccess(false);
            param.setError(e.getMessage());
            e.printStackTrace();
        }
        cacheService.invalidate(key);
        param.setEnd(true);
    }


    public Long getNumberOfSuccessfulTasks(String key,CacheService cacheService){
        List<String> range = cacheService.get(key);
        Integer count = range.size();
        if (count==0){
            return 0L;
        }
        Map<String, Long> statusMap = range.stream().collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));
        if (statusMap.containsKey(TaskStatus.StatusCode.SUCCESS.name())){
            return statusMap.get(TaskStatus.StatusCode.SUCCESS.name());
        }
        return 0L;
    }


    abstract public void execute(Channel channel,TaskParam taskParam);

    abstract public CacheService getCacheService();

    abstract public void setCacheService(CacheService cacheService);
}