package com.primihub.biz.service.data;

import com.primihub.biz.constant.RemoteConstant;
import com.primihub.biz.util.crypt.RemoteUtil;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Component
public class RemoteClient {
    @Resource(name = "soaRestTemplate")
    private RestTemplate restTemplate;

    public void queryFromRemote(String phoneNum) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        MultiValueMap map = new LinkedMultiValueMap<>();
        map.put(RemoteConstant.HEAD, resembleHeadMap());
        map.put(RemoteConstant.REQUEST, resembleRequestMap(phoneNum));
        HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
        Map respBodyMap = restTemplate.postForObject(RemoteConstant.REMOTE_SCORE_URL, request, Map.class);
    }

    public Map<String, String> resembleHeadMap() {
        Map<String, String> headMap = new HashMap<>();

        String requestId = RemoteUtil.generateRandomString();
        headMap.put(RemoteConstant.REQUEST_REF_ID, requestId);
        headMap.put(RemoteConstant.SECRET_ID, RemoteConstant.SECRET_ID_VALUE);
        headMap.put(RemoteConstant.SIGNATURE, RemoteUtil.generateSignature(requestId));

        return headMap;
    }

    public Map<String, Object> resembleRequestMap(String phoneNum) {
        Map<String, Object> requestMap = new HashMap<>();

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(RemoteConstant.IMSI, phoneNum);

        requestMap.put(RemoteConstant.PARAM, paramMap);

        return requestMap;
    }
}
