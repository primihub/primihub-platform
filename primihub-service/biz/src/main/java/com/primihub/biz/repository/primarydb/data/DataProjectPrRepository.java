package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectResource;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataProjectPrRepository {
    /**
     * 增加项目信息
     * @return
     */
    void saveProject(DataProject project);

    void editProject(DataProject project);

    void updateProjectAuthNum(Long projectId);

    void saveProjectResource(@Param("projectResources") List<DataProjectResource> projectResources);

    void delDataProject(Long projectId);

    void updateProjectResourceStatus(@Param("status")Integer status,@Param("projectId")Long projectId,@Param("resourceId")Long resourceId);
}
