package com.primihub.sdk.grpc.channel;

import com.primihub.sdk.config.GrpcClientConfig;
import io.grpc.Channel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NegotiationType;
import io.grpc.netty.NettyChannelBuilder;
import io.netty.handler.ssl.SslContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class GrpcChannel {
    private final static Logger log = LoggerFactory.getLogger(GrpcChannel.class);

    private Channel channel;

    public GrpcChannel(GrpcClientConfig grpcClientConfig) throws Exception {
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
}
