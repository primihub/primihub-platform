package com.primihub.biz.service.data.exam;

import com.primihub.biz.entity.data.req.DataExamReq;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExamExecuteImei implements ExamExecute {
    @Override
    public void processExam(DataExamReq req) {
        log.info("process exam future task : imei");
    }
}
