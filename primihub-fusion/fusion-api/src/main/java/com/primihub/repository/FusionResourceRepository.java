package com.primihub.repository;


import com.primihub.entity.resource.param.ResourceParam;
import com.primihub.entity.resource.po.FusionPublicRo;
import com.primihub.entity.resource.po.FusionResource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface FusionResourceRepository {
    List<FusionResource> selectFusionResourceById(@Param("resourceIds") Set<String> resourceIds);

    List<FusionResource> selectFusionResource(ResourceParam param);

    FusionResource selectFusionResourceByResourceId(@Param("resourceId") String resourceId);

    List<String> selectFusionResourceTag();

    Integer selectFusionResourceCount(ResourceParam param);

    void saveFusionResource(FusionResource fusionResource);

    void updateFusionResource(FusionResource fusionResource);

    void saveBatchResourceTag(@Param("tags") Set<String> tags);

    void saveBatchFusionPublicRo(@Param("publics")List<FusionPublicRo> publics);

    void deleteFusionPublicRoByResourceId(@Param("resourceId") Long resourceId);
}
