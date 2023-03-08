package com.primihub.biz.entity.data.req;

import com.primihub.biz.entity.data.po.DataPsi;
import com.primihub.biz.entity.data.po.DataPsiTask;
import com.primihub.biz.entity.data.po.DataTask;
import lombok.Data;

@Data
public class DataPsiTaskSyncReq {
    private DataPsiTask psiTask;
    private DataPsi dataPsi;
    private DataTask dataTask;
    private Long timestamp;
    private Integer nonce;

    public DataPsiTaskSyncReq() {
    }

    public DataPsiTaskSyncReq(DataPsiTask psiTask, DataPsi dataPsi, DataTask dataTask) {
        this.psiTask = psiTask;
        this.dataPsi = dataPsi;
        this.dataTask = dataTask;
        supplement();
    }
    public void supplement(){
        this.timestamp = System.currentTimeMillis();
        this.nonce = (int)Math.random()*100;
    }
}
