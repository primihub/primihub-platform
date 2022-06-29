package com.primihub.biz.repository.secondarydb.data;

import com.primihub.biz.entity.data.po.DataComponent;
import com.primihub.biz.entity.data.po.DataModel;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.entity.data.vo.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface DataModelRepository {
    List<ProjectModelVo> queryModelListByProjectId(Long projectId);

    List<ProjectModelNumVo> queryModelCountByProjectId(@Param("projectIds")Set<Long> projectIds);

    ModelVo queryModelById(Long modelId);

    DataModel  queryDataModelById(Long modelId);

    List<ModelResourceVo> queryModelResource(Long modelId);

    List<ModelQuotaVo> queryModelQuotaVoList(Long modelId);


    List<ModelListVo> queryModelList(Map<String,Object> paramMap);

    Integer queryModelListCount(Map<String,Object> paramMap);

    List<DataComponent> queryModelComponentByParams(@Param("modelId")Long modelId,@Param("componentCode") String componentCode);

    ModelComponentJson queryModelComponenJsonByUserId(@Param("userId")Long userId, @Param("isDraft") Integer isDraft, @Param("modelId")Long modelId);


}
