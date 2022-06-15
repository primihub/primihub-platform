package com.yyds.application.controller.data;


import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.base.BaseResultEnum;
import com.yyds.biz.entity.data.req.DataScriptReq;
import com.yyds.biz.service.data.DataMpcService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("mpc")
@RestController
public class MpcController {

    @Autowired
    private DataMpcService dataMpcService;

    @RequestMapping("saveOrUpdateScript")
    public BaseResultEntity saveOrUpdateScript(@RequestHeader("userId") Long userId,
                                               @RequestHeader("organId") Long organId,
                                               DataScriptReq req) {
        if (userId == null || userId == 0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "userId");
        }
        if (organId == null || organId == 0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "organId");
        }
        if (req.getScriptId() == null || req.getScriptId() == 0L) {
            req.setScriptId(null);
            if (StringUtils.isBlank(req.getName()))
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "name");
            if (req.getCatalogue() == null)
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "catalogue");
            if (req.getCatalogue() != 1)
                if (req.getScriptType() == null)
                    return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "scriptType");
            if (req.getScriptStatus() == null)
                req.setScriptStatus(0);
        } else {
            if (StringUtils.isBlank(req.getName()) && (req.getPScriptId() == null || req.getPScriptId() == 0L) && req.getScriptStatus() == null && StringUtils.isBlank(req.getScriptContent()))
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "至少需要一个修改的字段");
        }
        return dataMpcService.saveOrUpdateScript(userId, organId, req);
    }

    @RequestMapping("getDataScriptList")
    public BaseResultEntity getDataScriptList(@RequestHeader("userId") Long userId,
                                              @RequestHeader("organId") Long organId,
                                              String scriptName){
        if (userId == null || userId == 0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "userId");
        }
        if (organId == null || organId == 0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "organId");
        }
        return dataMpcService.getDataScriptList(userId,organId,scriptName);
    }

    @RequestMapping("delDataScript")
    public BaseResultEntity delDataScript(Long scriptId){
        if (scriptId == null || scriptId == 0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "scriptId");
        }
        return dataMpcService.delDataScript(scriptId);
    }

    @RequestMapping("runDataScript")
    public BaseResultEntity runDataScript(@RequestHeader("userId") Long userId,Long scriptId,Long projectId,String scriptContent){
        if (userId == null || userId == 0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "userId");
        }
        if (scriptId == null || scriptId == 0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "scriptId");
        }
        if (projectId == null || projectId == 0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "projectId");
        }
        if (StringUtils.isBlank(scriptContent))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "scriptContent");
        return dataMpcService.runDataScript(scriptId,projectId,userId,scriptContent);
    }
}
