package com.yyds.biz.repository.secondarydb.data;

import com.yyds.biz.entity.data.po.DataProject;
import com.yyds.biz.entity.data.po.DataProjectResource;
import com.yyds.biz.entity.data.po.DataResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DataProjectRepository {
    /**
     * 查询
     * @param paramMap
     * @return
     */
    List<DataProject> queryDataProject(Map<String,Object> paramMap);

    Integer queryDataProjectCount(Map<String,Object> paramMap);

    DataProject queryDataProjectById(Long projectId);

    List<DataProjectResource> queryProjectResourceByProjectId(Long projectId);

}
