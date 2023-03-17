package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.service.sys.SysFusionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("fusion")
@RestController
public class FusionController {

    @Autowired
    private SysFusionService sysFusionService;

    @RequestMapping("healthConnection")
    public BaseResultEntity healthConnection(String serverAddress){
        if(serverAddress==null||serverAddress.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        return sysFusionService.healthConnection(serverAddress);
    }

    @RequestMapping("registerConnection")
    public BaseResultEntity registerConnection(String serverAddress){
        if(serverAddress==null||serverAddress.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        return sysFusionService.registerConnection(serverAddress);
    }

    @RequestMapping("deleteConnection")
    public BaseResultEntity deleteConnection(String serverAddress){
        if(serverAddress==null||serverAddress.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        return sysFusionService.deleteConnection(serverAddress);
    }

    @RequestMapping("createGroup")
    public BaseResultEntity createGroup(String serverAddress,String groupName){
        if(serverAddress==null||serverAddress.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        if(groupName==null||groupName.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"groupName");
        return sysFusionService.createGroup(serverAddress,groupName);
    }

    @RequestMapping("findAllGroup")
    public BaseResultEntity findAllGroup(String serverAddress){
        if(serverAddress==null||serverAddress.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        return sysFusionService.findAllGroup(serverAddress);
    }

    @RequestMapping("joinGroup")
    public BaseResultEntity joinGroup(String serverAddress,Long groupId){
        if(serverAddress==null||serverAddress.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        if(groupId==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"groupId");
        return sysFusionService.joinGroup(serverAddress,groupId);
    }

    @RequestMapping("exitGroup")
    public BaseResultEntity exitGroup(String serverAddress,Long groupId){
        if(serverAddress==null||serverAddress.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        if(groupId==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"groupId");
        return sysFusionService.exitGroup(serverAddress,groupId);
    }

    @RequestMapping("findOrganInGroup")
    public BaseResultEntity findOrganInGroup(String serverAddress,Long groupId){
        if(serverAddress==null||serverAddress.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        if(groupId==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"groupId");
        return sysFusionService.findOrganInGroup(serverAddress,groupId);
    }

    @RequestMapping("findMyGroupOrgan")
    public BaseResultEntity findMyGroupOrgan(String serverAddress){
        if(serverAddress==null||serverAddress.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        return sysFusionService.findMyGroupOrgan(serverAddress);
    }

    @RequestMapping("getOrganExtendsList")
    public BaseResultEntity getOrganExtendsList(String serverAddress){
        if(serverAddress==null||serverAddress.trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        return sysFusionService.getOrganExtendsList(serverAddress);
    }

}
