package com.yyds.biz.config.grpc;

import com.yyds.biz.config.base.BaseConfiguration;
import com.yyds.biz.grpc.server.TestGrpcService;
import com.yyds.biz.grpc.server.WorkGrpcService;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

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

}
