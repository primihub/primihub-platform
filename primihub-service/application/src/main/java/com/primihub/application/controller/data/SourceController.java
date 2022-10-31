package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.req.DataSourceReq;
import com.primihub.biz.service.data.DataSourceService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据集数据库接口
 */
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
    @PostMapping("healthConnection")
    public BaseResultEntity healthConnection(@RequestBody DataSourceReq req){
        if (req.getDbType()==null || req.getDbType()<=0)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Type");
        if (StringUtils.isBlank(req.getDbDriver()))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Driver");
        if (StringUtils.isBlank(req.getDbUrl()))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Url");
        if (StringUtils.isBlank(req.getDbUsername()))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Username");
        if (StringUtils.isBlank(req.getDbPassword()))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Password");
        return dataSourceService.healthConnection(req);
    }

    /**
     * 数据库表详情
     * @param req
     * @return
     */
    @PostMapping("tableDetails")
    public BaseResultEntity dataSourceTableDetails(@RequestBody DataSourceReq req){
        if (req.getDbType()==null || req.getDbType()<=0)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Type");
        if (StringUtils.isBlank(req.getDbDriver()))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Driver");
        if (StringUtils.isBlank(req.getDbUrl()))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Url");
        if (StringUtils.isBlank(req.getDbUsername()))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Username");
        if (StringUtils.isBlank(req.getDbPassword()))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"Password");
        if (StringUtils.isBlank(req.getDbName()))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"DbName");
        return dataSourceService.dataSourceTableDetails(req);
    }
}
