package com.primihub.biz.entity.sys.config;

import lombok.Data;

@Data
public class BaseGrpcClientConfig {
    private String grpcClientAddress;
    private Integer grpcClientPort;
    private Integer[] grpcServerPorts;
    private boolean useTls = false;
    private String trustCertFilePath;
    private String keyCertChainFile;
    private String keyFile;

}
