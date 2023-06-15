package com.primihub.sdk.config;

import java.util.ArrayList;
import java.util.List;

public class GrpcClientConfig {
    private String address;
    private Integer port;
    private boolean useTls = false;
    private String trustCertFilePath;
    private String keyCertChainFile;
    private String keyFile;
    private String cacheType = "CaffeineCacheService";
    private List<String> customPackagePath;

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

    public boolean isUseTls() {
        return useTls;
    }

    public void setUseTls(boolean useTls) {
        this.useTls = useTls;
    }

    public String getTrustCertFilePath() {
        return trustCertFilePath;
    }

    public void setTrustCertFilePath(String trustCertFilePath) {
        this.trustCertFilePath = trustCertFilePath;
    }

    public String getKeyCertChainFile() {
        return keyCertChainFile;
    }

    public void setKeyCertChainFile(String keyCertChainFile) {
        this.keyCertChainFile = keyCertChainFile;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public void setKeyFile(String keyFile) {
        this.keyFile = keyFile;
    }

    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    public List<String> getCustomPackagePath() {
        if (this.customPackagePath == null){
            return new ArrayList<>();
        }
        return customPackagePath;
    }

    public void setCustomPackagePath(List<String> customPackagePath) {
        this.customPackagePath = customPackagePath;
    }
}
