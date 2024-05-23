package com.primihub.biz.util;

import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.sdk.config.GrpcProxyConfig;
import com.primihub.sdk.config.ProxyConfig;
import org.apache.commons.lang.StringUtils;

public class BaseUtil {
    public static boolean nodeHasGRPCInProxy(BaseConfiguration baseConfiguration) {
        GrpcProxyConfig grpcProxy = baseConfiguration.getGrpcProxy();
        if (grpcProxy == null) {
            return false;
        }
        ProxyConfig outProxy = grpcProxy.getInProxy();
        if (outProxy == null) {
            return false;
        }
        if (StringUtils.isBlank(outProxy.getAddress()) || outProxy.getPort() == null) {
            return false;
        }
        return true;
    }

    public static boolean nodeHasGRPCOutProxy(BaseConfiguration baseConfiguration) {
        GrpcProxyConfig grpcProxy = baseConfiguration.getGrpcProxy();
        if (grpcProxy == null) {
            return false;
        }
        ProxyConfig outProxy = grpcProxy.getOutProxy();
        if (outProxy == null) {
            return false;
        }
        if (StringUtils.isBlank(outProxy.getAddress()) || outProxy.getPort() == null) {
            return false;
        }
        return true;
    }
}
