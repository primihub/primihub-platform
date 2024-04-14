package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.req.DataFResourceReq;
import com.primihub.biz.service.data.OtherBusinessesService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Meta服务数据集接口
 */
@RequestMapping("fusionResource")
@RestController
public class FusionResourceController {

    @Autowired
    private OtherBusinessesService otherBusinessesService;

    /**
     * 获取资源详情列表
     */
    @GetMapping("getResourceList")
    public BaseResultEntity getResourceList(DataFResourceReq req){
        return otherBusinessesService.getResourceList(req);
    }
    /**
     * 根据资源唯一ID获取资源详情
     * MediaType.APPLICATION_FORM_URLENCODED_VALUE
     */
    @GetMapping(value = "getDataResource")
    public BaseResultEntity getDataResource(String resourceId){
        if (StringUtils.isBlank(resourceId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        return otherBusinessesService.getDataResource(resourceId);
    }

    /**
     * 获取资源集标签
     */
    @GetMapping("getResourceTagList")
    public BaseResultEntity getResourceTagList(){
        return otherBusinessesService.getResourceTagList();
    }
}
