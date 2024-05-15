package com.primihub.biz.service.data;


import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.po.PirRecord;
import com.primihub.biz.entity.data.po.PsiRecord;
import com.primihub.biz.entity.data.req.RecordReq;
import com.primihub.biz.repository.primarydb.data.RecordPrRepository;
import com.primihub.biz.repository.secondarydb.data.RecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RecordService {
    @Autowired
    private RecordRepository recordRepository;
    @Autowired
    private RecordPrRepository recordPrRepository;

    public BaseResultEntity savePsiRecord(PsiRecord record) {
        PsiRecord psiRecord = recordRepository.selectPsiRecordByRecordId(record.getRecordId());
        if (psiRecord == null) {
            recordPrRepository.savePsiRecord(psiRecord);
        } else {
            recordPrRepository.updatePsiRecord(psiRecord);
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity savePirRecord(PirRecord record) {
        PirRecord psiRecord = recordRepository.selectPirRecordByRecordId(record.getRecordId());
        if (psiRecord == null) {
            recordPrRepository.savePirRecord(psiRecord);
        } else {
            recordPrRepository.updatePirRecord(psiRecord);
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity getPsiRecordList(RecordReq req) {
        return BaseResultEntity.success(recordRepository.selectPsiRecordList(req));
    }
}
