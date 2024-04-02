package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.SourceEnum;
import com.primihub.biz.entity.data.req.DataSourceReq;
import com.primihub.biz.service.data.DataSourceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据集数据库接口
 */
@Api(value = "数据集数据库接口",tags = "数据集数据库接口")
@RequestMapping("dbsource")
@RestController
public class SourceController {
    @Autowired
    private DataSourceService dataSourceService;

    /**
     * 检测数据库连接
     * @param req
     * @return
     */
    @ApiOperation(value = "检测数据库连接",httpMethod = "POST",consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(value = "healthConnection",consumes = MediaType.APPLICATION_JSON_VALUE)
    public BaseResultEntity healthConnection(@RequestBody DataSourceReq req){
        BaseResultEntity baseResultEntity = checkDataSourceReq(req);
        if (!baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())) {
            return baseResultEntity;
        }
        return dataSourceService.healthConnection(req);
    }

    /**
     * 数据库表详情
     * @param req
     * @return
     */
    @ApiOperation(value = "数据库表详情",httpMethod = "POST",consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping(value = "tableDetails",consumes = MediaType.APPLICATION_JSON_VALUE)
    public BaseResultEntity dataSourceTableDetails(@RequestBody DataSourceReq req){
        BaseResultEntity baseResultEntity = checkDataSourceReq(req);
        if (!baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())) {
            return baseResultEntity;
        }
        if (StringUtils.isBlank(req.getDbName())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"DbName");
        }
        return dataSourceService.dataSourceTableDetails(req);
    }

    public BaseResultEntity checkDataSourceReq(DataSourceReq req){
        if (req.getDbType()==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Type");
        }
        SourceEnum sourceEnum = SourceEnum.SOURCE_MAP.get(req.getDbType());
        if (sourceEnum==null) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"Type");
        }
        if (sourceEnum == SourceEnum.mysql){
            if (StringUtils.isBlank(req.getDbDriver())) {
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Driver");
            }
//            if (StringUtils.isBlank(req.getDbUsername())) {
//                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Username");
//            }
//            if (StringUtils.isBlank(req.getDbPassword())) {
//                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Password");
//            }
        }
        if (StringUtils.isBlank(req.getDbUrl())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Url");
        }
        return BaseResultEntity.success();
    }
}
