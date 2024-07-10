package com.primihub.biz.service.data.pirphase1;

import com.primihub.biz.entity.data.req.DataPirCopyReq;
import org.springframework.stereotype.Service;

@Service
public interface PirPhase1Execute {
    void processPirPhase1(DataPirCopyReq req);
}
