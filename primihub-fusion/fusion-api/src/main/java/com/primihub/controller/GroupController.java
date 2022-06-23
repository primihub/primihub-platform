package com.primihub.controller;

import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.base.BaseResultEnum;
import com.primihub.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("group")
@RestController
public class GroupController {

    @Autowired
    private GroupService groupService;

    @RequestMapping("createGroup")
    public BaseResultEntity createGroup(String globalId,String groupName){
        if(groupName==null||groupName.equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"groupName");
        return groupService.createGroup(globalId,groupName);
    }

    @RequestMapping("findAllGroup")
    public BaseResultEntity findAllGroup(String globalId){
        return groupService.findAllGroup(globalId);
    }

    @RequestMapping("joinGroup")
    public BaseResultEntity joinGroup(String globalId,Long groupId){
        if(groupId==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"groupId");
        return groupService.joinGroup(globalId,groupId);
    }

    @RequestMapping("exitGroup")
    public BaseResultEntity exitGroup(String globalId,Long groupId){
        if(groupId==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"groupId");
        return groupService.exitGroup(globalId,groupId);
    }

    @RequestMapping("findOrganInGroup")
    public BaseResultEntity findOrganInGroup(Long groupId){
        if(groupId==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"groupId");
        return groupService.findOrganInGroup(groupId);
    }

    @RequestMapping("findMyGroupOrgan")
    public BaseResultEntity findMyGroupOrgan(String globalId){
        return groupService.findMyGroupOrgan(globalId);
    }

}
