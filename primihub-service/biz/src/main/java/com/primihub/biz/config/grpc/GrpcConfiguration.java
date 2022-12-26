package com.primihub.biz.config.grpc;

import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.entity.sys.config.BaseGrpcClientConfig;
import io.grpc.Channel;
import io.grpc.ChannelCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.NettySslContextChannelCredentials;
import io.netty.handler.ssl.SslContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.SSLException;
import java.io.File;

@Slf4j
@Configuration
public class GrpcConfiguration {

    @Bean(name="grpcClientChannel")
    public Channel initGrpcClientChannel(BaseConfiguration baseConfiguration) throws SSLException {
        BaseGrpcClientConfig grpcClient = baseConfiguration.getGrpcClient();
        if (grpcClient.isUseTls()){
            log.info("grpc Open tls");
            if (StringUtils.isBlank(grpcClient.getTrustCertFilePath()) || StringUtils.isBlank(grpcClient.getKeyCertChainFile()) || StringUtils.isBlank(grpcClient.getKeyFile())){
                log.info("grpc tls : Certificate path open default general connection missing");
                return getDefaultTypeChannel(grpcClient.getGrpcClientAddress(),grpcClient.getGrpcClientPort());
            }
            File trustCertFile = new File(grpcClient.getTrustCertFilePath());
            File keyCertChainFile = new File(grpcClient.getKeyCertChainFile());
            File keyFile = new File(grpcClient.getKeyFile());
            if (!trustCertFile.exists()){
                log.info("grpc tls : The certificate trustCertFile does not exist. open default general connection");
                return getDefaultTypeChannel(grpcClient.getGrpcClientAddress(),grpcClient.getGrpcClientPort());
            }
            if (!keyCertChainFile.exists()){
                log.info("grpc tls : The certificate keyCertChainFile does not exist. open default general connection");
                return getDefaultTypeChannel(grpcClient.getGrpcClientAddress(),grpcClient.getGrpcClientPort());
            }
            if (!keyFile.exists()){
                log.info("grpc tls : The certificate keyFile does not exist. open default general connection");
                return getDefaultTypeChannel(grpcClient.getGrpcClientAddress(),grpcClient.getGrpcClientPort());
            }
            SslContext sslContext = GrpcSslContexts.forClient()
                    .trustManager(trustCertFile)
                    .keyManager(keyCertChainFile,keyFile)
                    .build();
            return NettyChannelBuilder
                    .forAddress(grpcClient.getGrpcClientAddress(),grpcClient.getGrpcClientPort())
                    .maxInboundMessageSize(Integer.MAX_VALUE)
                    .maxInboundMetadataSize(Integer.MAX_VALUE)
                    .negotiationType(NegotiationType.TLS)
                    .sslContext(sslContext)
                    .build();
        }
        return getDefaultTypeChannel(grpcClient.getGrpcClientAddress(),grpcClient.getGrpcClientPort());
    }


    private Channel getDefaultTypeChannel(String grpcClientAddress,Integer grpcClientPort){
        return ManagedChannelBuilder
                .forAddress(grpcClientAddress, grpcClientPort)
                .maxInboundMessageSize(Integer.MAX_VALUE)
                .maxInboundMetadataSize(Integer.MAX_VALUE)
                .usePlaintext()
                .build();
    }

}
