package com.primihub.sdk.task.factory;

import com.primihub.sdk.task.cache.CacheService;
import com.primihub.sdk.task.param.TaskParam;
import io.grpc.Channel;
import java_worker.KillTaskRequest;
import java_worker.KillTaskResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import primihub.rpc.Common;

public class AbstractKillGRPCExecute extends AbstractGRPCExecuteFactory {

    private final static Logger log = LoggerFactory.getLogger(AbstractKillGRPCExecute.class);

    private CacheService cacheService;


    @Override
    public CacheService getCacheService() {
        return cacheService;
    }

    @Override
    public void setCacheService(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public void execute(Channel channel, TaskParam taskParam) {
        try {
            Common.TaskContext taskContext = assembleTaskContext(taskParam);
            KillTaskRequest request = KillTaskRequest.newBuilder().setTaskInfo(taskContext).setExecutor(KillTaskRequest.ExecutorType.CLIENT).build();
            log.info("kill start request:{}",request.toString());
            KillTaskResponse response = runVMNodeGrpc(o -> o.killTask(request), channel);
            if (response.getRetCode() == Common.retcode.SUCCESS){
                taskParam.setSuccess(true);
            }else {
                taskParam.setSuccess(false);
                taskParam.setError(response.getMsgInfoBytes().toStringUtf8());
            }
            log.info("kill end response:{}",response.toString());
        }catch (Exception e){
            taskParam.setSuccess(false);
            taskParam.setError(e.getMessage());
            log.info("kill grpc Exception:{}", e.getMessage());
        }

    }
}