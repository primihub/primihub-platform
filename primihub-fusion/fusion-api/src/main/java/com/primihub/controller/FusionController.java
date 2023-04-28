package com.primihub.controller;

import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.entity.fusion.param.FusionOrganExtendsParam;
import com.primihub.entity.fusion.param.FusionConnectionParam;
import com.primihub.service.FusionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public BaseResultEntity registerConnection(FusionConnectionParam fusionConnectionParam){
        if(fusionConnectionParam.getGlobalId()==null|| "".equals(fusionConnectionParam.getGlobalId())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"globalId");
        }
        if(fusionConnectionParam.getGlobalName()==null|| "".equals(fusionConnectionParam.getGlobalName().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"globalName");
        }
        if(fusionConnectionParam.getGatewayAddress()==null|| "".equals(fusionConnectionParam.getGatewayAddress().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"gatewayAddress");
        }
        if ("".equals(fusionConnectionParam.getPublicKey().trim())|| "".equals(fusionConnectionParam.getPrivateKey().trim())) {
            return  BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"secret");
        }
        return fusionService.registerConnection(fusionConnectionParam);
    }

    @RequestMapping("changeConnection")
    public BaseResultEntity changeConnection(FusionConnectionParam fusionConnectionParam){
        if(fusionConnectionParam.getGlobalName()==null|| "".equals(fusionConnectionParam.getGlobalName().trim())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"globalName");
        }
        if(fusionConnectionParam.getGatewayAddress()==null|| "".equals(fusionConnectionParam.getGatewayAddress())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"gatewayAddress");
        }
        return fusionService.changeConnection(fusionConnectionParam);
    }

    @RequestMapping("findOrganByGlobalId")
    public BaseResultEntity findOrganByGlobalId(String[] globalIdArray){
        if(globalIdArray==null||globalIdArray.length==0) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"globalIdArray");
        }
        return fusionService.findOrganByGlobalId(globalIdArray);
    }

    @RequestMapping("getOrganAllList")
    public BaseResultEntity getOrganAllList(){
        return fusionService.getOrganAllList();
    }

    @RequestMapping("changeOrganExtends")
    public BaseResultEntity changeOrganExtends(FusionOrganExtendsParam param){
        if (StringUtils.isEmpty(param.getIp())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"ip");
        }
        if (StringUtils.isEmpty(param.getLat())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"lat");
        }
        if (StringUtils.isEmpty(param.getLon())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"lon");
        }
        return fusionService.changeOrganExtends(param);
    }

    @RequestMapping("getOrganExtendsList")
    public BaseResultEntity getOrganExtendsList(){
        return fusionService.getOrganExtendsList();
    }

}
