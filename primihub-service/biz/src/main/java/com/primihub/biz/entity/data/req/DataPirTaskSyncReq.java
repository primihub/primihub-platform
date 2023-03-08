package com.primihub.biz.entity.data.req;

import com.primihub.biz.entity.data.po.DataPirTask;
import com.primihub.biz.entity.data.po.DataTask;
import lombok.Data;

@Data
public class DataPirTaskSyncReq {
    private DataTask dataTask;
    private DataPirTask dataPirTask;
    private Long timestamp;
    private Integer nonce;

    public DataPirTaskSyncReq() {
    }

    public DataPirTaskSyncReq(DataTask dataTask, DataPirTask dataPirTask) {
        this.dataTask = dataTask;
        this.dataPirTask = dataPirTask;
        supplement();
    }
    public void supplement(){
        this.timestamp = System.currentTimeMillis();
        this.nonce = (int)Math.random()*100;
    }
}
