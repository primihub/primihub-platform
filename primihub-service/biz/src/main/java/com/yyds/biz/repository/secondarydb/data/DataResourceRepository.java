package com.yyds.biz.repository.secondarydb.data;

import com.yyds.biz.entity.data.po.DataFileField;
import com.yyds.biz.entity.data.po.DataResource;
import com.yyds.biz.entity.data.po.DataResourceAuthRecord;
import com.yyds.biz.entity.data.po.DataResourceTag;
import com.yyds.biz.entity.data.vo.DataResourceAuthRecordVo;
import com.yyds.biz.entity.data.vo.DataResourceRecordVo;
import com.yyds.biz.entity.data.vo.ResourceTagListVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface DataResourceRepository {
    /**
     * 查询所以的资源标签
     * @return
     */
    List<DataResourceTag> queryAllResourceTag();

    List<DataResource> queryDataResource(Map<String,Object> paramMap);

    List<DataResource> queryDataResourceByResourceIds(@Param("resourceIds")Set<Long> resourceIds);

    List<DataResourceTag> queryTagsByResourceId(Long resourceId);

    DataResource queryDataResourceById(Long resourceId);

    Integer queryDataResourceCount(Map<String,Object> paramMap);

    List<Long> queryResourceTagRelation(Long resourceId);

    List<DataResourceAuthRecordVo> queryResourceAuthRecord(@Param("offset") Integer offset,@Param("pageSize") Integer pageSize, @Param("userId")Long userId,@Param("status")Integer status);

    Long queryResourceAuthRecordCount(@Param("userId")Long userId,@Param("status")Integer status);

    List<DataResourceRecordVo> queryDataResourceByIds(@Param("resourceIds") Set<Long> resourceIds);

    List<ResourceTagListVo> queryDataResourceListTags(@Param("resourceIds") List<Long> resourceIds);

    DataResourceAuthRecord queryDataResourceAuthRecordById(Long recordId);

    Integer queryResourceProjectRelationCount(Long resourceId);

    List<DataFileField> queryDataFileField(Map<String,Object> paramMap);

    Integer queryDataFileFieldCount(Map<String,Object> paramMap);


}
