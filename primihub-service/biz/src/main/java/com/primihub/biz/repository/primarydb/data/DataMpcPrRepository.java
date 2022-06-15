package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.DataMpcTask;
import com.primihub.biz.entity.data.po.DataScript;
import org.springframework.stereotype.Repository;

@Repository
public interface DataMpcPrRepository {

    void saveDataScript(DataScript dataScript);

    void updateDataScript(DataScript dataScript);

    void deleteDataScript(Long scriptId);

    void saveDataMpcTask(DataMpcTask dataMpcTask);

    void updateDataMpcTask(DataMpcTask dataMpcTask);
}
