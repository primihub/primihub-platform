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
        if (StringUtils.isBlank(req.getServerAddress())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        }
        return otherBusinessesService.getResourceList(req);
    }

    @RequestMapping("getDataResource")
    public BaseResultEntity getDataResource(String serverAddress,String resourceId){
        if (StringUtils.isBlank(serverAddress)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        }
        if (StringUtils.isBlank(resourceId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        return otherBusinessesService.getDataResource(serverAddress,resourceId);
    }

    @RequestMapping("getResourceTagList")
    public BaseResultEntity getResourceTagList(String serverAddress){
        if (StringUtils.isBlank(serverAddress)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        }
        return otherBusinessesService.getResourceTagList(serverAddress);
    }
}
