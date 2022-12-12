package com.primihub.biz.tool;

import com.primihub.biz.service.sys.SysSseEmitterService;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Map;

@Slf4j
public class ReadWebSocketClient extends WebSocketClient {

    private String taskId;

    private SysSseEmitterService sseEmitterService;


    public ReadWebSocketClient(String taskId,URI serverUri) {
        super(serverUri);
        this.taskId = taskId;
    }

    public ReadWebSocketClient(String taskId,URI serverUri, Draft protocolDraft) {
        super(serverUri, protocolDraft);
        this.taskId = taskId;
    }

    public ReadWebSocketClient(String taskId,URI serverUri, Draft protocolDraft, Map<String, String> httpHeaders, int connectTimeout) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
        this.taskId = taskId;
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        log.info("[websocket][{}] 连接成功",taskId);
    }

    @Override
    public void onMessage(String message) {
        log.info("1[websocket][{}] 收到消息",taskId);
        if (sseEmitterService!=null){
            sseEmitterService.sendMessage(taskId,message);
            log.info("1[websocket-sse][{}] 发送消息",taskId);
        }else {
            log.info("1[websocket][{}] - 无连接",taskId);
        }

    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        log.info("2[websocket][{}] 收到消息",taskId);
        if (sseEmitterService!=null){
            sseEmitterService.sendMessage(taskId,getByteString(bytes));
            log.info("2[websocket-sse][{}] 发送消息",taskId);
        }else {
            log.info("2[websocket][{}] - 无连接",taskId);
        }

    }

    @Override
    public void onClose(int i, String s, boolean b) {
        log.info("[websocket][{}] 退出连接",taskId);
    }

    @Override
    public void onError(Exception e) {
        log.info("[websocket][{}] 连接错误={}",taskId, e.getMessage());
        e.printStackTrace();
    }

    /**
     * ByteBuffer 转换 String
     * @param buffer
     * @return
     */
    public static String getByteString(ByteBuffer buffer)
    {
        Charset charset = null;
        CharsetDecoder decoder = null;
        CharBuffer charBuffer = null;
        try
        {
            charset = Charset.forName("UTF-8");
            decoder = charset.newDecoder();
            // charBuffer = decoder.decode(buffer);//用这个的话，只能输出来一次结果，第二次显示为空
            charBuffer = decoder.decode(buffer.asReadOnlyBuffer());
            return charBuffer.toString();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return "";
        }
    }

    public void setSseEmitterService(SysSseEmitterService sseEmitterService) {
        this.sseEmitterService = sseEmitterService;
    }

}