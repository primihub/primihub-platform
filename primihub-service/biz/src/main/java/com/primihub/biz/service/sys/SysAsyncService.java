package com.primihub.biz.service.sys;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.base.BaseFunctionHandleEntity;
import com.primihub.biz.entity.base.BaseFunctionHandleEnum;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.service.feign.FusionOrganService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.MessageBuilder;
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
    @Autowired
    private SingleTaskChannel singleTaskChannel;
    @Autowired
    private FusionOrganService fusionOrganService;

    @Async
    public void collectBaseData() {
        try {
            SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
            if (sysLocalOrganInfo==null){
                return;
            }
            if (sysLocalOrganInfo.getAddressInfo()==null){
                return;
            }
            Thread.sleep(5000L);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("key", new ArrayList() {{add(SysConstant.SYS_COLLECT_KEY);}});
            map.put("globalId", new ArrayList() {{add(sysLocalOrganInfo.getOrganId());}});
            map.put("globalName", new ArrayList() {{add(sysLocalOrganInfo.getOrganName());}});
            map.put("country", new ArrayList() {{add(sysLocalOrganInfo.getAddressInfo().getCountry());}});
            map.put("lat", new ArrayList() {{add(sysLocalOrganInfo.getAddressInfo().getLat());}});
            map.put("lon", new ArrayList() {{add(sysLocalOrganInfo.getAddressInfo().getLon());}});
            map.put("city", new ArrayList() {{add(sysLocalOrganInfo.getAddressInfo().getCity());}});
            map.put("region", new ArrayList() {{add(sysLocalOrganInfo.getAddressInfo().getRegion());}});
            map.put("globalGateway", new ArrayList() {{add(sysLocalOrganInfo.getGatewayAddress());}});
            map.put("publicKey", new ArrayList() {{add(sysLocalOrganInfo.getPublicKey());}});
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
            fusionOrganService.organData(sysOrgan.getOrganId(),sysOrgan.getOrganName());
            singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.BATCH_DATA_FUSION_RESOURCE_TASK.getHandleType(),sysOrgan))).build());
        }
    }
}
