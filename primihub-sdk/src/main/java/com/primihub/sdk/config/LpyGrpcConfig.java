package com.primihub.sdk.config;

public class LpyGrpcConfig {
    private String address = "192.168.99.34";
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
