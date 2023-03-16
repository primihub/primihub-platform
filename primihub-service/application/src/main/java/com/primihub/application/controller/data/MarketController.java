package com.primihub.application.controller.data;


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
    public BaseResultEntity submitVisitingUsers(@RequestBody List<DataVisitingUsersReq> req){
        if (req == null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"visitingUsersList");
        if (req.size()!=7)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"visitingUsersList 数量不够");
        for (DataVisitingUsersReq dv : req) {
            if (dv.isNull()){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"存在null值");
            }
        }
        return marketService.submitVisitingUsers(req);
    }

    @GetMapping("getvisitingusers")
    public BaseResultEntity getVisitingUsers(){
        return marketService.getVisitingUsers();
    }

}
