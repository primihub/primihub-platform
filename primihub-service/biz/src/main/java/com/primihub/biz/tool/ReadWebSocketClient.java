package com.primihub.biz.tool;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.service.sys.SysSseEmitterService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.List;
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
//            sseEmitterService.sendMessage(taskId,message);
            sendMessage(message);
            log.info("1[websocket-sse][{}] 发送消息",taskId);
        }else {
            log.info("1[websocket][{}] - 无连接",taskId);
        }

    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        log.info("2[websocket][{}] 收到消息",taskId);
        if (sseEmitterService!=null){
            sendMessage(getByteString(bytes));
//            sseEmitterService.sendMessage(taskId,);
            log.info("2[websocket-sse][{}] 发送消息",taskId);
        }else {
            log.info("2[websocket][{}] - 无连接",taskId);
        }

    }

    public void sendMessage(String message){
        List<LokiStreamEntity> streams = JSONObject.parseObject(message).getJSONArray("streams").toJavaList(LokiStreamEntity.class);
        streams.forEach(lokiStreamEntity -> {
            if (lokiStreamEntity!=null && lokiStreamEntity.getValues()!=null){
                for (String[] value : lokiStreamEntity.getValues()) {
                    if (value!=null && value.length==2){
                        sseEmitterService.sendMessage(taskId,value[1]);
                    }
                }
            }
        });
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

    public static void main(String[] args) {
        String v = "{\"streams\":[{\"stream\":{\"app\":\"primihub-node0\",\"container\":\"node0\",\"filename\":\"/var/log/pods/zhangjianning_primihub-node0-c6968679d-kthx8_f7409472-cba5-4476-a287-bb6691aef95e/node0/0.log\",\"job\":\"zhangjianning/primihub-node0\",\"namespace\":\"zhangjianning\",\"node_name\":\"node2\",\"pod\":\"primihub-node0-c6968679d-kthx8\"},\"values\":[[\"1702443898343828197\",\"{\\\"log\\\":\\\"I20231213 13:04:58.146282 21802 worker.cc:159] RequestId: 3c34f89aca3440a4a7afb32af899de8a Task Process::execute: intended_worker_id: \\\\\\\"1\\\\\\\" task { type: PSI_TASK name: \\\\\\\"psiTask\\\\\\\" language: PROTO params { param_map { key: \\\\\\\"CLIENT\\\\\\\" value { value_string: \\\\\\\"1af2e6f76cf0-918e0858-81d3-4206-8034-8c138a09b08d\\\\\\\" } } param_map { key: \\\\\\\"SERVER_CONF_FILE\\\\\\\" value { value_string: \\\\\\\"/data/primihub_node0.yaml\\\\\\\" } } param_map { key: \\\\\\\"clientData\\\\\\\" value { value_string: \\\\\\\"1af2e6f76cf0-918e0858-81d3-4206-8034-8c138a09b08d\\\\\\\" } } param_map { key: \\\\\\\"clientIndex\\\\\\\" value { is_array: true value_int32_array { value_int32_array: 17 } } } param_map { key: \\\\\\\"outputFullFilename\\\\\\\" value { value_string: \\\\\\\"/data/result/2023121313/1734801561551708162.csv\\\\\\\" } } param_map { key: \\\\\\\"psiTag\\\\\\\" value { value_int32: 0 } } param_map { key: \\\\\\\"psiType\\\\\\\" value { value_int32: 0 } } param_map { key: \\\\\\\"serverData\\\\\\\" value { value_string: \\\\\\\"ea86a809e648-65a24965-5265-4665-8d25-3b6e10fa445f\\\\\\\" } } param_map { key: \\\\\\\"serverIndex\\\\\\\" value { is_array: true value_int32_array { value_int32_array: 17 } } } } task_info { task_id: \\\\\\\"1734801561551708162\\\\\\\" job_id: \\\\\\\"1\\\\\\\" request_id: \\\\\\\"3c34f89aca3440a4a7afb32af899de8a\\\\\\\" } party_name: \\\\\\\"CLIENT\\\\\\\" party_datasets { key: \\\\\\\"CLIENT\\\\\\\" value { data { key: \\\\\\\"CLIENT\\\\\\\" value: \\\\\\\"{\\\\\\\\\\\\\\\"data_path\\\\\\\\\\\\\\\":\\\\\\\\\\\\\\\"/data/upload/1/2023121313/e682dcc5-261d-429b-afd9-f5c2e0cf79ae.csv\\\\\\\\\\\\\\\",\\\\\\\\\\\\\\\"dataset_id\\\\\\\\\\\\\\\":\\\\\\\\\\\\\\\"1af2e6f76cf0-918e0858-81d3-4206-8034-8c138a09b08d\\\\\\\\\\\\\\\",\\\\\\\\\\\\\\\"schema\\\\\\\\\\\\\\\":\\\\\\\\\\\\\\\"[{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\345\\\\\\\\247\\\\\\\\223\\\\\\\\345\\\\\\\\220\\\\\\\\215\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":13},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\350\\\\\\\\272\\\\\\\\253\\\\\\\\344\\\\\\\\273\\\\\\\\275\\\\\\\\350\\\\\\\\257\\\\\\\\201\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":13},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\346\\\\\\\\211\\\\\\\\213\\\\\\\\346\\\\\\\\234\\\\\\\\272\\\\\\\\345\\\\\\\\217\\\\\\\\267\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":9},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\347\\\\\\\\261\\\\\\\\215\\\\\\\\350\\\\\\\\264\\\\\\\\257\\\\\\\\347\\\\\\\\234\\\\\\\\201\\\\\\\\344\\\\\\\\273\\\\\\\\275\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":13},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\347\\\\\\\\216\\\\\\\\260\\\\\\\\344\\\\\\\\275\\\\\\\\217\\\\\\\\345\\\\\\\\235\\\\\\\\200\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":13},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\344\\\\\\\\273\\\\\\\\273\\\\\\\\350\\\\\\\\201\\\\\\\\214\\\\\\\\345\\\\\\\\205\\\\\\\\254\\\\\\\\345\\\\\\\\217\\\\\\\\270\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":13},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\344\\\\\\\\273\\\\\\\\273\\\\\\\\350\\\\\\\\201\\\\\\\\214\\\\\\\\350\\\\\\\\201\\\\\\\\214\\\\\\\\344\\\\\\\\275\\\\\\\\215\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":13},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\351\\\\\\\\202\\\\\\\\256\\\\\\\\347\\\\\\\\256\\\\\\\\261\\\\\\\\345\\\\\\\\234\\\\\\\\260\\\\\\\\345\\\\\\\\235\\\\\\\\200\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":13},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\345\\\\\\\\255\\\\\\\\246\\\\\\\\345\\\\\\\\216\\\\\\\\206\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":13},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\345\\\\\\\\256\\\\\\\\266\\\\\\\\345\\\\\\\\272\\\\\\\\255\\\\\\\\350\\\\\\\\203\\\\\\\\214\\\\\\\\346\\\\\\\\231\\\\\\\\257\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":13},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\347\\\\\\\\273\\\\\\\\217\\\\\\\\346\\\\\\\\265\\\\\\\\216\\\\\\\\346\\\\\\\\203\\\\\\\\205\\\\\\\\345\\\\\\\\206\\\\\\\\265\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":13},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\345\\\\\\\\271\\\\\\\\264\\\\\\\\346\\\\\\\\224\\\\\\\\266\\\\\\\\345\\\\\\\\205\\\\\\\\245\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":7},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\345\\\\\\\\271\\\\\\\\264\\\\\\\\346\\\\\\\\266\\\\\\\\210\\\\\\\\350\\\\\\\\264\\\\\\\\271\\\\\\\\346\\\\\\\\260\\\\\\\\264\\\\\\\\345\\\\\\\\271\\\\\\\\263\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":7},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\346\\\\\\\\230\\\\\\\\257\\\\\\\\345\\\\\\\\220\\\\\\\\246\\\\\\\\346\\\\\\\\234\\\\\\\\211\\\\\\\\346\\\\\\\\210\\\\\\\\277\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":13},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\346\\\\\\\\230\\\\\\\\257\\\\\\\\345\\\\\\\\220\\\\\\\\246\\\\\\\\346\\\\\\\\234\\\\\\\\211\\\\\\\\350\\\\\\\\275\\\\\\\\246\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":13},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\345\\\\\\\\207\\\\\\\\272\\\\\\\\347\\\\\\\\224\\\\\\\\237\\\\\\\\345\\\\\\\\271\\\\\\\\264\\\\\\\\344\\\\\\\\273\\\\\\\\275\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":7},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\345\\\\\\\\256\\\\\\\\266\\\\\\\\345\\\\\\\\272\\\\\\\\255\\\\\\\\346\\\\\\\\210\\\\\\\\220\\\\\\\\345\\\\\\\\221\\\\\\\\230\\\\\\\\345\\\\\\\\207\\\\\\\\240\\\\\\\\344\\\\\\\\272\\\\\\\\272\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":7},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\345\\\\\\\\251\\\\\\\\232\\\\\\\\345\\\\\\\\247\\\\\\\\273\\\\\\\\347\\\\\\\\212\\\\\\\\266\\\\\\\\346\\\\\\\\200\\\\\\\\201\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":13},{\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\"\\\\\\\\346\\\\\\\\211\\\\\\\\213\\\\\\\\346\\\\\\\\234\\\\\\\\272\\\\\\\\345\\\\\\\\217\\\\\\\\267\\\\\\\\345\\\\\\\\275\\\\\\\\222\\\\\\\\345\\\\\\\\261\\\\\\\\236\\\\\\\\345\\\\\\\\234\\\\\\\\260\\\\\\\\344\\\\\\\\277\\\\\\\\241\\\\\\\\346\\\\\\\\201\\\\\\\\257\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\":13}]\\\\\\\\\\\\\\\",\\\\\\\\\\\\\\\"type\\\\\\\\\\\\\\\":\\\\\\\\\\\\\\\"csv\\\\\\\\\\\\\\\"}\\\\\\\" } dataset_detail: true } } party_datasets { key: \\\\\\\"SERVER\\\\\\\" value { data { key: \\\\\\\"SERVER\\\\\\\" value: \\\\\\\"ea86a809e648-65a24965-5265-4665-8d25-3b6e10fa445f\\\\\\\" } } } party_access_info { key: \\\\\\\"CLIENT\\\\\\\" value { node_id: \\\\\\\"node0\\\\\\\" ip: \\\\\\\"primihub-node0\\\\\\\" port: 50050 } } party_access_info { key: \\\\\\\"SERVER\\\\\\\" value { node_id: \\\\\\\"node1\\\\\\\" ip: \\\\\\\"primihub-node1\\\\\\\" port: 50050 party_id: 1 } } auxiliary_server { key: \\\\\\\"PROXY_NODE\\\\\\\" value { ip: \\\\\\\"primihub-node0\\\\\\\" port: 50050 } } auxiliary_server { key: \\\\\\\"SCHEDULER_NODE\\\\\\\" value { node_id: \\\\\\\"node0\\\\\\\" ip: \\\\\\\"primihub-node0\\\\\\\" port: 50050 } } } sequence_number: 11 client_processed_up_to: 22 \\\\n\\\",\\\"stream\\\":\\\"stderr\\\",\\\"time\\\":\\\"2023-12-13T05:04:58.146520802Z\\\"}\"]]}]}";
        JSONObject jsonObject = JSONObject.parseObject(v);
        List<LokiStreamEntity> streams = jsonObject.getJSONArray("streams").toJavaList(LokiStreamEntity.class);
        for (LokiStreamEntity stream : streams) {
            System.out.println(JSONObject.toJSONString(stream));
        }
    }

}


@Data
class LokiStreamEntity {
    private String[][] values;
}
