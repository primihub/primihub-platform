package com.yyds.application.controller.sys;

import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.base.BaseResultEnum;
import com.yyds.biz.entity.sys.param.AlterOrganNodeStatusParam;
import com.yyds.biz.entity.sys.param.CreateOrganNodeParam;
import com.yyds.biz.entity.sys.param.FindOrganPageParam;
import com.yyds.biz.service.sys.SysOrganService;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

    @RequestMapping("organ")
@RestController
public class OrganController {

    @Autowired
    private SysOrganService sysOrganService;

    @RequestMapping("findOrganPage")
    public BaseResultEntity findOrganPage(FindOrganPageParam findOrganPageParam,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10")Integer pageSize) {
        if(findOrganPageParam.getPOrganId()==null)
            findOrganPageParam.setPOrganId(0L);
        return sysOrganService.findOrganPage(findOrganPageParam,pageNum,pageSize);
    }

    @RequestMapping("createOrganNode")
    public BaseResultEntity createOrganNode(CreateOrganNodeParam createOrganNodeParam){
        if(createOrganNodeParam.getOrganName()==null||createOrganNodeParam.getOrganName().trim().equals(""))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organName");
        if (createOrganNodeParam.getOrganName().length()>65)
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"机构名称请控制在65个字符以内");
        if(createOrganNodeParam.getOrganIndex()==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organIndex");
        if(createOrganNodeParam.getPOrganId()==null)
            createOrganNodeParam.setPOrganId(0L);
        return sysOrganService.createOrganNode(createOrganNodeParam);
    }

    @RequestMapping("alterOrganNodeStatus")
    public BaseResultEntity alterOrganNodeStatus(AlterOrganNodeStatusParam alterOrganNodeStatusParam) {
        if(alterOrganNodeStatusParam.getOrganId()==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        if((alterOrganNodeStatusParam.getOrganName()==null|| alterOrganNodeStatusParam.getOrganName().trim().equals("")))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"至少需要一个修改的字段");
        if (alterOrganNodeStatusParam.getOrganName().length()>65)
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"机构名称请控制在65个字符以内");
        return sysOrganService.alterOrganNodeStatus(alterOrganNodeStatusParam);
    }

    @RequestMapping("deleteOrganNode")
    public BaseResultEntity deleteOrganNode(@Param("organId") Long organId){
        if(organId==null)
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"organId");
        return sysOrganService.deleteOrganNode(organId);
    }
}
