package com.primihub.gateway.util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

@Slf4j
public class WebFluxUtil {
    private static final String IP_UNKNOWN = "unknown";
    private static final String IP_LOCAL = "127.0.0.1";
    private static final String IPV6_LOCAL = "0:0:0:0:0:0:0:1";

    public static String getIpAddress(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        String ipAddress = headers.getFirst("x-forwarded-for");
        log.info("x-forwarded-for:{}",ipAddress);
        if (ipAddress == null || ipAddress.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = headers.getFirst("Proxy-Client-IP");
        }
//        log.info("Proxy-Client-IP:{}",headers.getFirst("Proxy-Client-IP"));
        if (ipAddress == null || ipAddress.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = headers.getFirst("WL-Proxy-Client-IP");
        }
//        log.info("WL-Proxy-Client-IP:{}",headers.getFirst("WL-Proxy-Client-IP"));
        if (ipAddress == null || ipAddress.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = headers.getFirst("X-Real-Ip");
        }
        if (ipAddress == null || ipAddress.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = Optional.ofNullable(request.getRemoteAddress())
                    .map(address -> address.getAddress().getHostAddress())
                    .orElse("");
            if (IP_LOCAL.equals(ipAddress) || IPV6_LOCAL.equals(ipAddress)) {
                // 根据网卡取本机配置的IP
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ipAddress = inet.getHostAddress();
                    log.info("根据网卡取本机配置的IP:{}",ipAddress);
                } catch (UnknownHostException e) {
                    log.error(e.getMessage());
                }
            }
        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP，多个IP按照','分割
        if (ipAddress != null && ipAddress.indexOf(",") > 0) {
            ipAddress = ipAddress.split(",")[0];
        }

        return ipAddress;
    }
}
