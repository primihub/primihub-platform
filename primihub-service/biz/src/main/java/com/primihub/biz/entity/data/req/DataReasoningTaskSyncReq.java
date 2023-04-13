package com.primihub.biz.entity.data.req;

import com.primihub.biz.entity.data.po.DataModelTask;
import com.primihub.biz.entity.data.po.DataReasoning;
import com.primihub.biz.entity.data.po.DataReasoningResource;
import com.primihub.biz.entity.data.po.DataTask;
import lombok.Data;

import java.util.List;

@Data
public class DataReasoningTaskSyncReq {
    private DataReasoning dataReasoning;
    private List<DataReasoningResource> dataReasoningResourceList;
    private DataModelTask modelTask;
    private DataTask dataTask;
    private Long timestamp;
    private Integer nonce;

    public DataReasoningTaskSyncReq() {
        supplement();
    }

    public DataReasoningTaskSyncReq(DataReasoning dataReasoning, List<DataReasoningResource> dataReasoningResourceList, DataModelTask modelTask, DataTask dataTask) {
        this.dataTask = dataTask;
        this.dataReasoning = dataReasoning;
        this.dataReasoningResourceList = dataReasoningResourceList;
        this.modelTask = modelTask;
        supplement();
    }

    public void supplement(){
        this.timestamp = System.currentTimeMillis();
        this.nonce = (int)Math.random()*100;
    }
}
