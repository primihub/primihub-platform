package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectOrgan;
import com.primihub.biz.entity.data.po.DataProjectResource;
import com.primihub.biz.entity.data.req.DataProjectQueryReq;
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
    DataProjectOrgan selectDataProjectOrganById(@Param("id") Long id);
    DataProjectOrgan selectDataProjectOrganByProjectIdAndOrganId(@Param("projectId") String projectId,@Param("organId") String organId);
    List<DataProjectOrgan> selectDataProjectOrganByProjectId(@Param("projectId") String projectId);
    List<DataProjectOrgan> selectDataProjectOrganByProjectIds(@Param("projectIds") Set<String> projectIds);
    DataProjectResource selectProjectResourceById(@Param("id") Long id);
    List<DataProjectResource> selectProjectResourceByProjectId(@Param("projectId") String projectId);
    List<DataProjectResource> selectProjectResourceByProjectIdAndOrganId(@Param("projectId") String projectId,@Param("organId") String organId);
    List<DataProjectResource> selectProjectResourceByOrganIdPage(Map<String,Object> paramMap);
    Integer selectProjectResourceByOrganIdCount(Map<String,Object> paramMap);
    List<DataProjectResource> selectProjectResourceByProjectIds(@Param("projectIds") Set<String> projectIds);

    List<DataProjectResource> selectProjectResourceByResourceIds(@Param("resourceIds") Set<String> resourceIds);




    List<Map<String,Object>> selectProjectStatics(@Param("organId")String organId);
}
