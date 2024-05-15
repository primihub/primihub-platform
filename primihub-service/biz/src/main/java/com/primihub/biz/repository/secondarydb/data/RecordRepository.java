package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.PirRecord;
import com.primihub.biz.entity.data.po.PsiRecord;
import com.primihub.biz.entity.data.req.RecordReq;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface RecordRepository {

    PsiRecord selectPsiRecordByRecordId(@Param("recordId") String psiRecordId);

    PirRecord selectPirRecordByRecordId(@Param("recordId") String recordId);

    Set<PsiRecord> selectPsiRecordList(RecordReq req);
}
