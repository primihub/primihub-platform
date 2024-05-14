package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.PirRecord;
import com.primihub.biz.entity.data.po.PsiRecord;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordRepository {

    PsiRecord selectPsiRecordByRecordId(@Param("recordId") String psiRecordId);

    PirRecord selectPirRecordByRecordId(@Param("recordId") String recordId);
}
