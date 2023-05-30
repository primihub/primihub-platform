package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.req.DataFResourceReq;
import com.primihub.biz.service.data.OtherBusinessesService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("fusionResource")
@RestController
public class FusionResourceController {

    @Autowired
    private OtherBusinessesService otherBusinessesService;

    @RequestMapping("getResourceList")
    public BaseResultEntity getResourceList(DataFResourceReq req){
        return otherBusinessesService.getResourceList(req);
    }

    @RequestMapping("getDataResource")
    public BaseResultEntity getDataResource(String resourceId){
        if (StringUtils.isBlank(resourceId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        return otherBusinessesService.getDataResource(resourceId);
    }

    @RequestMapping("getResourceTagList")
    public BaseResultEntity getResourceTagList(){
        return otherBusinessesService.getResourceTagList();
    }
}
