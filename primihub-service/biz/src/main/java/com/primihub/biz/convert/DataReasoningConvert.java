package com.primihub.biz.convert;

import com.primihub.biz.entity.data.po.DataReasoning;
import com.primihub.biz.entity.data.po.DataReasoningResource;
import com.primihub.biz.entity.data.req.DataReasoningReq;
import com.primihub.biz.entity.data.req.DataReasoningResourceReq;
import com.primihub.biz.entity.data.vo.DataReasoningVo;

import java.util.UUID;

public class DataReasoningConvert {

    public static DataReasoning dataReasoningReqConvertPo(DataReasoningReq req){
        DataReasoning dataReasoning = new DataReasoning();
        dataReasoning.setReasoningId(UUID.randomUUID().toString());
        dataReasoning.setReasoningName(req.getReasoningName());
        dataReasoning.setReasoningDesc(req.getReasoningDesc());
        dataReasoning.setReasoningType(req.getResourceList().size());
        dataReasoning.setReasoningState(0);
        dataReasoning.setTaskId(req.getTaskId());
        dataReasoning.setUserId(req.getUserId());
        return dataReasoning;
    }

    public static DataReasoningResource dataReasoningResourceReqConvertPo(DataReasoningResourceReq req,Long reasoningId){
        DataReasoningResource dataReasoningResource = new DataReasoningResource();
        dataReasoningResource.setReasoningId(reasoningId);
        dataReasoningResource.setResourceId(req.getResourceId());
        dataReasoningResource.setParticipationIdentity(req.getParticipationIdentity());
        return dataReasoningResource;
    }

    public static DataReasoningVo dataReasoningConvertVo(DataReasoning dataReasoning){
        DataReasoningVo dataReasoningVo = new DataReasoningVo();
        dataReasoningVo.setId(dataReasoning.getId());
        dataReasoningVo.setReasoningId(dataReasoning.getReasoningId());
        dataReasoningVo.setReasoningName(dataReasoning.getReasoningName());
        dataReasoningVo.setReasoningDesc(dataReasoning.getReasoningDesc());
        dataReasoningVo.setReasoningType(dataReasoning.getReasoningType());
        dataReasoningVo.setReasoningState(dataReasoning.getReasoningState());
        dataReasoningVo.setTaskId(dataReasoning.getTaskId());
        dataReasoningVo.setReleaseDate(dataReasoning.getReleaseDate());
        dataReasoningVo.setRunTaskId(dataReasoning.getRunTaskId());
        return dataReasoningVo;
    }

}
