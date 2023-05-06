package com.primihub.biz.service.sys;


import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;

@Service
@Slf4j
public class SysAsyncService {

    @Resource(name="soaRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private OrganConfiguration organConfiguration;

    @Async
    public void collectBaseData() {
        try {
            Thread.sleep(5000L);
            SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
            if (sysLocalOrganInfo==null)
                return;
            if (sysLocalOrganInfo.getAddressInfo()==null)
                return;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("key", new ArrayList() {{add(SysConstant.SYS_COLLECT_KEY);}});
            map.put("globalId", new ArrayList() {{add(sysLocalOrganInfo.getOrganId());}});
            map.put("globalName", new ArrayList() {{add(sysLocalOrganInfo.getOrganName());}});
            map.put("country", new ArrayList() {{add(sysLocalOrganInfo.getAddressInfo().getCountry());}});
            map.put("lat", new ArrayList() {{add(sysLocalOrganInfo.getAddressInfo().getLat());}});
            map.put("lon", new ArrayList() {{add(sysLocalOrganInfo.getAddressInfo().getLon());}});
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            BaseResultEntity resultEntity = restTemplate.postForObject(SysConstant.SYS_COLLECT_URL, request, BaseResultEntity.class);
            log.info(JSONObject.toJSONString(resultEntity));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Async
    public void applyForJoinNode(SysOrgan sysOrgan) {
        log.info(JSONObject.toJSONString(sysOrgan));
        if (sysOrgan.getEnable()==1){
            // TODO 该机构下的数据进行下线处理
        }else if (sysOrgan.getExamineState()==1){
            // TODO 进行数据传输
        }
    }
}
