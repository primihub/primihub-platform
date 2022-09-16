package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.DataProjectOrgan;
import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectResource;
import com.primihub.biz.entity.data.req.DataProjectQueryReq;
import com.primihub.biz.entity.data.req.PageReq;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface DataProjectRepository {

    DataProject selectDataProjectByProjectId(@Param("id") Long id, @Param("projectId") String projectId);
    List<DataProject> selectDataProjectPage(DataProjectQueryReq req);
    Integer selectDataProjectCount(DataProjectQueryReq req);

    DataProjectOrgan selectDataProjcetOrganById(@Param("id") Long id);
    DataProjectOrgan selectDataProjcetOrganByProjectIdAndOrganId(@Param("projectId") String projectId,@Param("organId") String organId);
    List<DataProjectOrgan> selectDataProjcetOrganByProjectId(@Param("projectId") String projectId);
    List<DataProjectOrgan> selectDataProjcetOrganByProjectIds(@Param("projectIds") Set<String> projectIds);


    DataProjectResource selectProjectResourceById(@Param("id") Long id);
    List<DataProjectResource> selectProjectResourceByProjectId(@Param("projectId") String projectId);
    List<DataProjectResource> selectProjectResourceByProjectIdAndOrganId(@Param("projectId") String projectId,@Param("organId") String organId);
    List<DataProjectResource> selectProjectResourceByOrganIdPage(Map<String,Object> paramMap);
    Integer selectProjectResourceByOrganIdCount(Map<String,Object> paramMap);
    List<DataProjectResource> selectProjectResourceByProjectIds(@Param("projectIds") Set<String> projectIds);

    List<DataProjectResource> selectProjectResourceByResourceIds(@Param("resourceIds") Set<String> resourceIds);




    List<Map<String,Object>> selectProjectStatics(@Param("organId")String organId);
}
