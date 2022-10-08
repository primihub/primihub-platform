package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.DataReasoning;
import com.primihub.biz.entity.data.po.DataReasoningResource;
import com.primihub.biz.entity.data.req.ReasoningListReq;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataReasoningRepository {
    List<DataReasoning> selectDataReasoninPage(ReasoningListReq req);

    Integer selectDataReasoninCount(ReasoningListReq req);

    DataReasoning selectDataReasoninById(Long id);

    List<DataReasoningResource> selectDataReasoningResource(Long reasoninId );
}
