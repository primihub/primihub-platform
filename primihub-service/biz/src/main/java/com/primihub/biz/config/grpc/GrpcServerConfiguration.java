//package com.primihub.biz.config.grpc;
//
//import com.primihub.biz.grpc.server.DataGrpcService;
//import com.primihub.biz.grpc.server.TestGrpcService;
//import com.primihub.biz.grpc.server.WorkGrpcService;
//import com.primihub.biz.config.base.BaseConfiguration;
//import io.grpc.Server;
//import io.grpc.ServerBuilder;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//import java.io.IOException;
//
//@Slf4j
//@Configuration
//public class GrpcServerConfiguration {
//
//    @Bean(name="grpcServer")
//    public Server initGrpcServer(BaseConfiguration baseConfiguration){
//        Server server=ServerBuilder.forPort(baseConfiguration.getGrpcServerPort())
//                .addService(new TestGrpcService())
//                .addService(new WorkGrpcService())
//                .addService(new DataGrpcService())
//                .build();
//        try {
//            server.start();
//        } catch (IOException e) {
//            log.error("初始化GRPC端口失败",e);
//        }
//        return server;
//    }
//
//}
