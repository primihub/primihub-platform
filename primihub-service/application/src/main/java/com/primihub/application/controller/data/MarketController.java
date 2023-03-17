package com.primihub.application.controller.data;


import com.primihub.biz.entity.base.BaseJsonParam;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.req.DataVisitingUsersReq;
import com.primihub.biz.service.data.MarketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        if (param == null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"visitingUsersList");
        if (param.size()!=7)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"visitingUsersList 数量不够");
        for (DataVisitingUsersReq dv : param) {
            if (dv.isNull()){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"存在null值");
            }
        }
        return marketService.submitVisitingUsers(param);
    }

    @GetMapping("getvisitingusers")
    public BaseResultEntity getVisitingUsers(){
        return marketService.getVisitingUsers();
    }

}
