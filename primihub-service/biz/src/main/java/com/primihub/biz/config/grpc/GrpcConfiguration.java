package com.primihub.biz.config.grpc;

import com.primihub.biz.config.base.BaseConfiguration;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class GrpcConfiguration {

    @Bean(name="grpcClientChannel")
    public Channel initGrpcClientChannel(BaseConfiguration baseConfiguration){
        return ManagedChannelBuilder
                .forAddress(baseConfiguration.getGrpcClientAddress(),baseConfiguration.getGrpcClientPort())
                .usePlaintext()
                .build();
    }

    @Bean(name="grpcDataSetClientChannel")
    public Channel initGrpcDataSetClientChannel(BaseConfiguration baseConfiguration){
        return ManagedChannelBuilder
                .forAddress(baseConfiguration.getGrpcDataSetClientAddress(),baseConfiguration.getGrpcDataSetClientPort())
                .usePlaintext()
                .build();
    }
}
