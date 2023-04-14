package com.primihub.application.controller.data;


import com.primihub.biz.entity.base.BaseJsonParam;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.req.DataVisitingUsersReq;
import com.primihub.biz.service.data.MarketService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 应用市场
 */
@RequestMapping("market")
@RestController
public class MarketController {
    @Autowired
    private MarketService marketService;

    /**
     * 提交来访用户信息
     * @return
     */
    @PostMapping("submitvisitingusers")
    public BaseResultEntity submitVisitingUsers(@RequestBody BaseJsonParam<List<DataVisitingUsersReq>> req){
        List<DataVisitingUsersReq> param = req.getParam();
        if (param == null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"visitingUsersList");
        }
        if (param.size()!=7) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"visitingUsersList 数量不够");
        }
        for (DataVisitingUsersReq dv : param) {
            if (dv.isNull()){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"存在null值");
            }
        }
        return marketService.submitVisitingUsers(param);
    }

    /**
     * 获取来访用信息
     * @return
     */
    @GetMapping("getvisitingusers")
    public BaseResultEntity getVisitingUsers(){
        return marketService.getVisitingUsers();
    }

    /**
     * 创建、展示和记录展现类型累加
     * @param type 前端key
     * @param operation 0、展示 1、累加 2、创建type值,号拼接
     * @return
     */
    @GetMapping("display")
    public BaseResultEntity display(String type,Integer operation){
        if (operation==null) {
            operation = 0;
        }
        if (StringUtils.isBlank(type) && operation!=0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"type");
        }
        return BaseResultEntity.success(marketService.display(type,operation));
    }

}
