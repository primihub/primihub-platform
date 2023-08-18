package com.primihub.biz.service.sys;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.repository.primaryredis.sys.SysCommonPrimaryRedisRepository;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.util.crypt.CryptUtil;
import com.primihub.biz.util.crypt.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class SysCommonService {

    @Autowired
    private SysCommonPrimaryRedisRepository sysCommonPrimaryRedisRepository;
    @Resource
    private RestTemplate restTemplate;

    public BaseResultEntity getValidatePublicKey() {
        String[] rsaKeyPair;
        try {
            rsaKeyPair=CryptUtil.genRsaKeyPair();
        } catch (Exception e) {
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"生成加密串错误");
        }
        Date date=new Date();
        String dateStr=DateUtil.formatDate(date,DateUtil.DateStyle.TIME_FORMAT_SHORT.getFormat());
        Long seq=sysCommonPrimaryRedisRepository.getCurrentSecondIncr(dateStr);
        String seqStr = String.format("%06d", seq);
        String publicKey=new StringBuilder().append(SysConstant.SYS_COMMON_PUBLIC_KEY_PREFIX).append(dateStr).append(seqStr).toString();

        sysCommonPrimaryRedisRepository.setRsaKey(publicKey,rsaKeyPair[1]);

        Map map=new HashMap<>();
        map.put("publicKeyName",publicKey);
        map.put("publicKey",rsaKeyPair[0]);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getCollectList() {
        ResponseEntity<JSONObject> ipInfoData = restTemplate.getForEntity(SysConstant.SYS_QUERY_COLLECT_URL, JSONObject.class);
        log.info("获取客户端ip json:{}",JSONObject.toJSONString(ipInfoData));
        if (ipInfoData==null || ipInfoData.getBody()==null){
            return null;
        }
        JSONObject body = ipInfoData.getBody();
        return BaseResultEntity.success(body.getJSONArray("data"));
    }
}
