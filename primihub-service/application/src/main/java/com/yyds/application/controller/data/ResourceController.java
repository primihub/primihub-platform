package com.yyds.application.controller.data;

import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.base.BaseResultEnum;
import com.yyds.biz.entity.data.dataenum.FieldTypeEnum;
import com.yyds.biz.entity.data.req.DataResourceFieldReq;
import com.yyds.biz.entity.data.req.DataResourceReq;
import com.yyds.biz.entity.data.req.PageReq;
import com.yyds.biz.service.data.DataResourceService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RequestMapping("resource")
@RestController
public class ResourceController {

    @Autowired
    private DataResourceService dataResourceService;

    /**
     * 资源概览列表信息接口
     * @param userId    用户id
     * @param organId   机构id
     * @param req       分页、条件信息
     * @return
     */
    @GetMapping("getdataresourcelist")
    public BaseResultEntity getDataResourceList(@RequestHeader("userId") Long userId,
                                                @RequestHeader("organId")Long organId,
                                                DataResourceReq req){
        if (organId<0){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        }
        return dataResourceService.getDataResourceList(req,userId,organId,false);
    }

    /**
     * 增加或删除资源接口
     * @param req       资源信息
     * @param userId    用户id
     * @param organId   机构id
     * @return
     */
    @PostMapping("saveorupdateresource")
    public BaseResultEntity saveDataResource(DataResourceReq req,
                                             @RequestHeader("userId") Long userId,
                                             @RequestHeader("organId")Long organId){
        if (organId<0){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        }
        if (req.getResourceId()!=null&&req.getResourceId()!=0L){
            if ((req.getResourceName()==null||req.getResourceName().trim().equals(""))
                    &&(req.getResourceDesc()==null||req.getResourceDesc().trim().equals(""))
                    &&(req.getResourceSortType()==null||req.getResourceSortType()==0)
                    &&(req.getResourceAuthType()==null || req.getResourceAuthType()==0)
                    &&(req.getResourceSource()==null || req.getResourceSource()==0)
                    &&(req.getTags()==null||req.getTags().size()==0)){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"至少需要一个修改的字段");
            }
            return dataResourceService.editDataResource(req,userId,organId);
        }else {
            if (req.getResourceName()==null||req.getResourceName().trim().equals("")){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceName");
            }
            if (req.getResourceDesc()==null||req.getResourceDesc().trim().equals("")){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceDesc");
            }
            if (req.getResourceSortType()==null||req.getResourceSortType()==0){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceSortType");
            }
            if (req.getResourceAuthType()==null || req.getResourceAuthType()==0){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceAuthType");
            }
            if (req.getResourceSource()==null || req.getResourceSource()==0){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceSource");
            }
            if (req.getTags()==null||req.getTags().size()==0){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"tags");
            }
            if (req.getFileId()==null||req.getFileId()==0L){
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"fileId");
            }
            return dataResourceService.saveDataResource(req,userId,organId);
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

    /**
     * 资源审核列表
     * @param userId
     * @param req
     * @return
     */
    @GetMapping("getauthorizationlist")
    public BaseResultEntity getAuthorizationList(@RequestHeader("userId") Long userId,
                                                 Integer status,
                                                 PageReq req){
        return dataResourceService.getAuthorizationList(req,userId,status);
    }

    /**
     * 审批
     * @param userId
     * @param recordId
     * @param status 1 通过 2拒绝
     * @return
     */
    @PostMapping("approval")
    public BaseResultEntity approvalAuthorization(@RequestHeader("userId") Long userId,
                                                  Long recordId,Integer status){
        if (recordId==null||recordId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"recordId");
        }
        if (status==0||status>2){
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"status");
        }
        return dataResourceService.approvalauthorization(recordId,userId,status);
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



}
