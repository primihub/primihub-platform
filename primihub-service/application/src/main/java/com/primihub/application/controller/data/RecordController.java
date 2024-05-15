package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.req.RecordReq;
import com.primihub.biz.service.data.RecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping
@RestController
@Slf4j
public class RecordController {
    @Autowired
    private RecordService recordService;

    @GetMapping("/record/psiRecordList")
    public BaseResultEntity getPsiRecordList(RecordReq req) {
        return recordService.getPsiRecordList(req);
    }

}
