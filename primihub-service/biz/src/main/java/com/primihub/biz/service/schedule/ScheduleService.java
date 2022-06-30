package com.primihub.biz.service.schedule;

import com.primihub.biz.constant.RedisKeyConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.po.DataFusionCopyTask;
import com.primihub.biz.repository.primaryredis.sys.SysCommonPrimaryRedisRepository;
import com.primihub.biz.service.data.DataCopyService;
import com.primihub.biz.util.crypt.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class ScheduleService {

    @Autowired
    private SysCommonPrimaryRedisRepository sysCommonPrimaryRedisRepository;
    @Autowired
    private DataCopyService dataCopyService;

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
                dataCopyService.handleFusionCopyTask(task);
            }
            result.put("fusionMsg","本节点处理");
        }
        return BaseResultEntity.success(result);
    }
}
