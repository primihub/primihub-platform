package com.primihub.application.controller.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.req.DataProjectApprovalReq;
import com.primihub.biz.entity.data.req.DataProjectQueryReq;
import com.primihub.biz.entity.data.req.DataProjectReq;
import com.primihub.biz.service.data.DataProjectService;
import com.primihub.biz.util.crypt.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RequestMapping("project")
@RestController
public class ProjectController {

    @Autowired
    private DataProjectService dataProjectService;

    @RequestMapping("getListStatistics")
    public BaseResultEntity getListStatistics(){
        return dataProjectService.getListStatistics();
    }


    /**
     * 创建编辑项目接口
     * @return
     */
    @RequestMapping("saveOrUpdateProject")
    public BaseResultEntity saveOrUpdateProject(@RequestBody DataProjectReq req,
                                                @RequestHeader("userId") Long userId){
        if (req.getId()==null || req.getId()==0L){
            if (StringUtils.isBlank(req.getProjectName()))
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"projectName");
            if (StringUtils.isBlank(req.getServerAddress()))
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
            if (req.getProjectOrgans()==null||req.getProjectOrgans().isEmpty())
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"projectOrgans");
        }
        return dataProjectService.saveOrUpdateProject(req,userId);
    }

    /**
     * 项目列表接口
     * @return
     */
    @RequestMapping("getProjectList")
    public BaseResultEntity getProjectList(DataProjectQueryReq req){
        try {
            if (StringUtils.isNotBlank(req.getStartDate()))
                DateUtil.parseDate(req.getStartDate(),DateUtil.DateStyle.TIME_FORMAT_NORMAL.getFormat());
            if (StringUtils.isNotBlank(req.getEndDate()))
                DateUtil.parseDate(req.getEndDate(),DateUtil.DateStyle.TIME_FORMAT_NORMAL.getFormat());
        }catch (Exception e){
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"开始时间或结束时间校验失败");
        }
        return dataProjectService.getProjectList(req);
    }

    /**
     * 项目列表接口
     * @return
     */
    @RequestMapping("getProjectDetails")
    public BaseResultEntity getProjectDetails(Long id){
        if (id==null || id == 0L)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"id");
        return dataProjectService.getProjectDetails(id);
    }

    /**
     * 项目机构、资源审核接口
     * @return
     */
    @RequestMapping("approval")
    public BaseResultEntity approval(DataProjectApprovalReq req){
        if (req.getId()==null || req.getId() == 0L)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"id");
        if (req.getType()==null || req.getType()==0 || req.getType() >=3)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"type");
        if (req.getAuditStatus()==null||req.getAuditStatus()==0||req.getAuditStatus()>=3)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"auditStatus");
        return dataProjectService.approval(req);
    }

    /**
     * 删除项目资源
     * @return
     */
    @RequestMapping("removeResource")
    public BaseResultEntity removeResource(Long id){
        if (id==null || id == 0L)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"id");
        return dataProjectService.removeResource(id);
    }

    /**
     * 关闭项目
     * @param id
     * @return
     */
    @RequestMapping("closeProject")
    public BaseResultEntity closeProject(Long id){
        if (id==null || id == 0L)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"id");
        return dataProjectService.closeProject(id);
    }

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


}
