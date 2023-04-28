package com.primihub.repository;

import com.primihub.entity.fusion.po.FusionOrganExtends;
import com.primihub.entity.fusion.po.FusionOrgan;
import com.primihub.entity.fusion.vo.OrganExtendsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface FusionRepository {
    void insertFusionOrgan(FusionOrgan fusionOrgan);
    void updateFusionOrganSpeByGlobalId(FusionOrgan fusionOrgan);

    FusionOrgan getFusionOrganByGlobalId(String globalId);
    FusionOrganExtends getFusionOrganExtendsByGlobalId(Long globalId);
    List<OrganExtendsVo> getFusionOrganExtends();
    List<FusionOrgan> selectFusionOrganByGlobalIds(@Param("globalIds") Set<String> globalIds);
    List<FusionOrgan> selectFusionOrganAllList();


    void insertFusionOrganExtends(FusionOrganExtends fusionOrganExtends);
    void updateFusionOrganExtends(FusionOrganExtends fusionOrganExtends);

}
