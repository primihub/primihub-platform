package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.DataPsi;
import com.primihub.biz.entity.data.po.DataPsiTask;
import org.springframework.stereotype.Repository;

@Repository
public interface DataPsiPrRepository {

    void saveDataPsi(DataPsi dataPsi);
    void saveDataPsiTask(DataPsiTask dataPsiTask);

    void updateDataPsiTask(DataPsiTask dataPsiTask);
    void updateDataPsi(DataPsi dataPsi);

    void delPsiTask(Long taskId);

    void delPsi(Long psiId);
}
