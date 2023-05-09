package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.param.ChangeLocalOrganInfoParam;
import com.primihub.biz.entity.sys.param.OrganParam;
import com.primihub.biz.service.sys.SysOrganService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 机构
 */
@RequestMapping("organ")
@RestController
public class OrganController {

    @Autowired
    private SysOrganService sysOrganService;

    @RequestMapping("getLocalOrganInfo")
    public BaseResultEntity getLocalOrganInfo(){
        return sysOrganService.getLocalOrganInfo();
    }

    @RequestMapping("changeLocalOrganInfo")
    public BaseResultEntity changeLocalOrganInfo(ChangeLocalOrganInfoParam changeLocalOrganInfoParam){
        return sysOrganService.changeLocalOrganInfo(changeLocalOrganInfoParam);
    }

    @RequestMapping("changeHomepage")
    public BaseResultEntity changeHomepage(@RequestBody Map<String,Object> homeMap){
        homeMap.remove("token");
        homeMap.remove("timestamp");
        homeMap.remove("nonce");
        if (homeMap.size()==0) {
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"无参数");
        }
        return sysOrganService.changeHomepage(homeMap);
    }

    @RequestMapping("getHomepage")
    public BaseResultEntity getHomepage(){
        return sysOrganService.getHomepage();
    }

    /**
     * 加入合作方
     * @param gateway
     * @param publicKey
     * @return
     */
    @RequestMapping("joiningPartners")
    public BaseResultEntity joiningPartners(String gateway,String publicKey){
        if (StringUtils.isBlank(gateway)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"gateway");
        }
        if (StringUtils.isBlank(publicKey)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"publicKey");
        }
        return sysOrganService.joiningPartners(gateway,publicKey);
    }

    /**
     * 查询合作列表
     * @param param
     * @return
     */
    @RequestMapping("getOrganList")
    public BaseResultEntity getOrganList(OrganParam param){
        return sysOrganService.getOrganList(param);
    }

    /**
     * 查询可用合作机构列表
     * @return
     */
    @RequestMapping("getAvailableOrganList")
    public BaseResultEntity getAvailableOrganList(){
        return sysOrganService.getAvailableOrganList();
    }

    /**
     * 审核申请的机构
     * @param id                申请数字ID
     * @param examineState      审核状态    1同意 2拒绝
     * @param examineMsg        审核意见
     * @return
     */
    @RequestMapping("examineJoining")
    public BaseResultEntity examineJoining(Long id,Integer examineState,String examineMsg){
        if (id==null || id==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"id");
        }
        if (examineState==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"examineState");
        }
        if (examineState!=1 &&examineState!=2) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"examineState");
        }
        return sysOrganService.examineJoining(id,examineState,examineMsg);
    }

    /**
     * 开启状态修改
     * @param id
     * @param status
     * @return
     */
    @RequestMapping("enableStatus")
    public BaseResultEntity enableStatus(Long id,Integer status){
        if (id==null || id==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"id");
        }
        if (status!=1 &&status!=0) {
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"status");
        }
        return sysOrganService.enableStatus(id,status);
    }
}
