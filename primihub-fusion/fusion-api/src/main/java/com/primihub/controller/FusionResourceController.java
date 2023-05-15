package com.primihub.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONValidator;
import com.primihub.entity.DataSet;
import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.entity.copy.dto.CopyResourceDto;
import com.primihub.entity.resource.param.ResourceParam;
import com.primihub.service.ResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RequestMapping("fusionResource")
@RestController
@Slf4j
public class FusionResourceController {

    @Autowired
    private ResourceService resourceService;

    @RequestMapping("getResourceList")
    public BaseResultEntity getResourceList(@RequestBody ResourceParam resourceParam){
        return resourceService.getResourceList(resourceParam);
    }

    @RequestMapping("getResourceListById")
    public BaseResultEntity getResourceListById(String[] resourceIdArray,String globalId){
        if(resourceIdArray==null||resourceIdArray.length==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceIdArray");
        }
        return resourceService.getResourceListById(resourceIdArray,globalId);
    }
    @RequestMapping("getCopyResource")
    public BaseResultEntity getCopyResource(String[] resourceIds){
        if(resourceIds==null||resourceIds.length==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceIds");
        }
        BaseResultEntity copyResource = resourceService.getCopyResource(resourceIds);
        log.info(JSONObject.toJSONString(copyResource));
        return copyResource;
    }

    @RequestMapping("getResourceTagList")
    public BaseResultEntity getResourceTagList(){
        return resourceService.getResourceTagList();
    }

    @RequestMapping("getDataResource")
    public BaseResultEntity getDataResource(String resourceId,String globalId){
        if (StringUtils.isEmpty(resourceId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        return resourceService.getDataResource(resourceId,globalId);
    }
    @PostMapping("saveResource")
    public BaseResultEntity saveResource(String globalId, @RequestBody List<CopyResourceDto> copyResourceDtoList){
        if (StringUtils.isEmpty(globalId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"globalId");
        }
        if (copyResourceDtoList==null || copyResourceDtoList.size()==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"copyResourceDtoList");
        }
        return resourceService.batchSaveResource(globalId,copyResourceDtoList);
    }

    @RequestMapping("getTestDataSet")
    public BaseResultEntity getTestDataSet(String id){
        return resourceService.getTestDataSet(id);
    }

    @PostMapping("batchSaveTestDataSet")
    public BaseResultEntity batchSaveTestDataSet(@RequestBody List<DataSet> dataSets){
        if (dataSets == null || dataSets.size()==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"shareData - dataSets");
        }
        return resourceService.batchSaveTestDataSet(dataSets);
    }

}
