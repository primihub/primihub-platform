package com.primihub.application.controller.data;


import com.primihub.biz.entity.base.BaseJsonParam;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.req.DataModelAndComponentReq;
import com.primihub.biz.entity.data.req.DataModelReq;
import com.primihub.biz.entity.data.req.PageReq;
import com.primihub.biz.service.data.DataModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RequestMapping("model")
@RestController
public class ModelController {

    @Autowired
    private DataModelService dataModelService;

    /**
     * 创建模型
     * @return
     */
    @PostMapping("savemodel")
    public BaseResultEntity saveDataProject(@RequestHeader("userId") Long userId,
                                            @RequestHeader("organId")Long organId,
                                            DataModelReq req){
        if (organId<0){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        }
        if (req.getProjectId()==null || req.getProjectId()==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"projectId");
        }
        if (req.getModelName()==null || req.getModelName().trim().equals("")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelName");
        }
        if (req.getModelDesc()==null || req.getModelDesc().trim().equals("")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelDesc");
        }
        if (req.getModelType()==null || req.getModelType()==0){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelType");
        }
        if (req.getYValueColumn()==null || req.getYValueColumn().trim().equals("")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"yValueColumn");
        }
        if (req.getResourceId()==null||req.getResourceId()==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resourceId");
        }
        return dataModelService.saveDataModel(userId,organId,req);
    }


    /**
     * 获取模型列表
     * @return
     */
    @GetMapping("getmodellist")
    public BaseResultEntity getDataModelList(String projectName,
                                             String modelName,
                                             Integer taskStatus,
                                             Long projectId,
                                             PageReq req){
        return dataModelService.getDataModelList(req,projectName,modelName,taskStatus,projectId);
    }

    /**
     * 获取模型详情
     * @return
     */
    @GetMapping("getdatamodel")
    public BaseResultEntity getDataProject(Long modelId){
        if (modelId==null||modelId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelId");
        }
        return dataModelService.getDataModel(modelId);
    }

    //---------------------------------v0.2----------------------------------
    /**
     * 获取模型详情
     * @return
     */
    @GetMapping("getModelComponent")
    public BaseResultEntity getModelComponent(){
        return dataModelService.getModelComponent();
    }

    /**
     * 获取模型详情
     * @return
     */
    @GetMapping("getModelComponentDetail")
    public BaseResultEntity getModelComponentDetail(@RequestHeader("userId") Long userId,
                                                    Long modelId){
        if (userId==null||userId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        return dataModelService.getModelComponentDetail(modelId,userId);
    }
    /**
     * 创建模型
     * @return
     */
    @PostMapping("saveModelAndComponent")
    public BaseResultEntity saveModelAndComponent(@RequestHeader("userId") Long userId,
                                                  @RequestBody BaseJsonParam<DataModelAndComponentReq> req){
        if (userId<0){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        return dataModelService.saveModelAndComponent(userId,req.getParam());
    }

    /***
     *  删除模型
     * @param userId
     * @param modelId
     * @return
     */
    @GetMapping("deleteModel")
    public BaseResultEntity deleteModel(@RequestHeader("userId") Long userId,
                                         Long modelId){
        if (userId<0){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        if (modelId==null||modelId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelId");
        }
        return dataModelService.deleteModel(modelId);
    }

    /**
     * 运行模型
     * @param modelId
     * @return
     */
    @GetMapping("runTaskModel")
    public BaseResultEntity runTaskModel(Long modelId){
        if (modelId==null||modelId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelId");
        }
        return dataModelService.runTaskModel(modelId);
    }

    /**
     * 获取运行模型信息
     * @param modelId
     * @return
     */
    @GetMapping("getTaskModelComponent")
    public BaseResultEntity getTaskModelComponent(Long modelId){
        if (modelId==null||modelId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelId");
        }
        return dataModelService.getTaskModelComponent(modelId);
    }

    @RequestMapping("getModelPrediction")
    public BaseResultEntity getModelPrediction(Long modelId){
        if (modelId==null||modelId==0L)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelId");
        return dataModelService.getModelPrediction(modelId);
    }


}
