package com.primihub.application.controller.data;


import com.primihub.biz.entity.base.BaseJsonParam;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.req.DataModelAndComponentReq;
import com.primihub.biz.entity.data.req.DataModelReq;
import com.primihub.biz.entity.data.req.ModelTaskSuccessReq;
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
    public BaseResultEntity getDataModel(Long taskId){
        if (taskId==null||taskId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return dataModelService.getDataModel(taskId);
    }

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
                                                    Long modelId,
                                                    Long projectId){
        if (userId==null||userId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        return dataModelService.getModelComponentDetail(modelId,userId,projectId);
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
     * @param modelId
     * @return
     */
    @GetMapping("deleteModel")
    public BaseResultEntity deleteModel(Long modelId){
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
    public BaseResultEntity runTaskModel(@RequestHeader("userId") Long userId,Long modelId){
        if (modelId==null||modelId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelId");
        }
        return dataModelService.runTaskModel(modelId,userId);
    }

    /**
     * 重启模型
     * @param taskId
     * @return
     */
    @GetMapping("restartTaskModel")
    public BaseResultEntity restartTaskModel(Long taskId){
        if (taskId==null||taskId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return dataModelService.restartTaskModel(taskId);
    }

    /**
     * 获取运行模型信息
     * @param taskId
     * @return
     */
    @GetMapping("getTaskModelComponent")
    public BaseResultEntity getTaskModelComponent(Long taskId){
        if (taskId==null||taskId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return dataModelService.getTaskModelComponent(taskId);
    }

    @RequestMapping("getModelPrediction")
    public BaseResultEntity getModelPrediction(Long modelId){
        if (modelId==null||modelId==0L)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelId");
        return dataModelService.getModelPrediction(modelId);
    }

    @RequestMapping("getModelTaskSuccessList")
    public BaseResultEntity getModelTaskSuccessList(@RequestHeader("userId") Long userId,ModelTaskSuccessReq req){
        if (userId==null||userId==0L)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        req.setUserId(userId);
        return dataModelService.getModelTaskSuccessList(req);
    }




}
