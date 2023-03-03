package com.primihub.application.controller.share;


import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.req.DataPsiTaskSyncReq;
import com.primihub.biz.entity.data.vo.ShareModelVo;
import com.primihub.biz.entity.data.vo.ShareProjectVo;
import com.primihub.biz.service.data.DataModelService;
import com.primihub.biz.service.data.DataProjectService;
import com.primihub.biz.service.data.DataPsiService;
import com.primihub.biz.service.data.DataTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("shareData")
@RestController
public class ShareDataController {

    @Autowired
    private DataProjectService dataProjectService;
    @Autowired
    private DataModelService dataModelService;
    @Autowired
    private DataPsiService dataPsiService;
    @Autowired
    private DataTaskService dataTaskService;

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
        return dataPsiService.syncPsi(req.getPsiTask(),req.getDataPsi(),req.getDataTask());
    }
    @RequestMapping("syncPir")
    public BaseResultEntity syncPir(@RequestBody DataTask dataTask){
        return dataTaskService.saveDataTask(dataTask);
    }

}
