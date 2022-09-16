package com.primihub.biz.repository.primarydb.data;


import com.primihub.biz.entity.data.po.DataReasoning;
import com.primihub.biz.entity.data.po.DataReasoningResource;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataReasoningPrRepository {
    void saveDataReasoning(DataReasoning dataReasoning);

    void updateDataReasoning(DataReasoning dataReasoning);

    void deleteDataReasoning(Long id);

    void saveDataReasoningResources(@Param("list") List<DataReasoningResource> list);

    void deleteDataReasoningResources(Long reasoningId);
}
