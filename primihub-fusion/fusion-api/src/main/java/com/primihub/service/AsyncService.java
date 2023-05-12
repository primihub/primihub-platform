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
        int d = dataSet.toString().hashCode();
        int nd = newDataSet.toString().hashCode();
        if (d!=nd){
            BaseResultEntity baseResultEntity = noticeService.noticeResource(dataSet.getId());
            log.info(JSONObject.toJSONString(baseResultEntity));
            if (baseResultEntity.getCode().equals("1003")){
                baseResultEntity = noticeService.testDataSet(dataSet.getId());
                log.info(JSONObject.toJSONString(baseResultEntity));
            }
        }
    }

}
