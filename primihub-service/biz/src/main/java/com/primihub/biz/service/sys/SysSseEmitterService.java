package com.primihub.biz.service.sys;

import com.primihub.biz.util.comm.CommStorageUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Slf4j
@Service
public class SysSseEmitterService {


    /**
     * 创建用户连接并返回 SseEmitter
     *
     * @param key 标识
     * @return SseEmitter
     */
    public SseEmitter connect(String key) {
        // 设置超时时间，0表示不过期。默认5分钟，超过时间未完成会抛出异常：AsyncRequestTimeoutException
        SseEmitter sseEmitter = new SseEmitter(30L * 60L * 1000L);
        sseEmitter.onCompletion(completionCallBack(key));
        sseEmitter.onError(errorCallBack(key));
        sseEmitter.onTimeout(timeoutCallBack(key));
        CommStorageUtil.getSseEmitterMap().put(key, sseEmitter);
        CommStorageUtil.getSseEmitterCount().getAndIncrement();
        log.info("创建新的sse连接，当前标识：{}", key);
        return sseEmitter;
    }

    /**
     * 给指定用户发送信息
     */
    public void sendMessage(String key, String message) {
        if (CommStorageUtil.getSseEmitterMap().containsKey(key)) {
            try {
//                CommStorageUtil.getSseEmitterMap().get(key).send(message, MediaType.APPLICATION_JSON);
                CommStorageUtil.getSseEmitterMap().get(key).send(message);
//                sseEmitterMap.get(key).send(message);
            } catch (IOException e) {
                log.error("标识[{}]推送异常:{}", key, e.getMessage());
                removeKey(key);
            }
        }
    }

    /**
     * 群发消息
     */
    public void batchSendMessage(String wsInfo, List<String> ids) {
        ids.forEach(key -> sendMessage(wsInfo, key));
    }

    /**
     * 群发所有人
     */
    public void batchSendMessage(String wsInfo) {
        CommStorageUtil.getSseEmitterMap().forEach((k, v) -> {
            try {
                v.send(wsInfo, MediaType.APPLICATION_JSON);
            } catch (IOException e) {
                log.error("标识[{}]推送异常:{}", k, e.getMessage());
                removeKey(k);
            }
        });
    }

    /**
     * 移除用户连接
     */
    public void removeKey(String key) {
        CommStorageUtil.getSseEmitterMap().remove(key);
        // 数量-1
        CommStorageUtil.getSseEmitterCount().getAndDecrement();
        log.info("移除标识：{}", key);
        CommStorageUtil.removeWebSocketKey(key);
    }

    /**
     * 获取当前连接信息
     */
    public List<String> getIds() {
        return new ArrayList<>(CommStorageUtil.getSseEmitterMap().keySet());
    }

    /**
     * 获取当前连接数量
     */
    public int getUserCount() {
        return CommStorageUtil.getSseEmitterCount().intValue();
    }

    private Runnable completionCallBack(String key) {
        return () -> {
            log.info("结束连接：{}", key);
            removeKey(key);
        };
    }

    private Runnable timeoutCallBack(String key) {
        return () -> {
            log.info("连接超时：{}", key);
            removeKey(key);
        };
    }

    private Consumer<Throwable> errorCallBack(String key) {
        return throwable -> {
            log.info("连接异常：{}", key);
            removeKey(key);
        };
    }
}
