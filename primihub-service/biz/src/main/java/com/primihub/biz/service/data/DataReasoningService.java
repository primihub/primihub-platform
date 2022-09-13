package com.primihub.biz.service.data;

import com.primihub.biz.convert.DataReasoningConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.po.DataModelTask;
import com.primihub.biz.entity.data.po.DataProjectResource;
import com.primihub.biz.entity.data.po.DataReasoning;
import com.primihub.biz.entity.data.po.DataReasoningResource;
import com.primihub.biz.entity.data.req.DataReasoningReq;
import com.primihub.biz.entity.data.req.DataReasoningResourceReq;
import com.primihub.biz.entity.data.req.ReasoningListReq;
import com.primihub.biz.repository.primarydb.data.DataReasoningPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataModelRepository;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.repository.secondarydb.data.DataReasoningRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DataReasoningService {

    @Autowired
    private DataReasoningPrRepository dataReasoningPrRepository;
    @Autowired
    private DataReasoningRepository dataReasoningRepository;
    @Autowired
    private DataModelRepository dataModelRepository;
    @Autowired
    private DataProjectRepository dataProjectRepository;



    public BaseResultEntity getReasoningList(ReasoningListReq req) {
        List<DataReasoning> dataReasonings = dataReasoningRepository.selectDataReasoninPage(req);
        if (dataReasonings.size()==0){
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Integer tolal = dataReasoningRepository.selectDataReasoninCount(req);
        return BaseResultEntity.success(new PageDataEntity(tolal,req.getPageSize(),req.getPageNo(),dataReasonings.stream().map(DataReasoningConvert::dataReasoningConvertVo).collect(Collectors.toList())));
    }

    public BaseResultEntity saveReasoning(DataReasoningReq req) {
        DataModelTask modelTask = dataModelRepository.queryModelTaskById(req.getTaskId());
        if (modelTask == null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"没有查询到模型信息");
        DataReasoning dataReasoning = DataReasoningConvert.dataReasoningReqConvertPo(req);
        Set<String> resourceIds = req.getResourceList().stream().map(DataReasoningResourceReq::getResourceId).collect(Collectors.toSet());
        List<DataProjectResource> dataProjectResources = dataProjectRepository.selectProjectResourceByResourceIds(resourceIds);
        if (dataProjectResources.isEmpty()){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"没有查询到资源信息");
        }
        dataReasoningPrRepository.saveDataReasoning(dataReasoning);
        Map<String, String> resourceMap = dataProjectResources.stream().collect(Collectors.toMap(DataProjectResource::getResourceId, DataProjectResource::getServerAddress));
        List<DataReasoningResource> dataReasoningResourceList = req.getResourceList().stream().map(r -> DataReasoningConvert.dataReasoningResourceReqConvertPo(r, dataReasoning.getId(), resourceMap.get(r.getResourceId()))).collect(Collectors.toList());
        dataReasoningPrRepository.saveDataReasoningResources(dataReasoningResourceList);
        Map<String,Object> map = new HashMap<>();
        map.put("id",dataReasoning.getId());
        map.put("reasoningId",dataReasoning.getReasoningId());
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getReasoning(Long id) {
        DataReasoning dataReasoning = dataReasoningRepository.selectDataReasoninById(id);
        if (dataReasoning==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"没有查询到数据");
        return BaseResultEntity.success(DataReasoningConvert.dataReasoningConvertVo(dataReasoning));
    }
}
