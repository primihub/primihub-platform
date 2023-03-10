package com.primihub.application.controller.share;


import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.req.DataPirTaskSyncReq;
import com.primihub.biz.entity.data.req.DataPsiTaskSyncReq;
import com.primihub.biz.entity.data.req.DataReasoningTaskSyncReq;
import com.primihub.biz.entity.data.vo.ShareModelVo;
import com.primihub.biz.entity.data.vo.ShareProjectVo;
import com.primihub.biz.service.data.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
        log.info("syncModel - :{}",JSONObject.toJSONString(vo));
        return dataModelService.syncModel(vo);
    }

    @RequestMapping("syncPsi")
    public BaseResultEntity syncPsi(@RequestBody DataPsiTaskSyncReq req){
        log.info("syncModel - :{}",JSONObject.toJSONString(req));
        return dataPsiService.syncPsi(req.getPsiTask(),req.getDataPsi(),req.getDataTask());
    }
    @RequestMapping("syncPir")
    public BaseResultEntity syncPir(@RequestBody DataPirTaskSyncReq req){
        return dataTaskService.saveDataTask(req);
    }

    @RequestMapping("syncReasoning")
    public BaseResultEntity syncReasoning(@RequestBody DataReasoningTaskSyncReq req){
        log.info("syncReasoning - :{}",JSONObject.toJSONString(req));
        return dataReasoningService.syncReasoning(req);
    }

}
