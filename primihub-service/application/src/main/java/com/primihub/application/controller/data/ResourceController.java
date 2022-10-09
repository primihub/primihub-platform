package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.DataResourceAuthType;
import com.primihub.biz.entity.data.dataenum.FieldTypeEnum;
import com.primihub.biz.entity.data.dataenum.ResourceStateEnum;
import com.primihub.biz.entity.data.req.DataResourceFieldReq;
import com.primihub.biz.entity.data.req.DataResourceReq;
import com.primihub.biz.entity.data.req.PageReq;
import com.primihub.biz.service.data.DataResourceService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RequestMapping("resource")
@RestController
public class ResourceController {

    @Autowired
    private DataResourceService dataResourceService;

    /**
     * 获取资源标签列表
     * @return
     */
    @GetMapping("getResourceTags")
    public BaseResultEntity getResourceTags(){
        return dataResourceService.getResourceTags();
    }

    /**
     * 资源概览列表信息接口
     * @param userId    用户id
     * @param req       分页、条件信息
     * @return
     */
    @GetMapping("getdataresourcelist")
    public BaseResultEntity getDataResourceList(@RequestHeader("userId") Long userId,
                                                DataResourceReq req){
        return dataResourceService.getDataResourceList(req,userId);
    }

    /**
     * 增加或删除资源接口
     * @param req       资源信息
     * @param userId    用户id
     * @return
     */
    @PostMapping("saveorupdateresource")
    public BaseResultEntity saveDataResource(@RequestBody DataResourceReq req,
                                             @RequestHeader("userId") Long userId){
        if (req.getResourceId()!=null&&req.getResourceId()!=0L){
            if ((req.getResourceName()==null||req.getResourceName().trim().equals(""))
                    &&(req.getResourceDesc()==null||req.getResourceDesc().trim().equals(""))
                    &&(req.getResourceAuthType()==null || req.getResourceAuthType()==0)
                    &&(req.getPublicOrganId()==null||req.getPublicOrganId().trim().equals(""))
                    &&(req.getResourceSource()==null || req.getResourceSource()==0)
                    &&(req.getTags()==null||req.getTags().size()==0)){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"至少需要一个修改的字段");
            }
            if(!DataResourceAuthType.AUTH_TYPE_MAP.containsKey(req.getResourceAuthType()))
                return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"resourceAuthType");
            return dataResourceService.editDataResource(req,userId);
        }else {
            if (req.getResourceName()==null||req.getResourceName().trim().equals("")){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceName");
            }
            if (req.getResourceDesc()==null||req.getResourceDesc().trim().equals("")){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceDesc");
            }
            if (req.getResourceAuthType()==null || req.getResourceAuthType()==0){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceAuthType");
            }

            if(!DataResourceAuthType.AUTH_TYPE_MAP.containsKey(req.getResourceAuthType()))
                return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"resourceAuthType");
            if (req.getResourceSource()==null || req.getResourceSource()==0){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceSource");
            }
            if (req.getTags()==null||req.getTags().size()==0){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"tags");
            }
            if (req.getFileId()==null||req.getFileId()==0L){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileId");
            }
            if (req.getFieldList()==null || req.getFieldList().size()==0)
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fieldList");
            return dataResourceService.saveDataResource(req,userId);
        }
    }

    /**
     * 查询单个资源信息
     * @param resourceId    资源id
     * @return
     */
    @GetMapping("getdataresource")
    public BaseResultEntity getDataResource(Long resourceId){
        if (resourceId==null){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        return dataResourceService.getDataResource(resourceId);

    }

    /**
     * 删除一个资源信息
     * @param resourceId
     * @return
     */
    @GetMapping("deldataresource")
    public BaseResultEntity deleteDataResource(Long resourceId){
        if (resourceId==null){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM);
        }
        return dataResourceService.deleteDataResource(resourceId);
    }

    @RequestMapping("resourceFilePreview")
    public BaseResultEntity resourceFilePreview(Long fileId,String resourceId){
        if (StringUtils.isBlank(resourceId)){
            if(fileId==null||fileId==0L)
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileId or resourceId");
        }
        return dataResourceService.resourceFilePreview(fileId,resourceId);
    }


    /**
     * 获取资源文件字段
     * @param resourceId
     * @return
     */
    @RequestMapping("getDataResourceFieldPage")
    public BaseResultEntity getDataResourceFieldPage(@RequestHeader("userId") Long userId,
                                                     Long resourceId,
                                                     PageReq req){
        if (resourceId==null||resourceId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        if (userId==null||userId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        return dataResourceService.getDataResourceFieldPage(resourceId,req,userId);
    }

    /**
     * 修改字段信息
     * @return
     */
    @RequestMapping("updateDataResourceField")
    public BaseResultEntity updateDataResourceField(DataResourceFieldReq req){
        if (req.getFieldId()==null||req.getFieldId()==0L)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fieldId");
        FieldTypeEnum fieldTypeEnum = null;
        if (StringUtils.isNotBlank(req.getFieldType())){
            fieldTypeEnum = FieldTypeEnum.getEnumByTypeName(req.getFieldType());
            if (fieldTypeEnum==null)
                return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"类型错误");
        }
        return dataResourceService.updateDataResourceField(req,fieldTypeEnum);
    }


    @RequestMapping("resourceStatusChange")
    public BaseResultEntity resourceStatusChange(Long resourceId,Integer resourceState){
        if (resourceId==null||resourceId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        if (resourceState==null){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceState");
        }
        if (!ResourceStateEnum.RESOURCE_STATE_MAP.containsKey(resourceState))
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"resourceState");
        return dataResourceService.resourceStatusChange(resourceId,resourceState);
    }


}
