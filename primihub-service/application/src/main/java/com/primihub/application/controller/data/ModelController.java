package com.primihub.application.controller.data;


import com.primihub.biz.entity.base.BaseJsonParam;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.service.data.DataModelService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

/**
 * 模型管理
 */
@Api(value = "模型接口",tags = "模型接口")
@RequestMapping("model")
@RestController
public class ModelController {

    @Autowired
    private DataModelService dataModelService;


    @ApiOperation(value = "获取模型列表",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(name="projectName", value = "项目名称", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name="modelName", value = "模型名称", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name="taskStatus", value = "任务状态", dataType = "Integer", paramType = "query"),
            @ApiImplicitParam(name="projectId", value = "项目ID", dataType = "Long", paramType = "query")
    })
    @GetMapping("getmodellist")
    public BaseResultEntity getDataModelList(String projectName,
                                             String modelName,
                                             Integer taskStatus,
                                             Long projectId,
                                             PageReq req){
        return dataModelService.getDataModelList(req,projectName,modelName,taskStatus,projectId);
    }


    @ApiOperation(value = "获取模型详情",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(name="userId", value = "用户ID,不用填写getaway处理", dataType = "Long", paramType = "header"),
            @ApiImplicitParam(name="taskId", value = "任务ID", dataType = "Long", paramType = "query")
    })
    @GetMapping("getdatamodel")
    public BaseResultEntity getDataModel(@RequestHeader("userId") Long userId,Long taskId){
        if (taskId==null||taskId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        if (userId==null||userId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        return dataModelService.getDataModel(taskId,userId);
    }


    @ApiOperation(value = "获取模型组件列表",httpMethod = "GET")
    @GetMapping("getModelComponent")
    public BaseResultEntity getModelComponent(){
        return dataModelService.getModelComponent();
    }


    @ApiOperation(value = "获取模型组件列表详情",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(name="userId", value = "用户ID,不用填写getaway处理", dataType = "Long", paramType = "header"),
            @ApiImplicitParam(name="modelId", value = "模型ID", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name="projectId", value = "项目ID", dataType = "Long", paramType = "query")
    })
    @GetMapping("getModelComponentDetail")
    public BaseResultEntity getModelComponentDetail(@RequestHeader("userId") Long userId,
                                                    Long modelId,
                                                    Long projectId){
        if (userId==null||userId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        return dataModelService.getModelComponentDetail(modelId,userId,projectId);
    }


    @ApiOperation(value = "获取模型组件列表详情",httpMethod = "GET",consumes = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("saveModelAndComponent")
    public BaseResultEntity saveModelAndComponent(@RequestHeader("userId") Long userId,
                                                  @RequestBody BaseJsonParam<DataModelAndComponentReq> req){
        if (userId<0){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        return dataModelService.saveModelAndComponent(userId,req.getParam());
    }


    @ApiOperation(value = "删除模型",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParam(name="modelId", value = "模型ID", dataType = "Long", paramType = "query")
    @GetMapping("deleteModel")
    public BaseResultEntity deleteModel(Long modelId){
        if (modelId==null||modelId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelId");
        }
        return dataModelService.deleteModel(modelId);
    }


    @ApiOperation(value = "运行模型",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(name="userId", value = "用户ID,不用填写getaway处理", dataType = "Long", paramType = "header"),
            @ApiImplicitParam(name="modelId", value = "模型ID", dataType = "Long", paramType = "query")
    })
    @GetMapping("runTaskModel")
    public BaseResultEntity runTaskModel(@RequestHeader("userId") Long userId,Long modelId){
        if (modelId==null||modelId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelId");
        }
        return dataModelService.runTaskModel(modelId,userId);
    }


    @ApiOperation(value = "重启模型",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParam(name="taskId", value = "任务ID", dataType = "Long", paramType = "query")
    @GetMapping("restartTaskModel")
    public BaseResultEntity restartTaskModel(Long taskId){
        if (taskId==null||taskId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return dataModelService.restartTaskModel(taskId);
    }


    @ApiOperation(value = "获取运行模型信息",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParam(name="taskId", value = "任务ID", dataType = "Long", paramType = "query")
    @GetMapping("getTaskModelComponent")
    public BaseResultEntity getTaskModelComponent(Long taskId){
        if (taskId==null||taskId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"taskId");
        }
        return dataModelService.getTaskModelComponent(taskId);
    }

    @GetMapping("getModelPrediction")
    public BaseResultEntity getModelPrediction(Long modelId){
        if (modelId==null||modelId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelId");
        }
        return dataModelService.getModelPrediction(modelId);
    }


    @ApiOperation(value = "运行成功的模型列表",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParam(name="userId", value = "用户ID,不用填写getaway处理", dataType = "Long", paramType = "header")
    @GetMapping("getModelTaskSuccessList")
    public BaseResultEntity getModelTaskSuccessList(@RequestHeader("userId") Long userId,ModelTaskSuccessReq req){
        if (userId==null||userId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        req.setUserId(userId);
        return dataModelService.getModelTaskSuccessList(req);
    }

    @PostMapping("saveOrUpdateComponentDraft")
    public BaseResultEntity saveOrUpdateComponentDraft(@RequestHeader("userId") Long userId, ComponentDraftReq req){
        if (userId==null||userId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        req.setUserId(userId);
        if (StringUtils.isBlank(req.getComponentJson())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"componentJson");
        }
        if (StringUtils.isBlank(req.getComponentImage())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"componentImage");
        }
        return dataModelService.saveOrUpdateComponentDraft(req);
    }

    @GetMapping("getComponentDraftList")
    public BaseResultEntity getComponentDraftList(@RequestHeader("userId") Long userId){
        if (userId==null||userId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        return dataModelService.getComponentDraftList(userId);
    }

    @GetMapping("deleteComponentDraft")
    public BaseResultEntity deleteComponentDraft(@RequestHeader("userId") Long userId,Long draftId){
        if (userId==null||userId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"userId");
        }
        if (draftId==null||draftId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"draftId");
        }
        return dataModelService.deleteComponentDraft(draftId,userId);
    }

    @ApiOperation(value = "修改模型描述",httpMethod = "GET",consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ApiImplicitParams({
            @ApiImplicitParam(name="modelId", value = "模型ID", dataType = "Long", paramType = "query"),
            @ApiImplicitParam(name="modelDesc", value = "模型描述", dataType = "String", paramType = "query")
    })
    @GetMapping("updateModelDesc")
    public BaseResultEntity updateModelDesc(Long modelId,String modelDesc){
        if (modelId==null||modelId==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelId");
        }
        if (StringUtils.isBlank(modelDesc)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"modelDesc");
        }
        return dataModelService.updateModelDesc(modelId,modelDesc);
    }





}
