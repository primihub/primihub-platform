package com.primihub.sdk.config;

public class LpyGrpcConfig {
    private String address = "172.21.1.64";
    private Integer port = 50051;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
