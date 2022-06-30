package com.primihub.biz.repository.primarydb.data;


import com.primihub.biz.entity.data.po.DataSecretkey;
import org.springframework.stereotype.Repository;

@Repository
public interface DataSecretkeyPrRepository {

    void saveDataSecretkey(DataSecretkey dataSecretkey);

}
