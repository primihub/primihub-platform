package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.po.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataModelPrRepository {

    void saveDataModel(DataModel dataModel);

    void saveDataModelTask(DataModelTask dataModelTask);

    void updateDataModelTask(DataModelTask dataModelTask);

    void updateDataModel(DataModel dataModel);

    void saveDataModelResource(List<DataModelResource> list);

    void saveDataModelQuota(DataModelQuota modelQuota);

    void saveDataComponent(DataComponent dataComponent);

    void saveDataModelComponent(DataModelComponent dataModelComponent);

    void deleteModelByModelId(@Param("modelId") Long modelId,@Param("isDraft") Integer isDraft);

    void deleteDataComponent(Long modelId);

    void deleteDataModelComponent(Long modelId);

    void deleteDataModelResource(Long modelId);

    void updateDataComponent(DataComponent dataComponent);
}
