package com.primihub.repository;

import com.primihub.entity.fusion.FusionOrgan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface FusionRepository {
    void insertFusionOrgan(FusionOrgan fusionOrgan);
    void updateFusionOrganSpeByGlobalId(FusionOrgan fusionOrgan);

    FusionOrgan getFusionOrganByGlobalId(String globalId);
    List<FusionOrgan> selectFusionOrganByGlobalIds(@Param("globalIds") Set<String> globalIds);

}
