package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.DataProjectOrgan;
import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectResource;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface DataProjectPrRepository {

    void saveDataProject(DataProject dataProject);

    void updateDataProject(DataProject dataProject);

    void saveDataProjcetOrgan(DataProjectOrgan dataProjectOrgan);

    void updateDataProjcetOrgan(DataProjectOrgan dataProjectOrgan);

    void saveDataProjectResource(DataProjectResource dataProjectResource);

    void updateDataProjectResource(DataProjectResource dataProjectResource);

    void deleteDataProjectOrgan(@Param("id") Long id,@Param("projectId") String projectId);

    void deleteDataProject(@Param("id") Long id,@Param("projectId") String projectId);

    void deleteDataProjectResource(@Param("id") Long id,@Param("projectId") String projectId);

}
