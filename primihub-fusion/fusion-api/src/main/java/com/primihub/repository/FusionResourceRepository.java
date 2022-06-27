package com.primihub.repository;


import com.primihub.entity.resource.param.ResourceParam;
import com.primihub.entity.resource.po.FusionResource;
import com.primihub.entity.resource.po.FusionResourceVisibilityAuth;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

@Mapper
public interface FusionResourceRepository {
    List<FusionResource> selectFusionResourceById(@Param("resourceIds") Set<String> resourceIds);

    List<FusionResource> selectFusionResource(ResourceParam param);

    FusionResource selectFusionResourceByResourceId(@Param("resourceId") String resourceId);
    List<FusionResource> selectFusionResourceByResourceIds(@Param("resourceIds") Set<String> resourceIds);

    List<String> selectFusionResourceTag();

    Integer selectFusionResourceCount(ResourceParam param);

    void saveFusionResource(FusionResource fusionResource);

    void updateFusionResource(FusionResource fusionResource);

    void saveBatchResourceTag(@Param("tags") Set<String> tags);

    void saveBatchResourceAuthOrgan(@Param("authOrganList")List<FusionResourceVisibilityAuth> authOrganList);

    void deleteResourceAuthOrgan(@Param("resourceId")String resourceId);
}
