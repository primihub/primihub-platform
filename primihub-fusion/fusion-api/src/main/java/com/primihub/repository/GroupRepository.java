package com.primihub.repository;

import com.primihub.entity.fusion.po.FusionOrgan;
import com.primihub.entity.group.po.FusionGo;
import com.primihub.entity.group.po.FusionGroup;
import com.primihub.entity.group.vo.FusionGroupVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GroupRepository {
    void insertFusionGroup(FusionGroup fusionGroup);
    List<FusionGroupVo> findAllFusionGroup();
    void insertFusionGo(FusionGo fusionGo);
    void deleteByUniqueCon(Long groupId,String organGlobalId);
    boolean selectFusionGo(Long groupId,String organGlobalId);
    List<Long> findOrganInGroup(String globalId);
    List<FusionOrgan> findOrganDetailInGroup(Long groupId);
}
