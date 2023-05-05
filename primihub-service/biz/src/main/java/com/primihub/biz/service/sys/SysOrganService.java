package com.primihub.biz.service.sys;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.param.ChangeLocalOrganInfoParam;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrganFusion;
import com.primihub.biz.repository.primaryredis.sys.SysCommonPrimaryRedisRepository;
import com.primihub.biz.tool.nodedata.AddressInfoEntity;
import com.primihub.biz.tool.nodedata.BasicIPInfoHelper;
import com.primihub.biz.util.crypt.CryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
public class SysOrganService {

    @Resource
    private Environment environment;
    @Resource(name = "soaRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private SysCommonPrimaryRedisRepository sysCommonPrimaryRedisRepository;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private SysAsyncService sysAsyncService;

    public BaseResultEntity getLocalOrganInfo() {
        String group = environment.getProperty("nacos.config.group");
        String serverAddr = environment.getProperty("nacos.config.server-addr");
        String namespace = environment.getProperty("nacos.config.namespace");
        ConfigService configService;
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            properties.put("namespace", namespace);
            configService = NacosFactory.createConfigService(properties);
            String organInfoContent = configService.getConfig(SysConstant.SYS_ORGAN_INFO_NAME, group, 3000);
            SysLocalOrganInfo sysLocalOrganInfo = JSON.parseObject(organInfoContent, SysLocalOrganInfo.class);
            Map result = new HashMap();
            result.put("sysLocalOrganInfo", sysLocalOrganInfo);
            return BaseResultEntity.success(result);
        } catch (NacosException e) {
            log.info("getLocalOrganInfo", e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE, e.getMessage());
        }
    }

    public BaseResultEntity changeLocalOrganInfo(ChangeLocalOrganInfoParam changeLocalOrganInfoParam) {
        if (!sysCommonPrimaryRedisRepository.lock(SysConstant.SYS_LOCAL_ORGAN_INFO_LOCK)) {
            return BaseResultEntity.failure(BaseResultEnum.HANDLE_RIGHT_NOW);
        }
        String group = environment.getProperty("nacos.config.group");
        String serverAddr = environment.getProperty("nacos.config.server-addr");
        String namespace = environment.getProperty("nacos.config.namespace");
        ConfigService configService;
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            properties.put("namespace", namespace);
            configService = NacosFactory.createConfigService(properties);
            String organInfoContent = configService.getConfig(SysConstant.SYS_ORGAN_INFO_NAME, group, 3000);
            SysLocalOrganInfo sysLocalOrganInfo = JSON.parseObject(organInfoContent, SysLocalOrganInfo.class);
            int flag = 0;
            if (sysLocalOrganInfo == null) {
                sysLocalOrganInfo = new SysLocalOrganInfo();
            }
            if (sysLocalOrganInfo.getOrganId() == null || "".equals(sysLocalOrganInfo.getOrganId().trim())) {
                sysLocalOrganInfo.setOrganId(UUID.randomUUID().toString());
                flag |= 1;
            }
            if (changeLocalOrganInfoParam.getOrganName() != null && !"".equals(changeLocalOrganInfoParam.getOrganName().trim())) {
                sysLocalOrganInfo.setOrganName(changeLocalOrganInfoParam.getOrganName());
                flag |= 1;
            }
            if (changeLocalOrganInfoParam.getGatewayAddress() != null && !"".equals(changeLocalOrganInfoParam.getGatewayAddress().trim())) {
                sysLocalOrganInfo.setGatewayAddress(changeLocalOrganInfoParam.getGatewayAddress());
                flag |= 1;
            }
            if (sysLocalOrganInfo != null && (
                    sysLocalOrganInfo.getOrganName() == null || "".equals(changeLocalOrganInfoParam.getOrganName().trim())
                            || sysLocalOrganInfo.getGatewayAddress() == null || "".equals(changeLocalOrganInfoParam.getGatewayAddress().trim()))) {
                sysCommonPrimaryRedisRepository.unlock(SysConstant.SYS_LOCAL_ORGAN_INFO_LOCK);
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "缺少参数organName和gatewayAddress");
            }
            if (StringUtils.isBlank(sysLocalOrganInfo.getPrivateKey())) {
                String[] rsaKeyPair = CryptUtil.genRsaKeyPair();
                sysLocalOrganInfo.setPublicKey(rsaKeyPair[0]);
                sysLocalOrganInfo.setPrivateKey(rsaKeyPair[1]);
            }
            if (flag == 1) {
                if (sysLocalOrganInfo.getAddressInfo()==null){
                    try {
                        BasicIPInfoHelper basicIPInfoHelper = new BasicIPInfoHelper(restTemplate);
                        AddressInfoEntity addressInfoEntity = basicIPInfoHelper.getAddressInfo();
                        sysLocalOrganInfo.setAddressInfo(addressInfoEntity);
                    }catch (Exception e){
                        log.info("Exception in obtaining address will not be reported");
                    }
                }
                configService.publishConfig(SysConstant.SYS_ORGAN_INFO_NAME, group, JSON.toJSONString(sysLocalOrganInfo), ConfigType.JSON.getType());
                sysAsyncService.collectBaseData();
            }
            Map result = new HashMap();
            result.put("sysLocalOrganInfo", sysLocalOrganInfo);
            return BaseResultEntity.success(result);
        } catch (Exception e) {
            log.info("changeLocalOrganInfo", e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE, e.getMessage());
        } finally {
            sysCommonPrimaryRedisRepository.unlock(SysConstant.SYS_LOCAL_ORGAN_INFO_LOCK);
        }
    }

    public BaseResultEntity changeHomepage(Map<String, Object> homeMap) {
        if (!sysCommonPrimaryRedisRepository.lock(SysConstant.SYS_LOCAL_ORGAN_INFO_LOCK)) {
            return BaseResultEntity.failure(BaseResultEnum.HANDLE_RIGHT_NOW);
        }
        String group = environment.getProperty("nacos.config.group");
        String serverAddr = environment.getProperty("nacos.config.server-addr");
        String namespace = environment.getProperty("nacos.config.namespace");
        ConfigService configService;
        try {
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            properties.put("namespace", namespace);
            configService = NacosFactory.createConfigService(properties);
            String organInfoContent = configService.getConfig(SysConstant.SYS_ORGAN_INFO_NAME, group, 3000);
            SysLocalOrganInfo sysLocalOrganInfo = JSON.parseObject(organInfoContent, SysLocalOrganInfo.class);
            if (sysLocalOrganInfo.getHomeMap() == null) {
                sysLocalOrganInfo.setHomeMap(homeMap);
            } else {
                sysLocalOrganInfo.getHomeMap().putAll(homeMap);
            }
            log.info("send publish config");
            configService.publishConfig(SysConstant.SYS_ORGAN_INFO_NAME, group, JSON.toJSONString(sysLocalOrganInfo), ConfigType.JSON.getType());
            Map result = new HashMap();
            result.put("sysLocalOrganInfo", sysLocalOrganInfo);
            return BaseResultEntity.success(result);
        } catch (NacosException e) {
            log.info("changeLocalOrganInfo", e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE, e.getMessage());
        } finally {
            sysCommonPrimaryRedisRepository.unlock(SysConstant.SYS_LOCAL_ORGAN_INFO_LOCK);
        }
    }

    public BaseResultEntity getHomepage() {
        if (organConfiguration.getSysLocalOrganInfo() == null) {
            return BaseResultEntity.success();
        }
        return BaseResultEntity.success(organConfiguration.getSysLocalOrganInfo().getHomeMap());
    }

}
