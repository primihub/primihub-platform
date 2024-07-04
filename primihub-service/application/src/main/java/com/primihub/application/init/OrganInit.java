package com.primihub.application.init;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.dataenum.DataFusionCopyEnum;
import com.primihub.biz.entity.data.po.DataFusionCopyTask;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.repository.primarydb.sys.SysOrganPrimarydbRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.data.OtherBusinessesService;
import com.primihub.biz.service.sys.SysOrganService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrganInit implements CommandLineRunner {

    @Autowired
    private SysOrganSecondarydbRepository sysOrganSecondarydbRepository;
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private SysOrganPrimarydbRepository sysOrganPrimarydbRepository;
    /**
     * 系统重启 重新链接
     * @param args
     * @throws Exception
     */
    @Override
    public void run(String... args) throws Exception {
        // 获取 正常链接的 机构信息
        List<SysOrgan> sysOrgans = sysOrganSecondarydbRepository.selectSysOrganByExamine();
        if (CollectionUtils.isEmpty(sysOrgans)){
            return;
        }
        for (SysOrgan sysOrgan : sysOrgans) {
            String localOrganShortCode = organConfiguration.getLocalOrganShortCode();
            if (sysOrgan.getApplyId().contains(localOrganShortCode)&&sysOrgan.getExamineState()==0){
                return;
            }
            sysOrgan.setEnable(0);
            sysOrgan.setApplyId(organConfiguration.generateUniqueCode());
            sysOrgan.setExamineState(0);
            SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
            Map<String,Object> map = new HashMap<>();
            map.put("organId",sysLocalOrganInfo.getOrganId());
            map.put("organName",sysLocalOrganInfo.getOrganName());
            map.put("gateway",sysLocalOrganInfo.getGatewayAddress());
            map.put("publicKey",sysLocalOrganInfo.getPublicKey());
            map.put("applyId",sysOrgan.getApplyId());
            map.put("examineState",sysOrgan.getExamineState());
            map.put("enable",sysOrgan.getEnable());
            BaseResultEntity baseResultEntity = otherBusinessesService.syncGatewayApiData(map, sysOrgan.getOrganGateway() + "/share/shareData/apply", sysOrgan.getPublicKey());
            if (baseResultEntity == null && baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
                sysOrganPrimarydbRepository.updateSysOrgan(sysOrgan);
            }
        }
    }
}

