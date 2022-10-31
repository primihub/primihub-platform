package com.primihub.biz.repository.primarydb.data;

import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.po.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataResourcePrRepository {
    /**
     * 创建资源信息
     * @param dataResource
     * @return
     */
    void saveResource(DataResource dataResource);
    /**
     * 添加db连接信息
     * @param dataSource
     */
    void saveSource(DataSource dataSource);

    /**
     * 修改资源信息
     * @param dataResource
     * @return
     */
    void editResource(DataResource dataResource);

    /**
     * 删除资源信息
     * @param resourceId
     * @return
     */
    void deleteResource(Long resourceId);

    /**
     * 删除关联关系
     * @param resourceId
     * @return
     */
    void deleteRelationResourceTag(@Param("tags") List<Long> tags,@Param("resourceId") Long resourceId);

    /**
     * 删除标签信息
     * @return
     */
    void deleteResourceTag(@Param("tags") List<Long> tags);

    /**
     * 保存标签
     * @param tag
     * @return
     */
    void saveResourceTag(DataResourceTag tag);

    /**
     * 资源和标签绑定
     * @param tagId
     * @param resourceId
     * @return
     */
    void saveResourceTagRelation(@Param("tagId") Long tagId, @Param("resourceId") Long resourceId);


    void updateAuthRecordStatus(@Param("status")Integer status,@Param("recordId")Long recordId,@Param("userId")Long userId,@Param("userName")String userName);

    /**
     * 批量增加字段名信息
     * @param list
     */
    void saveResourceFileFieldBatch(@Param("list")List<DataFileField> list);

    /**
     * 修改资源字段名称信息
     * @param fileField
     */
    void updateResourceFileField(DataFileField fileField);

    void saveVisibilityAuth(@Param("list")List<DataResourceVisibilityAuth> list);

    void deleteVisibilityAuthByResourceId(@Param("resourceId") Long resourceId);

    void updateVisibilityAuthName(@Param("organName") String organName,@Param("organGlobalId")String organGlobalId);

}
