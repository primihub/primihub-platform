package com.primihub.biz.service.sys;

import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.tool.ReadWebSocketClient;
import com.primihub.biz.util.comm.CommStorageUtil;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

@Service
@Slf4j
public class SysWebSocketService {

    @Autowired
    private SysSseEmitterService sseEmitterService;



    /**
     * 创建用户连接并返回
     *
     * @param key 标识
     * @return SseEmitter
     */
    public BaseResultEntity connect(String key,String wsUrl) {
        try {
            ReadWebSocketClient webSocketClient = new ReadWebSocketClient(key,new URI(wsUrl));
            webSocketClient.connect();
            Timer t = new Timer();
            t.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if(webSocketClient.isClosed()){
                        log.error("{}:断线重连",key);
                        sseEmitterService.sendMessage(key,"ws 连接断开,请刷新后重试!");
                        this.cancel();
                    }
                }
            },1000,5000);
            webSocketClient.setSseEmitterService(sseEmitterService);
            CommStorageUtil.getWebSocketCount().getAndIncrement();
            CommStorageUtil.getWebSocketClientMap().put(key,webSocketClient);
            log.info("创建新的WebSocketClient连接，当前标识：{}", key);
            return BaseResultEntity.success();
        }catch (Exception e){
            log.info("创建新的WebSocketClient连接，当前标识：{} e: {}", key,e.getMessage());
            e.printStackTrace();
            return BaseResultEntity.failure(BaseResultEnum.DATA_LOG_FAIL,"创建长连接失败,请联系管理员");
        }

    }

    /**
     * 移除用户连接
     */
    public void removeKey(String key) {
        CommStorageUtil.removeWebSocketKey(key);
        log.info("移除WebSocketClient标识：{}", key);
    }
}
