package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.req.DataFResourceReq;
import com.primihub.biz.service.data.OtherBusinessesService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "Meta服务数据集接口",tags = "Meta服务数据集接口")
@RequestMapping("fusionResource")
@RestController
public class FusionResourceController {

    @Autowired
    private OtherBusinessesService otherBusinessesService;

    @ApiOperation(value = "获取资源详情列表",httpMethod = "GET")
    @GetMapping("getResourceList")
    public BaseResultEntity getResourceList(DataFResourceReq req){
        return otherBusinessesService.getResourceList(req);
    }

    @ApiOperation(value = "根据资源唯一ID获取资源详情",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParam(name = "resourceId", value = "资源唯一ID", dataType = "String", paramType = "query")
    @GetMapping(value = "getDataResource")
    public BaseResultEntity getDataResource(String resourceId){
        if (StringUtils.isBlank(resourceId)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        return otherBusinessesService.getDataResource(resourceId);
    }

    @ApiOperation(value = "获取资源集标签",httpMethod = "GET")
    @GetMapping("getResourceTagList")
    public BaseResultEntity getResourceTagList(){
        return otherBusinessesService.getResourceTagList();
    }
}
