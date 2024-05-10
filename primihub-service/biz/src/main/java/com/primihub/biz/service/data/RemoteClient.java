package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSONObject;
import com.anji.captcha.util.StringUtils;
import com.primihub.biz.config.base.ClientConfiguration;
import com.primihub.biz.constant.RemoteConstant;
import com.primihub.biz.entity.data.vo.RemoteRespVo;
import com.primihub.biz.util.crypt.RemoteUtil;
import com.primihub.biz.util.crypt.SM3Util;
import com.primihub.biz.util.crypt.SM4Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Objects;

@Slf4j
@Component
public class RemoteClient {
    @Resource(name = "soaRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private ClientConfiguration clientConfiguration;

    public RemoteRespVo queryFromRemote(String phoneNum) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
        MultiValueMap map = new LinkedMultiValueMap<>();
        map.put(RemoteConstant.HEAD, resembleHeadMap());
        map.put(RemoteConstant.REQUEST, resembleRequestMap(phoneNum, clientConfiguration.getSecretKey()));
        HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
        RemoteRespVo respVo = restTemplate.postForObject(RemoteConstant.REMOTE_SCORE_URL, request, RemoteRespVo.class);
        if (Objects.nonNull(respVo) && Objects.equals(respVo.getHead().getResult(), "Y")
                && StringUtils.isNotBlank(respVo.getResponse())) {
            String jsonResponse = null;
            try {
                jsonResponse = SM4Util.decrypt(respVo.getResponse(), clientConfiguration.getSecretKey());
                RemoteRespVo.RespResp respResp = JSONObject.parseObject(jsonResponse, RemoteRespVo.RespResp.class);
                respVo.setRespBody(respResp);
            } catch (Exception e) {
                log.error("remote response sm4 parse error : {}", e.getMessage());
            }
        }
        return respVo;
    }

    public Map<String, String> resembleHeadMap() {
        Map<String, String> headMap = new HashMap<>();

        String requestId = RemoteUtil.generateRandomString();
        headMap.put(RemoteConstant.REQUEST_REF_ID, requestId);
        headMap.put(RemoteConstant.SECRET_ID, RemoteConstant.SECRET_ID_VALUE);
        headMap.put(RemoteConstant.SIGNATURE, RemoteUtil.generateSignature(requestId));

        return headMap;
    }

    public Map<String, Object> resembleRequestMap(String phoneNum, String secretKey) {
        Map<String, Object> requestMap = new HashMap<>();

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put(RemoteConstant.MOBILE, SM3Util.encrypt(phoneNum, secretKey));
        paramMap.put(RemoteConstant.EMPOWER_NO, String.valueOf(System.currentTimeMillis()));

        requestMap.put(RemoteConstant.PARAM, paramMap);

        return requestMap;
    }
}
