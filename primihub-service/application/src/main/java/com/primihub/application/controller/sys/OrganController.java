package com.primihub.application.controller.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.param.ChangeLocalOrganInfoParam;
import com.primihub.biz.entity.sys.param.ChangeOtherOrganInfoParam;
import com.primihub.biz.entity.sys.param.OrganParam;
import com.primihub.biz.service.sys.SysOrganService;
import io.swagger.annotations.Api;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 机构
 */
@Api(value = "机构(节点)接口",tags = "机构(节点)接口")
@RequestMapping("organ")
@RestController
public class OrganController {

    @Autowired
    private SysOrganService sysOrganService;

    @GetMapping("getLocalOrganInfo")
    public BaseResultEntity getLocalOrganInfo(){
        return sysOrganService.getLocalOrganInfo();
    }

    @PostMapping("changeLocalOrganInfo")
    public BaseResultEntity changeLocalOrganInfo(ChangeLocalOrganInfoParam changeLocalOrganInfoParam){
        return sysOrganService.changeLocalOrganInfo(changeLocalOrganInfoParam);
    }

    @PostMapping("changeHomepage")
    public BaseResultEntity changeHomepage(@RequestBody Map<String,Object> homeMap){
        homeMap.remove("token");
        homeMap.remove("timestamp");
        homeMap.remove("nonce");
        if (homeMap.size()==0) {
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"无参数");
        }
        return sysOrganService.changeHomepage(homeMap);
    }

    @GetMapping("getHomepage")
    public BaseResultEntity getHomepage(){
        return sysOrganService.getHomepage();
    }

    /**
     * 加入合作方
     * @param gateway
     * @param publicKey
     * @return
     */
    @GetMapping("joiningPartners")
    public BaseResultEntity joiningPartners(String gateway,String publicKey){
        if (StringUtils.isBlank(gateway)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"gateway");
        }
        if (StringUtils.isBlank(publicKey)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"publicKey");
        }
        // 解决 "+" 在传输中替换为 " " 的问题
        if (publicKey.contains(" ")) {
            publicKey = publicKey.replace(" ", "+");
        }
        return sysOrganService.joiningPartners(gateway,publicKey);
    }

    /**
     * 加入合作方
     * @param gateway
     * @param publicKey
     * @return
     */
    @GetMapping("joiningPartnersForResource")
    public BaseResultEntity joiningPartnersForResource(String gateway,String publicKey){
        if (StringUtils.isBlank(gateway)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"gateway");
        }
        if (StringUtils.isBlank(publicKey)) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"publicKey");
        }
        // 解决 "+" 在传输中替换为 " " 的问题
        if (publicKey.contains(" ")) {
            publicKey = publicKey.replace(" ", "+");
        }
        return sysOrganService.joiningPartnersForResource(gateway,publicKey);
    }

    /**
     * 查询合作列表
     * @param param
     * @return
     */
    @GetMapping("getOrganList")
    public BaseResultEntity getOrganList(OrganParam param){
        return sysOrganService.getOrganList(param);
    }

    /**
     * 查询可用合作机构列表
     * @return
     */
    @GetMapping("getAvailableOrganList")
    public BaseResultEntity getAvailableOrganList(){
        return sysOrganService.getAvailableOrganList();
    }

    /**
     * 修改合作机构网关和公钥
     * @return
     */
    @PostMapping("changeOtherOrganInfo")
    public BaseResultEntity changeOtherOrganInfo(ChangeOtherOrganInfoParam changeOtherOrganInfoParam){
        return sysOrganService.changeOtherOrganInfo(changeOtherOrganInfoParam);
    }
    /**
     * 审核申请的机构
     * @param id                申请数字ID
     * @param examineState      审核状态    0再次申请 1同意 2拒绝
     * @param examineMsg        审核意见
     * @return
     */
    @GetMapping("examineJoining")
    public BaseResultEntity examineJoining(Long id,Integer examineState,String examineMsg){
        if (id==null || id==0L) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"id");
        }
        if (examineState==null) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"examineState");
        }
        if (examineState!=0 &&examineState!=1 &&examineState!=2) {
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
    @GetMapping("enableStatus")
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
