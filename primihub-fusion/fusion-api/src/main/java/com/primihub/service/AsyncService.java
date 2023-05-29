package com.primihub.service;

import com.alibaba.fastjson.JSONObject;
import com.primihub.entity.DataSet;
import com.primihub.entity.base.BaseResultEntity;
import com.primihub.service.feign.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AsyncService {

    @Autowired
    private NoticeService noticeService;

    @Async
    public void noticeResource(DataSet dataSet,DataSet newDataSet){
        int d = 0;
        if (dataSet!=null){
            d = dataSet.toString().hashCode();
        }
        int nd = newDataSet.toString().hashCode();
        if (d!=nd){
            BaseResultEntity baseResultEntity = noticeService.noticeResource(newDataSet.getId());
            log.info("{} - {}",dataSet.getId(),JSONObject.toJSONString(baseResultEntity));
            if (!"0".equals(baseResultEntity.getCode())){
                log.info("进入{}",dataSet.getId());
                baseResultEntity = noticeService.testDataSet(newDataSet.getId());
                log.info("{} - {}",dataSet.getId(),JSONObject.toJSONString(baseResultEntity));
            }
        }
    }

}
