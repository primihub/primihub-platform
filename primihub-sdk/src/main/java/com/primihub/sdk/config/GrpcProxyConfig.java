package com.primihub.sdk.config;

public class GrpcProxyConfig {
    private ProxyConfig inProxy;
    private ProxyConfig outProxy;

    public ProxyConfig getInProxy() {
        return inProxy;
    }

    public void setInProxy(ProxyConfig inProxy) {
        this.inProxy = inProxy;
    }

    public ProxyConfig getOutProxy() {
        return outProxy;
    }

    public void setOutProxy(ProxyConfig outProxy) {
        this.outProxy = outProxy;
    }
}
