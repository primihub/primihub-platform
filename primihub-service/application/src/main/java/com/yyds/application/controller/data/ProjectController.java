package com.yyds.application.controller.data;

import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.base.BaseResultEnum;
import com.yyds.biz.entity.data.req.DataProjectReq;
import com.yyds.biz.service.data.DataProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("project")
@RestController
public class ProjectController {

    @Autowired
    private DataProjectService dataProjectService;

    /**
     * 创建项目
     * @return
     */
    @PostMapping("saveproject")
    public BaseResultEntity saveDataProject(@RequestHeader("userId") Long userId,
                                            @RequestHeader("organId")Long organId,
                                            DataProjectReq req){
        if (organId<0){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        }
        if (req.getProjectName()==null || req.getProjectName().trim().equals("")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"projectName");
        }
        if (req.getProjectDesc()==null || req.getProjectDesc().trim().equals("")){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"projectDesc");
        }
        if (req.getResources()==null||req.getResources().size()==0){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"resources");
        }
        return dataProjectService.saveDataProject(userId,organId,req);
    }

    /**
     * 修改项目
     * @return
     */
//    @PostMapping("/project/editproject")
//    public BaseResultEntity editDataProject(@RequestHeader(value = "userId",defaultValue = "1") Long userId,
//                                            @RequestHeader(value = "organId",defaultValue = "1")Long organId,
//                                            @Validated DataProjectReq req){
//        return dataProjectService.editDataProject(userId,organId,req);
//    }

    /**
     * 获取项目列表
     * @return
     */
    @GetMapping("getprojectlist")
    public BaseResultEntity getDataProjectList(@RequestHeader("userId") Long userId,
                                               @RequestHeader("organId")Long organId,
                                               DataProjectReq req){
        if (organId<0){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        }
        return dataProjectService.getDataProjectList(userId,organId,req);
    }

    /**
     * 获取项目详情
     * @return
     */
    @GetMapping("getdataproject")
    public BaseResultEntity getDataProject(Long projectId){
        if (projectId==null||projectId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"projectId");
        }
        return dataProjectService.getDataProject(projectId);
    }

    /**
     * 删除项目信息
     * @return
     */
    @GetMapping("deldataproject")
    public BaseResultEntity delDataProject(Long projectId){
        if (projectId==null||projectId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"projectId");
        }
        return dataProjectService.delDataProject(projectId);
    }

    //---------------------------------v0.2----------------------------------
    /**
     * 获取项目资源、y字段接口
     * @return
     */
    @GetMapping("getProjectResourceData")
    public BaseResultEntity getProjectResourceData(Long projectId){
        if (projectId==null||projectId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"projectId");
        }
        return dataProjectService.getProjectResourceData(projectId);
    }
    /**
     * 获取审核通过的项目列表
     * @return
     */
    @GetMapping("getProjectAuthedeList")
    public BaseResultEntity getProjectAuthedeList(@RequestHeader("userId") Long userId,
                                                  @RequestHeader("organId")Long organId,
                                                  DataProjectReq req){
        return dataProjectService.getProjectAuthedeList(req,userId,organId);
    }

    @GetMapping("getMpcProjectResourceData")
    public BaseResultEntity getMpcProjectResourceData(Long projectId){
        if (projectId==null||projectId==0L){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"projectId");
        }
        return dataProjectService.getMpcProjectResourceData(projectId);
    }



}
