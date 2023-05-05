package com.primihub.application.controller.share;


import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.vo.ShareModelVo;
import com.primihub.biz.entity.data.vo.ShareProjectVo;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.service.data.DataModelService;
import com.primihub.biz.service.data.DataProjectService;
import com.primihub.biz.service.sys.SysOrganService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("shareData")
@RestController
public class ShareDataController {

    @Autowired
    private DataProjectService dataProjectService;
    @Autowired
    private DataModelService dataModelService;
    @Autowired
    private SysOrganService sysOrganService;

    /**
     * 创建编辑项目接口
     * @return
     */
    @RequestMapping("syncProject")
    public BaseResultEntity syncProject(@RequestBody ShareProjectVo vo){
        return dataProjectService.syncProject(vo);
    }

    /**
     * 创建编辑项目接口
     * @return
     */
    @RequestMapping("syncModel")
    public BaseResultEntity syncModel(@RequestBody ShareModelVo vo){
        return dataModelService.syncModel(vo);
    }

    @RequestMapping("apply")
    public BaseResultEntity applyForJoinNode(@RequestBody Map<String,Object> info){
        if (info==null){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"info");
        }
        if (!info.containsKey("applyId")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"applyId");
        }
        if (!info.containsKey("organId")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        }
        if (!info.containsKey("organName")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organName");
        }
        if (!info.containsKey("gateway")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"gateway");
        }
        if (!info.containsKey("publicKey")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"publicKey");
        }
        return sysOrganService.applyForJoinNode(info);
    }



}
