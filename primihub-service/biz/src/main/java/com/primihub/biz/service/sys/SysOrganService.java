package com.primihub.biz.service.sys;

import com.alibaba.fastjson.JSON;
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
    @Resource(name="soaRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private SysCommonPrimaryRedisRepository sysCommonPrimaryRedisRepository;
    @Autowired
    private OrganConfiguration organConfiguration;

    public BaseResultEntity getLocalOrganInfo(){
        String group=environment.getProperty("nacos.config.group");
        String serverAddr=environment.getProperty("nacos.config.server-addr");
        String namespace=environment.getProperty("nacos.config.namespace");
        ConfigService configService;
        try {
            Properties properties = new Properties();
            properties.put("serverAddr",serverAddr);
            properties.put("namespace",namespace);
            configService= NacosFactory.createConfigService(properties);
            String organInfoContent=configService.getConfig(SysConstant.SYS_ORGAN_INFO_NAME,group,3000);
            SysLocalOrganInfo sysLocalOrganInfo=JSON.parseObject(organInfoContent,SysLocalOrganInfo.class);
            if (sysLocalOrganInfo!=null){
                List<SysOrganFusion> fusionList=new ArrayList<>();
                if(sysLocalOrganInfo.getFusionMap()!=null) {
                    Iterator<String> iterator = sysLocalOrganInfo.getFusionMap().keySet().iterator();
                    while(iterator.hasNext()){
                        fusionList.add(sysLocalOrganInfo.getFusionMap().get(iterator.next()));
                    }
                }
                sysLocalOrganInfo.setFusionList(fusionList);
            }
            Map result=new HashMap();
            result.put("sysLocalOrganInfo",sysLocalOrganInfo);
            return BaseResultEntity.success(result);
        } catch (NacosException e) {
            log.info("getLocalOrganInfo",e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }
    }

   public BaseResultEntity changeLocalOrganInfo(ChangeLocalOrganInfoParam changeLocalOrganInfoParam){
       if(!sysCommonPrimaryRedisRepository.lock(SysConstant.SYS_LOCAL_ORGAN_INFO_LOCK)) {
           return BaseResultEntity.failure(BaseResultEnum.HANDLE_RIGHT_NOW);
       }
       String group=environment.getProperty("nacos.config.group");
       String serverAddr=environment.getProperty("nacos.config.server-addr");
       String namespace=environment.getProperty("nacos.config.namespace");
       ConfigService configService;
       try {
           Properties properties = new Properties();
           properties.put("serverAddr",serverAddr);
           properties.put("namespace",namespace);
           configService= NacosFactory.createConfigService(properties);
           String organInfoContent=configService.getConfig(SysConstant.SYS_ORGAN_INFO_NAME,group,3000);
           SysLocalOrganInfo sysLocalOrganInfo=JSON.parseObject(organInfoContent,SysLocalOrganInfo.class);
           int flag=0;
           if(sysLocalOrganInfo==null) {
               sysLocalOrganInfo=new SysLocalOrganInfo();
           }
           if(sysLocalOrganInfo.getOrganId()==null|| "".equals(sysLocalOrganInfo.getOrganId().trim())){
               sysLocalOrganInfo.setOrganId(UUID.randomUUID().toString());
               flag|=1;
           }
           if(sysLocalOrganInfo.getPinCode()==null|| "".equals(sysLocalOrganInfo.getPinCode().trim())){
               sysLocalOrganInfo.setPinCode(RandomStringUtils.randomAlphanumeric(16));
               flag|=1;
           }
           if(changeLocalOrganInfoParam.getOrganName()!=null&&!"".equals(changeLocalOrganInfoParam.getOrganName().trim())){
               sysLocalOrganInfo.setOrganName(changeLocalOrganInfoParam.getOrganName());
               flag|=1;
           }
           if(changeLocalOrganInfoParam.getGatewayAddress()!=null&&!"".equals(changeLocalOrganInfoParam.getGatewayAddress().trim())){
               sysLocalOrganInfo.setGatewayAddress(changeLocalOrganInfoParam.getGatewayAddress());
               flag|=1;
           }
           if(sysLocalOrganInfo!=null&&(
                   sysLocalOrganInfo.getOrganName()==null|| "".equals(changeLocalOrganInfoParam.getOrganName().trim())
                ||sysLocalOrganInfo.getGatewayAddress()==null|| "".equals(changeLocalOrganInfoParam.getGatewayAddress().trim()))) {
               sysCommonPrimaryRedisRepository.unlock(SysConstant.SYS_LOCAL_ORGAN_INFO_LOCK);
               return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "缺少参数organName和gatewayAddress");
           }
           if (StringUtils.isBlank(sysLocalOrganInfo.getPrivateKey())){
               String[] rsaKeyPair= CryptUtil.genRsaKeyPair();
               sysLocalOrganInfo.setPublicKey(rsaKeyPair[0]);
               sysLocalOrganInfo.setPrivateKey(rsaKeyPair[1]);
           }
           if(flag==1) {
               configService.publishConfig(SysConstant.SYS_ORGAN_INFO_NAME, group, JSON.toJSONString(sysLocalOrganInfo), ConfigType.JSON.getType());
               if (sysLocalOrganInfo.getFusionMap() != null && sysLocalOrganInfo.getFusionMap().size() > 0) {
                   String organId = sysLocalOrganInfo.getOrganId();
                   String organName = sysLocalOrganInfo.getOrganName();
                   String gatewayAddress = sysLocalOrganInfo.getGatewayAddress();
                   String pinCode = sysLocalOrganInfo.getPinCode();
                   HttpHeaders headers = new HttpHeaders();
                   headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                   Iterator<Map.Entry<String, SysOrganFusion>> iterator = sysLocalOrganInfo.getFusionMap().entrySet().iterator();
                   while (iterator.hasNext()) {
                       SysOrganFusion sysOrganFusion = iterator.next().getValue();
                       try {
                           MultiValueMap map = new LinkedMultiValueMap<>();
                           map.put("globalId", new ArrayList() {{add(organId);}});
                           map.put("globalName", new ArrayList() {{add(organName);}});
                           map.put("pinCode", new ArrayList() {{add(pinCode);}});
                           map.put("gatewayAddress", new ArrayList() {{add(gatewayAddress);}});
                           SysLocalOrganInfo finalSysLocalOrganInfo = sysLocalOrganInfo;
                           map.put("publicKey", new ArrayList(){{add(finalSysLocalOrganInfo.getPublicKey());}});
                           map.put("privateKey", new ArrayList(){{add(finalSysLocalOrganInfo.getPrivateKey());}});
                           HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
                           BaseResultEntity resultEntity = restTemplate.postForObject(sysOrganFusion.getServerAddress() + "/fusion/changeConnection", request, BaseResultEntity.class);
                           log.info("changeOrganInfo serverAddress:{} | param -- organId:{},globalName:{}  | result -- code:{},msg:{},result:{}", sysOrganFusion.getServerAddress(), organId, organName, resultEntity.getCode(), resultEntity.getMsg(), resultEntity.getResult());
                       } catch (Exception e) {
                           log.info("changeOrganInfoException serverAddress:{} | param -- organId:{},globalName:{} | message:{}", sysOrganFusion.getServerAddress(), organId, organName, e.getMessage());
                       }
                   }
               }
           }

           Map result=new HashMap();
           result.put("sysLocalOrganInfo",sysLocalOrganInfo);
           return BaseResultEntity.success(result);
       } catch (Exception e) {
           log.info("changeLocalOrganInfo",e);
           return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
       } finally {
           sysCommonPrimaryRedisRepository.unlock(SysConstant.SYS_LOCAL_ORGAN_INFO_LOCK);
       }
   }

    public BaseResultEntity changeHomepage(Map<String, Object> homeMap) {
        if(!sysCommonPrimaryRedisRepository.lock(SysConstant.SYS_LOCAL_ORGAN_INFO_LOCK)) {
            return BaseResultEntity.failure(BaseResultEnum.HANDLE_RIGHT_NOW);
        }
        String group=environment.getProperty("nacos.config.group");
        String serverAddr=environment.getProperty("nacos.config.server-addr");
        String namespace=environment.getProperty("nacos.config.namespace");
        ConfigService configService;
        try {
            Properties properties = new Properties();
            properties.put("serverAddr",serverAddr);
            properties.put("namespace",namespace);
            configService= NacosFactory.createConfigService(properties);
            String organInfoContent=configService.getConfig(SysConstant.SYS_ORGAN_INFO_NAME,group,3000);
            SysLocalOrganInfo sysLocalOrganInfo=JSON.parseObject(organInfoContent,SysLocalOrganInfo.class);
            if(sysLocalOrganInfo.getHomeMap()==null){
                sysLocalOrganInfo.setHomeMap(homeMap);
            }else {
                sysLocalOrganInfo.getHomeMap().putAll(homeMap);
            }
            log.info("send publish config");
            configService.publishConfig(SysConstant.SYS_ORGAN_INFO_NAME, group, JSON.toJSONString(sysLocalOrganInfo), ConfigType.JSON.getType());
            Map result=new HashMap();
            result.put("sysLocalOrganInfo",sysLocalOrganInfo);
            return BaseResultEntity.success(result);
        } catch (NacosException e) {
            log.info("changeLocalOrganInfo",e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        } finally {
            sysCommonPrimaryRedisRepository.unlock(SysConstant.SYS_LOCAL_ORGAN_INFO_LOCK);
        }
    }

    public BaseResultEntity getHomepage() {
        if (organConfiguration.getSysLocalOrganInfo()==null){
            return BaseResultEntity.success();
        }
        return BaseResultEntity.success(organConfiguration.getSysLocalOrganInfo().getHomeMap());
    }
}
