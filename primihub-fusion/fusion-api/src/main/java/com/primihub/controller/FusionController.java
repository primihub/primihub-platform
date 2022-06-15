package com.primihub.controller;

import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.entity.fusion.param.RegisterConnectionParam;
import com.primihub.service.FusionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RequestMapping("fusion")
@RestController
public class FusionController {

    @Autowired
    private FusionService fusionService;

    @RequestMapping("healthConnection")
    public BaseResultEntity healthConnection(){
        return BaseResultEntity.success(System.currentTimeMillis());
    }

    @RequestMapping("registerConnection")
    public BaseResultEntity registerConnection(RegisterConnectionParam registerConnectionParam){
        if(registerConnectionParam.getGlobalId()==null||registerConnectionParam.getGlobalId().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"globalId");
        if(registerConnectionParam.getGlobalName()==null||registerConnectionParam.getGlobalName().trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"globalName");
        if(registerConnectionParam.getPinCode()==null||registerConnectionParam.getPinCode().trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"pinCode");
        return fusionService.registerConnection(registerConnectionParam);
    }

    @RequestMapping("changeConnection")
    public BaseResultEntity changeConnection(String globalId,String globalName){
        if(globalId==null||globalId.equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"globalId");
        if(globalName==null||globalName.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"globalName");
        return fusionService.changeConnection(globalId,globalName);
    }

    @RequestMapping("findOrganByGlobalId")
    public BaseResultEntity findOrganByGlobalId(String[] globalIdArray){
        if(globalIdArray==null||globalIdArray.length==0)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"globalIdArray");
        return fusionService.findOrganByGlobalId(globalIdArray);
    }

}
