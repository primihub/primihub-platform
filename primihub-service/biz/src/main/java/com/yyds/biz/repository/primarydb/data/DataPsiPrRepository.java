package com.yyds.biz.repository.primarydb.data;

import com.yyds.biz.entity.data.po.DataPsi;
import com.yyds.biz.entity.data.po.DataPsiResource;
import com.yyds.biz.entity.data.po.DataPsiTask;
import org.springframework.stereotype.Repository;

@Repository
public interface DataPsiPrRepository {

    void saveDataPsiResource(DataPsiResource dataPsiResource);
    void saveDataPsi(DataPsi dataPsi);
    void saveDataPsiTask(DataPsiTask dataPsiTask);

    void updateDataPsiResource(DataPsiResource dataPsiResource);
    void updateDataPsiTask(DataPsiTask dataPsiTask);

    void delPsiTask(Long taskId);

    void delPsi(Long psiId);
}
