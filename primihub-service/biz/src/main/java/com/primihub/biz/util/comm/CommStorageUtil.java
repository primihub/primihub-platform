package com.primihub.biz.util.comm;

import com.primihub.biz.tool.ReadWebSocketClient;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class CommStorageUtil {
    /**
     * 当前连接数
     */
    private static AtomicInteger webSocketCount = new AtomicInteger(0);
    private static AtomicInteger sseEmitterCount = new AtomicInteger(0);

    private static Map<String, ReadWebSocketClient> webSocketClientMap = new ConcurrentHashMap<>();
    private static Map<String, SseEmitter> sseEmitterMap = new ConcurrentHashMap<>();

    public static AtomicInteger getWebSocketCount() {
        return webSocketCount;
    }

    public static void setWebSocketCount(AtomicInteger webSocketCount) {
        CommStorageUtil.webSocketCount = webSocketCount;
    }

    public static AtomicInteger getSseEmitterCount() {
        return sseEmitterCount;
    }

    public static void setSseEmitterCount(AtomicInteger sseEmitterCount) {
        CommStorageUtil.sseEmitterCount = sseEmitterCount;
    }

    public static Map<String, ReadWebSocketClient> getWebSocketClientMap() {
        return webSocketClientMap;
    }

    public static void setWebSocketClientMap(Map<String, ReadWebSocketClient> webSocketClientMap) {
        CommStorageUtil.webSocketClientMap = webSocketClientMap;
    }

    public static Map<String, SseEmitter> getSseEmitterMap() {
        return sseEmitterMap;
    }

    public static void setSseEmitterMap(Map<String, SseEmitter> sseEmitterMap) {
        CommStorageUtil.sseEmitterMap = sseEmitterMap;
    }

    public static void removeWebSocketKey(String key){
        ReadWebSocketClient webSocketClient =webSocketClientMap.get(key);
        if (webSocketClient!=null){
            webSocketClient.setSseEmitterService(null);
            webSocketClient.close();
            webSocketClientMap.remove(key);
            // 数量-1
            webSocketCount.getAndDecrement();
        }
    }
}
