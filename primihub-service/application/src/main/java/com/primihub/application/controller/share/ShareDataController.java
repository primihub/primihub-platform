package com.primihub.application.controller.share;


import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.po.DataPsi;
import com.primihub.biz.entity.data.po.DataPsiTask;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.entity.data.vo.ShareModelVo;
import com.primihub.biz.entity.data.vo.ShareProjectVo;
import com.primihub.biz.service.data.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("shareData")
@RestController
@Slf4j
public class ShareDataController {

    @Autowired
    private DataProjectService dataProjectService;
    @Autowired
    private DataModelService dataModelService;
    @Autowired
    private DataPsiService dataPsiService;
    @Autowired
    private PirService pirService;
    @Autowired
    private DataTaskService dataTaskService;
    @Autowired
    private DataReasoningService dataReasoningService;

    /**
     * 创建编辑项目接口
     * @return
     */
    @RequestMapping("syncProject")
    public BaseResultEntity syncProject(@RequestBody ShareProjectVo vo){
        return dataProjectService.syncProject(vo);
    }

    /**
     * 创建编辑项目接口
     * @return
     */
    @RequestMapping("syncModel")
    public BaseResultEntity syncModel(@RequestBody ShareModelVo vo){
        return dataModelService.syncModel(vo);
    }

    @RequestMapping("syncPsi")
    public BaseResultEntity syncPsi(@RequestBody DataPsiTaskSyncReq req){
        log.info(JSONObject.toJSONString(req));
        return dataPsiService.syncPsi(req.getPsiTask(),req.getDataPsi(),req.getDataTask());
    }

    @RequestMapping("dispatchRunPsi")
    public BaseResultEntity dispatchRunPsi(@RequestBody DataPsiTaskSyncReq taskReq){
        log.info(JSONObject.toJSONString(taskReq));
        return dataPsiService.runPsi(taskReq.getPsiTask(),taskReq.getDataPsi(),taskReq.getDataTask());
    }

    @RequestMapping("dispatchRunPir")
    public BaseResultEntity dispatchRunPir(@RequestBody DataPirTaskSyncReq taskReq){
        log.info(JSONObject.toJSONString(taskReq));
        return pirService.runPir(taskReq);
    }

    @RequestMapping("syncPir")
    public BaseResultEntity syncPir(@RequestBody DataPirTaskSyncReq req){
        log.info(JSONObject.toJSONString(req));
        return dataTaskService.saveDataTask(req);
    }





    @RequestMapping("dispatchRunTaskModel")
    public BaseResultEntity dispatchRunTaskModel(@RequestBody ComponentTaskReq taskReq){
        return dataModelService.dispatchRunTaskModel(taskReq);
    }

    @RequestMapping("dispatchRestartTaskModel")
    public BaseResultEntity dispatchRestartTaskModel(@RequestBody DataTaskReq req) {
        return dataModelService.dispatchRestartTaskModel(req);
    }


    @RequestMapping("dispatchRunReasoning")
    public BaseResultEntity dispatchRunReasoning(@RequestBody DataReasoningTaskSyncReq taskReq){
        log.info(JSONObject.toJSONString(taskReq));
        return dataReasoningService.dispatchRunReasoning(taskReq);
    }

}
