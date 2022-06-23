package com.primihub.service;

import com.primihub.entity.base.BaseResultEntity;
import com.primihub.entity.fusion.po.FusionOrgan;
import com.primihub.entity.group.po.FusionGo;
import com.primihub.entity.group.po.FusionGroup;
import com.primihub.entity.group.vo.FusionGroupVo;
import com.primihub.repository.FusionRepository;
import com.primihub.repository.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupService {

    @Autowired
    private GroupRepository groupRepository;
    @Autowired
    private FusionRepository fusionRepository;

    public BaseResultEntity createGroup(String globalId,String groupName){
        FusionGroup fusionGroup=new FusionGroup();
        fusionGroup.setGroupName(groupName);
        fusionGroup.setGroupOrganId(globalId);
        fusionGroup.setIsDel(0);
        groupRepository.insertFusionGroup(fusionGroup);
        FusionGo fusionGo=new FusionGo();
        fusionGo.setIsDel(0);
        fusionGo.setGroupId(fusionGroup.getId());
        fusionGo.setOrganGlobalId(globalId);
        groupRepository.insertFusionGo(fusionGo);
        Map result=new HashMap<>();
        result.put("group",fusionGroup);
        return BaseResultEntity.success(result);
    }

    public BaseResultEntity findAllGroup(String globalId){
        List<FusionGroupVo> groupList=groupRepository.findAllFusionGroup();
        List<Long> organInGroupList=groupRepository.findOrganInGroup(globalId);
        Set<Long> organInGroupSet=organInGroupList.stream().collect(Collectors.toSet());
        for(FusionGroupVo item:groupList){
            if(organInGroupSet.contains(item.getId())){
                item.setIn(true);
            }
        }
        Map result=new HashMap<>();
        result.put("groupList",groupList);
        return BaseResultEntity.success(result);
    }

    public BaseResultEntity joinGroup(String globalId,Long groupId){
        if(groupRepository.selectFusionGo(groupId,globalId))
            return BaseResultEntity.success();
        FusionGo fusionGo=new FusionGo();
        fusionGo.setIsDel(0);
        fusionGo.setGroupId(groupId);
        fusionGo.setOrganGlobalId(globalId);
        groupRepository.insertFusionGo(fusionGo);
        return BaseResultEntity.success();
    }

    public BaseResultEntity exitGroup(String globalId,Long groupId){
        groupRepository.deleteByUniqueCon(groupId,globalId);
        return BaseResultEntity.success();
    }

    public BaseResultEntity findOrganInGroup(Long groupId){
        Map result=new HashMap<>();
        result.put("organList",groupRepository.findOrganDetailInGroup(groupId));
        return BaseResultEntity.success(result);
    }

    public BaseResultEntity findMyGroupOrgan(String globalId){
        List<Long> organInGroupList=groupRepository.findOrganInGroup(globalId);
        if(organInGroupList==null||organInGroupList.size()==0)
            return BaseResultEntity.success(new ArrayList());
        List<String> organIdList = groupRepository.findOrganGlobalIdByGroupIdList(organInGroupList);
        Set<String> organIdSet=organIdList.stream().filter(item->!item.equals(globalId)).collect(Collectors.toSet());
        if(organIdSet.size()==0)
            return BaseResultEntity.success(new ArrayList());
        List<FusionOrgan> organList = fusionRepository.selectFusionOrganByGlobalIds(organIdSet);
        Map result=new HashMap<>();
        result.put("organList",organList);
        return BaseResultEntity.success(result);
    }

}
