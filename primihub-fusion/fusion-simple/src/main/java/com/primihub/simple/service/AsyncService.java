package com.primihub.simple.service;

import com.primihub.entity.DataSet;
import com.primihub.simple.base.BaseResultEntity;
import com.primihub.simple.constant.SysConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service
public class AsyncService {
    private Logger log = LoggerFactory.getLogger(this.getClass());
    @Resource
    private RestTemplate restTemplate;
    @Autowired
    private DataSetService dataSetService;

    @Value("#{'${collaborate}'.split(',')}")
    private List<String> collaborates;


    @Async
    public void syncOne(DataSet dataSet){
        for (String collaborate : collaborates) {
            syncGatewayApiData(dataSet,collaborate+ SysConstant.ONE_URL);
        }
    }

    @Async
    public void syncMany(List<DataSet> dataSets){
        for (String collaborate : collaborates) {
            syncGatewayApiData(dataSets,collaborate+ SysConstant.ONE_URL);
        }
    }

    @Async
    public void syncDelete(String id){
        for (String collaborate : collaborates) {
            syncGatewayApiData(id,collaborate+ SysConstant.DELETE_URL);
        }
    }

    private void syncGatewayApiData(Object data,String addressAndApi){
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(data, headers);
            log.info(addressAndApi);
            BaseResultEntity baseResultEntity = restTemplate.postForObject(addressAndApi, request, BaseResultEntity.class);
            log.info("baseResultEntity code:{}",baseResultEntity.getCode());
        }catch (Exception e){
            log.info("addressAndApi api url:{} Exception:{}",addressAndApi,e.getMessage());
            e.printStackTrace();
        }
        log.info("addressAndApi api url:{} end:{}",addressAndApi,System.currentTimeMillis());
    }
    @Async
    public void startSync(){
        List<DataSet> all = dataSetService.getAll();
        syncMany(all);
    }
}
