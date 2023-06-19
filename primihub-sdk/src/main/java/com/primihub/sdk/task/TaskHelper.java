package com.primihub.sdk.task;

import com.primihub.sdk.config.GrpcClientConfig;
import com.primihub.sdk.task.annotation.TaskTypeExample;
import com.primihub.sdk.task.cache.CacheService;
import com.primihub.sdk.task.factory.AbstractGRPCExecuteFactory;
import com.primihub.sdk.task.param.TaskParam;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * node task
 */
public class TaskHelper {

    private final static  Logger log = LoggerFactory.getLogger(TaskHelper.class);


    private static Map<Class<?>, AbstractGRPCExecuteFactory> EXECUTE_MAP = new HashMap<>();

    private static class TaskHelperInstance{
        final static TaskHelper INSTANCE = new TaskHelper();
    }

    public static TaskHelper getInstance(GrpcClientConfig grpcClientConfig) throws Exception {
        TaskHelperInstance.INSTANCE.grpcClientConfig = grpcClientConfig;
        TaskHelperInstance.INSTANCE.initGrpcClientChannel();
        TaskHelperInstance.INSTANCE.initExecuteFactory();
        return TaskHelperInstance.INSTANCE;
    }

    public static TaskHelper getInstance(){
        return TaskHelperInstance.INSTANCE;
    }

    private GrpcClientConfig grpcClientConfig;

    private Channel channel;

    private CacheService cacheService = null;

    public CacheService getCacheService() {
        return cacheService;
    }

    public Channel getChannel() {
        return channel;
    }
    public void initGrpcClientChannel() throws Exception {
        if (grpcClientConfig == null || grpcClientConfig.getAddress()==null || "".equals(grpcClientConfig.getAddress()) || grpcClientConfig.getPort() == null){
            throw new Exception("grpc config null");
        }
        if (grpcClientConfig.isUseTls()){
            log.info("grpc Open tls");
            if ("".equals(grpcClientConfig.getTrustCertFilePath()) || "".equals(grpcClientConfig.getKeyCertChainFile()) || "".equals(grpcClientConfig.getKeyFile())){
                log.info("grpc tls : Certificate path open default general connection missing");
                getDefaultTypeChannel(grpcClientConfig.getAddress(), grpcClientConfig.getPort());
            }
            File trustCertFile = new File(grpcClientConfig.getTrustCertFilePath());
            File keyCertChainFile = new File(grpcClientConfig.getKeyCertChainFile());
            File keyFile = new File(grpcClientConfig.getKeyFile());
            if (!trustCertFile.exists()){
                log.info("grpc tls : The certificate trustCertFile does not exist. open default general connection");
                getDefaultTypeChannel(grpcClientConfig.getAddress(), grpcClientConfig.getPort());
            }
            if (!keyCertChainFile.exists()){
                log.info("grpc tls : The certificate keyCertChainFile does not exist. open default general connection");
                getDefaultTypeChannel(grpcClientConfig.getAddress(), grpcClientConfig.getPort());
            }
            if (!keyFile.exists()){
                log.info("grpc tls : The certificate keyFile does not exist. open default general connection");
                getDefaultTypeChannel(grpcClientConfig.getAddress(), grpcClientConfig.getPort());
            }
            SslContext sslContext = GrpcSslContexts.forClient()
                    .trustManager(trustCertFile)
                    .keyManager(keyCertChainFile,keyFile)
                    .build();
            channel = NettyChannelBuilder
                    .forAddress(grpcClientConfig.getAddress(), grpcClientConfig.getPort())
                    .maxInboundMessageSize(Integer.MAX_VALUE)
                    .maxInboundMetadataSize(Integer.MAX_VALUE)
                    .negotiationType(NegotiationType.TLS)
                    .sslContext(sslContext)
                    .build();
        }
        getDefaultTypeChannel(grpcClientConfig.getAddress(), grpcClientConfig.getPort());
    }


    private void getDefaultTypeChannel(String grpcClientAddress,Integer grpcClientPort){
        channel = ManagedChannelBuilder
                .forAddress(grpcClientAddress, grpcClientPort)
                .maxInboundMessageSize(Integer.MAX_VALUE)
                .maxInboundMetadataSize(Integer.MAX_VALUE)
                .usePlaintext()
                .build();
    }

    private void initExecuteFactory(){
        String cacheType = this.grpcClientConfig.getCacheType();
        log.info("cacheType : {}",cacheType);
        ServiceLoader<CacheService> cacheServices = ServiceLoader.load(CacheService.class);
        for (CacheService service : cacheServices) {
            if (service.getType().equals(cacheType)){
                cacheService = service;
                break;
            }
        }
        if (cacheService==null){
            throw new NullPointerException("cacheService is not null");
        }
        ServiceLoader<AbstractGRPCExecuteFactory> executeFactorys = ServiceLoader.load(AbstractGRPCExecuteFactory.class);
        for (AbstractGRPCExecuteFactory execute : executeFactorys) {
            execute.setCacheService(cacheService);
            EXECUTE_MAP.put(execute.getClass(),execute);
        }
    }

    public void submit(TaskParam taskParam){
        submit(this.channel,taskParam);
    }

    public void submit(Channel channel, TaskParam taskParam){
        TaskTypeExample annotation = taskParam.getTaskContentParam().getClass().getAnnotation(TaskTypeExample.class);
        if (annotation ==null || annotation.value()==null){
            throw new NullPointerException("TaskContentParam 缺少TaskTypeExample注解或value为空");
        }
        if (!EXECUTE_MAP.containsKey(annotation.value())){
            log.info("taskParam:{}",taskParam.toString());
            throw new NullPointerException("TaskContentParam 实例对象为null");
        }
        EXECUTE_MAP.get(annotation.value()).execute(channel,taskParam);
        if (taskParam.getOpenGetStatus()){
            taskParam.setEnd(true);
        }
    }

    public void continuouslyObtainTaskStatus(Channel channel, TaskParam taskParam){
        TaskTypeExample annotation = taskParam.getTaskContentParam().getClass().getAnnotation(TaskTypeExample.class);
        if (annotation ==null || annotation.value()==null){
            throw new NullPointerException("TaskContentParam 缺少TaskTypeExample注解或value为空");
        }
        if (!EXECUTE_MAP.containsKey(annotation.value())){
            log.info("taskParam:{}",taskParam.toString());
            throw new NullPointerException("TaskContentParam 实例对象为null");
        }
        AbstractGRPCExecuteFactory abstractGRPCExecuteFactory = EXECUTE_MAP.get(annotation.value());
        abstractGRPCExecuteFactory.continuouslyObtainTaskStatus(channel,abstractGRPCExecuteFactory.assembleTaskContext(taskParam),taskParam,taskParam.getPartyCount());
    }

}
