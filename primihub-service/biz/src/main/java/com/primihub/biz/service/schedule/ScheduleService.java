package com.primihub.biz.service.schedule;

import com.primihub.biz.constant.RedisKeyConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.po.DataFusionCopyTask;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.repository.primarydb.sys.SysOrganPrimarydbRepository;
import com.primihub.biz.repository.primaryredis.sys.SysCommonPrimaryRedisRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.data.DataCopyService;
import com.primihub.biz.service.data.OtherBusinessesService;
import com.primihub.biz.service.sys.SysAsyncService;
import com.primihub.biz.util.crypt.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class ScheduleService {

    @Autowired
    private SysCommonPrimaryRedisRepository sysCommonPrimaryRedisRepository;
    @Autowired
    private SysOrganSecondarydbRepository sysOrganSecondarydbRepository;
    @Autowired
    private SysOrganPrimarydbRepository sysOrganPrimarydbRepository;
    @Autowired
    private DataCopyService dataCopyService;
    @Autowired
    private SysAsyncService sysAsyncService;
    @Autowired
    private OtherBusinessesService otherBusinessesService;


    public BaseResultEntity recallNotFinishedTask(){
        Date date=new Date();
        String key=RedisKeyConstant.SCHEDULE_FUSION_COPY_KEY
                .replace("<date>", DateUtil.formatDate(date,DateUtil.DateStyle.HOUR_FORMAT_SHORT.getFormat()))
                .replace("<piece>",String.valueOf(Integer.valueOf(DateUtil.formatDate(date,"mm"))/10));
        Long currentAtom=sysCommonPrimaryRedisRepository.atomIncrement(key,11, TimeUnit.MINUTES);
        Map result=new HashMap<>();
        result.put("fusionMsg","已有节点处理");
        if(true||currentAtom.equals(1L)){
            Date threeDayAgo= DateUtil.changeDate(date, Calendar.DAY_OF_WEEK,-3);
            Date tenMinuteAgo= DateUtil.changeDate(date, Calendar.MINUTE,-3);
            List<DataFusionCopyTask> notFinishedTask=dataCopyService.selectNotFinishedTask(threeDayAgo,tenMinuteAgo);
            for(DataFusionCopyTask task:notFinishedTask){
                log.info("本次recall的任务id: {}", task.getId());
                dataCopyService.handleFusionCopyTask(task);
            }
            result.put("fusionMsg","本节点处理");
        }
        return BaseResultEntity.success(result);
    }

    /**
     * 定时处理节点业务 10分钟一次
     */
    @Scheduled(cron="0 0/10 * * * ? ")
    private void nodeOperations(){
        log.info("定时处理节点业务");
        // 上报节点状态
        sysAsyncService.collectBaseData();
        List<SysOrgan> sysOrgans = sysOrganSecondarydbRepository.selectSysOrganByExamine();
        long time = System.currentTimeMillis();
        String data = String.format("{'time':%s}", time);
        for (SysOrgan sysOrgan : sysOrgans) {
            try {
                BaseResultEntity baseResultEntity = otherBusinessesService.syncGatewayApiData(data, sysOrgan.getOrganGateway() + "/share/shareData/healthConnection", sysOrgan.getPublicKey());
                if (baseResultEntity==null || !baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
                    Set<String> services = (Set<String>) baseResultEntity.getResult();
                    sysOrgan.setPlatformState(services.contains("platform")?1:0);
                    sysOrgan.setNodeState(services.contains("node")?1:0);
                    sysOrgan.setFusionState(services.contains("fusion")?1:0);
                }else {
                    sysOrgan.setPlatformState(0);
                    sysOrgan.setNodeState(0);
                    sysOrgan.setFusionState(0);
                }
            }catch (Exception e){
                sysOrgan.setPlatformState(0);
                sysOrgan.setNodeState(0);
                sysOrgan.setFusionState(0);
                log.info("机构ID:{} - 机构名称:{} - 机构网关地址:{} - 状态获取失败",sysOrgan.getOrganId(),sysOrgan.getOrganName(),sysOrgan.getOrganGateway());
                e.printStackTrace();
            }
            sysOrganPrimarydbRepository.updateSysOrgan(sysOrgan);
        }
    }
}
