package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSONObject;
import com.anji.captcha.util.StringUtils;
import com.primihub.biz.config.base.ClientConfiguration;
import com.primihub.biz.constant.RemoteConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.po.ScoreModel;
import com.primihub.biz.entity.data.vo.RemoteRespVo;
import com.primihub.biz.repository.primarydb.data.ScoreModelPrRepository;
import com.primihub.biz.repository.secondarydb.data.ScoreModelRepository;
import com.primihub.biz.util.crypt.RemoteUtil;
import com.primihub.biz.util.crypt.SM3Util;
import com.primihub.biz.util.crypt.SM4Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Component
public class RemoteClient {
    @Resource(name = "soaRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private ClientConfiguration clientConfiguration;
    @Autowired
    private ScoreModelPrRepository scoreModelPrRepository;
    @Autowired
    private ScoreModelRepository scoreModelRepository;

    public RemoteRespVo queryFromRemote(String phoneNum,String scoreTypeValue) {
        ScoreModel scoreModel = scoreModelRepository.selectScoreModelByScoreTypeValue(scoreTypeValue);
        if (scoreModel == null) {
            log.error("scoreModelType not found : {}", scoreTypeValue);
            return null;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        Map<String, Object> map = new HashMap<>();
        map.put(RemoteConstant.HEAD, resembleHeadMap());

        Map<String, Object> requestMap = resembleRequestMap(phoneNum, clientConfiguration.getSecretKey());
        log.info("request param before encrypt: {}", JSONObject.toJSONString(requestMap));
        String mapJson = JSONObject.toJSONString(requestMap);
        String requestEncString = SM4Util.encrypt(mapJson, clientConfiguration.getSecretKey());
        map.put(RemoteConstant.REQUEST, requestEncString);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(map, headers);
        String url = RemoteConstant.REMOTE_SCORE_URL + scoreModel.getScoreModelCode();
        log.info("remote request body: {}", JSONObject.toJSONString(map));
        RemoteRespVo respVo = restTemplate.postForObject(url, request, RemoteRespVo.class);
        log.info("remote result: {}", JSONObject.toJSONString(respVo));
        if (Objects.nonNull(respVo) && Objects.equals(respVo.getHead().getResult(), "Y")
                && StringUtils.isNotBlank(respVo.getResponse())) {
            String jsonResponse = null;
            try {
                jsonResponse = SM4Util.decrypt(respVo.getResponse(), clientConfiguration.getSecretKey());
                log.info("remote decrypt result: {}", jsonResponse);
                RemoteRespVo.RespResp respResp = JSONObject.parseObject(jsonResponse, RemoteRespVo.RespResp.class);
                respVo.setRespBody(respResp);
            } catch (Exception e) {
                log.error("remote response sm4 parse error : {}", e.getMessage());
                return null;
            }
        }
        log.info("remote parse result: {}", JSONObject.toJSONString(respVo));
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

    /**
     *
     * @param req
     * @return
     */
    public BaseResultEntity submitScoreModelType(ScoreModel req) {
        try {
            scoreModelPrRepository.saveScoreModel(req);
        } catch (Exception e) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_DB_FAIL, "保存失败");
        }
        Set<ScoreModel> scoreModelSet = scoreModelRepository.selectAll();
        return BaseResultEntity.success(scoreModelSet);
    }
}
