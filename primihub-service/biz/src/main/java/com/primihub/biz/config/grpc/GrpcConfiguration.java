package com.primihub.biz.config.grpc;

import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.sdk.task.TaskHelper;
import com.primihub.sdk.task.cache.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Slf4j
@Configuration
public class GrpcConfiguration {

    @Bean(name="taskHelper")
    public TaskHelper initTaskHelper(@Qualifier("baseConfiguration") BaseConfiguration baseConfiguration) throws Exception {
        return TaskHelper.getInstance(baseConfiguration.getGrpcClient());
    }

    @Bean(name="cacheService")
    @DependsOn(value = {"taskHelper"})
    public CacheService getCacheService() {
        return TaskHelper.getInstance().getCacheService();
    }

}
