package com.primihub.biz.service.share;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.entity.event.RemoteDataResourceEvent;
import com.primihub.biz.service.data.DataResourceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;


@Service
@Slf4j
public class RemoteShareService {
    private static final String remoteAddress2 = "http://192.168.99.12:31210";
    private static final String remoteAddress1 = "http://27.185.28.52:32210";
    private static final String remoteUrl = "/dataShare/share";
    @Resource
    private Environment environment;
    @Resource(name="soaRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    DataResourceService resourceService;
    /**
     * 什么时候调用：
     *      1.创建资源+公开
     *      2.资源进行上下线+公开
     *      3.修改资源
     *          1).公开->私有或者授权
     *          2).私有或者授权 -> 公开
     */
    @org.springframework.context.event.EventListener(RemoteDataResourceEvent.class)
    public void transDataResource(RemoteDataResourceEvent event) {
        log.info("spring event 接受的数据: {}", JSON.toJSONString(event));
        Long resourceId = event.getResourceId();
        if (resourceId == null) {
            log.error("本次数据资源传送 没有数据id 请修改代码逻辑");
        }
        Map transMap = resourceService.getDataResourceToTransfer(event.getResourceId(), event.getResourceState());
        if (transMap==null) {
            log.info("未找到id: {} 的数据资源，传送失败", event.getResourceId());
        }
        transDataResource(resourceId, transMap, 1);
    }

    public void transDataResource(Long resourceId, Map transMap, int count) {
        String remoteAddress;
        String profile = environment.getProperty(AbstractEnvironment.ACTIVE_PROFILES_PROPERTY_NAME);
        log.info("当前的profile: {}", profile);
        if (profile == null) {
            profile = "";
        }
        if (profile.trim().startsWith("test") || profile.trim().startsWith("dev")) {
            remoteAddress = remoteAddress2;
        } else {
            remoteAddress = remoteAddress1;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity(transMap, headers);
        log.info("传送地址：{}， 传送的资源Id: {}", remoteAddress+remoteUrl, resourceId);
        Map map = restTemplate.postForObject(remoteAddress + remoteUrl, request, Map.class);
        log.info("传送地址：{}， 传送的资源Id: {}， 返回结果：{}", remoteAddress+remoteUrl, resourceId, JSONObject.toJSONString(map));
        if (map.get("code") == null || !map.get("code").toString().equals("0")) {
            if (count<=3) {
                transDataResource(resourceId, transMap, count+1);
            }
            log.info("传送地址：{}， 传送的资源Id: {}，重试次数用完，未传送成功", remoteAddress+remoteUrl, resourceId);
        }
    }

}
