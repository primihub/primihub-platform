package com.primihub.repository;

import com.primihub.entity.fusion.po.FusionOrganExtends;
import com.primihub.entity.fusion.po.FusionOrgan;
import com.primihub.entity.fusion.po.FusionOrganSecret;
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

    void insertFusionOrganExtends(FusionOrganExtends fusionOrganExtends);
    void updateFusionOrganExtends(FusionOrganExtends fusionOrganExtends);

    void insertFusionOrganSecret(FusionOrganSecret fusionOrganSecret);
    void updateFusionOrganSecret(FusionOrganSecret fusionOrganSecret);

}
