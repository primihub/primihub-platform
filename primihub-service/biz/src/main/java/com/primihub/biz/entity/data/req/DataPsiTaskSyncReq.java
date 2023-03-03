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
}
