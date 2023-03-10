package com.primihub.biz.entity.data.req;

import com.primihub.biz.entity.data.po.DataModelTask;
import com.primihub.biz.entity.data.po.DataReasoning;
import com.primihub.biz.entity.data.po.DataReasoningResource;
import com.primihub.biz.entity.data.po.DataTask;
import lombok.Data;

import java.util.List;

@Data
public class DataReasoningTaskReq {
    private DataReasoning dataReasoning;
    private DataTask dataTask;
    private Long timestamp;
    private Integer nonce;

    public DataReasoningTaskReq() {
        supplement();
    }

    public DataReasoningTaskReq(DataReasoning dataReasoning, DataTask dataTask) {
        this.dataTask = dataTask;
        this.dataReasoning = dataReasoning;
        supplement();
    }

    public void supplement(){
        this.timestamp = System.currentTimeMillis();
        this.nonce = (int)Math.random()*100;
    }
}
