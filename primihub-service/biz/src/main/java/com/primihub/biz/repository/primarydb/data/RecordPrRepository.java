package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.PirRecord;
import com.primihub.biz.entity.data.po.PsiRecord;
import org.springframework.stereotype.Repository;

@Repository
public interface RecordPrRepository {
    void savePsiRecord(PsiRecord psiRecord);

    void updatePsiRecord(PsiRecord psiRecord);

    void savePirRecord(PirRecord pirRecord);
}
