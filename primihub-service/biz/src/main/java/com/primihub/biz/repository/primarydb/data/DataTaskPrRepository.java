package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.DataPirTask;
import com.primihub.biz.entity.data.po.DataTask;
import org.springframework.stereotype.Repository;

@Repository
public interface DataTaskPrRepository {

    void saveDataTask(DataTask dataTask);

    void updateDataTask(DataTask dataTask);

    void deleteDataTask(Long taskId);

    void saveDataPirTask(DataPirTask DataPirTask);

    void deleteDataPirTask(Long taskId);

}
