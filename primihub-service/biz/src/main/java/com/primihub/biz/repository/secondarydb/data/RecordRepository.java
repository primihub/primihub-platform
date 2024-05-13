package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.PirRecord;
import com.primihub.biz.entity.data.po.PsiRecord;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordRepository {

    PsiRecord selectPsiRecordByRecordId(String psiRecordId);

    PirRecord selectPirRecordByRecordId(String recordId);
}
