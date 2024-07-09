package com.primihub.biz.service.data.exam;

import com.primihub.biz.entity.data.req.DataExamReq;
import org.springframework.stereotype.Service;

@Service
public interface ExamExecute {
    void processExam(DataExamReq req);
}
