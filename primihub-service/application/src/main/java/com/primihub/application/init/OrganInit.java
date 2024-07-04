package com.primihub.application.init;

import com.primihub.biz.entity.data.dataenum.DataFusionCopyEnum;
import com.primihub.biz.entity.data.po.DataFusionCopyTask;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.sys.SysOrganService;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrganInit implements CommandLineRunner {

    @Autowired
    private SysOrganSecondarydbRepository sysOrganSecondarydbRepository;
    @Autowired
    private SysOrganService sysOrganService;
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
            sysOrganService.examineJoining(sysOrgan.getId(), 0, "");
        }
    }
}

