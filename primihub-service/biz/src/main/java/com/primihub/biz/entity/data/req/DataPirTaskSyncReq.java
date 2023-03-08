package com.primihub.biz.entity.data.req;

import com.primihub.biz.entity.data.po.DataPirTask;
import com.primihub.biz.entity.data.po.DataTask;
import lombok.Data;

@Data
public class DataPirTaskSyncReq {
    private DataTask dataTask;
    private DataPirTask dataPirTask;

    public DataPirTaskSyncReq() {
    }

    public DataPirTaskSyncReq(DataTask dataTask, DataPirTask dataPirTask) {
        this.dataTask = dataTask;
        this.dataPirTask = dataPirTask;
    }
}
