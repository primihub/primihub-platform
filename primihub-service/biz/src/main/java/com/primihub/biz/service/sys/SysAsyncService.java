package com.primihub.biz.service.sys;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.tool.nodedata.AddressInfoEntity;
import com.primihub.biz.tool.nodedata.BasicIPInfoHelper;
import lombok.extern.slf4j.Slf4j;
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
import java.util.*;

@Service
@Slf4j
public class SysAsyncService {
    @Resource(name="soaRestTemplate")
    private RestTemplate restTemplate;
    @Resource
    private Environment environment;

    @Async
    public void changeExtends(){
        BasicIPInfoHelper basicIPInfoHelper = new BasicIPInfoHelper(restTemplate);
        AddressInfoEntity addressInfoEntity = basicIPInfoHelper.getAddressInfo();
        if (addressInfoEntity==null){
            log.info("获取ip信息失败 - null");
            return;
        }
        String group=environment.getProperty("nacos.config.group");
        String serverAddr=environment.getProperty("nacos.config.server-addr");
        String namespace=environment.getProperty("nacos.config.namespace");
        ConfigService configService;
        SysLocalOrganInfo sysLocalOrganInfo;
        try {
            Properties properties = new Properties();
            properties.put("serverAddr",serverAddr);
            properties.put("namespace",namespace);
            configService= NacosFactory.createConfigService(properties);
            String organInfoContent=configService.getConfig(SysConstant.SYS_ORGAN_INFO_NAME,group,3000);
            sysLocalOrganInfo = JSON.parseObject(organInfoContent,SysLocalOrganInfo.class);
        } catch (NacosException e) {
            log.info("nacos-get",e);
            return;
        }

        if(sysLocalOrganInfo==null
                ||(sysLocalOrganInfo.getOrganId()==null&& "".equals(sysLocalOrganInfo.getOrganId()))
                ||(sysLocalOrganInfo.getOrganName()==null&& "".equals(sysLocalOrganInfo.getOrganName()))
                ||(sysLocalOrganInfo.getPinCode()==null&& "".equals(sysLocalOrganInfo.getPinCode()))){
            log.info("没有机构信息请重新生成");
            return;
        }
//        if (sysLocalOrganInfo.getAddressInfo()!=null && sysLocalOrganInfo.getAddressInfo().getIp()!=null && sysLocalOrganInfo.getAddressInfo().getIp().equals(addressInfoEntity.getIp())){
//            log.info("ip 一致不需要更改");
//            return;
//        }

        if(sysLocalOrganInfo.getFusionMap()==null){
            log.info("没有中心节点map信息");
            return;
        }
        Set<String> serverAddressKeys = sysLocalOrganInfo.getFusionMap().keySet();
        Iterator<String> iterator = serverAddressKeys.iterator();
        while (iterator.hasNext()){
            String serverAddress = iterator.next();
            try{
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                MultiValueMap map = new LinkedMultiValueMap<>();
                map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
                map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
                map.put("ip", new ArrayList(){{add(addressInfoEntity.getIp());}});
                map.put("lat", new ArrayList(){{add(addressInfoEntity.getLat());}});
                map.put("lon", new ArrayList(){{add(addressInfoEntity.getLon());}});
                map.put("country", new ArrayList(){{add(addressInfoEntity.getCountry());}});
                HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
                BaseResultEntity resultEntity=restTemplate.postForObject(serverAddress+"/fusion/changeOrganExtends",request, BaseResultEntity.class);
                log.info("serverAddress:{} - ip update return Json:{}",serverAddress, JSONObject.toJSONString(resultEntity));
            }catch (Exception e){
                log.info("serverAddress:{} - e:{}",serverAddress,e.getMessage());
            }
        }
        try {
            sysLocalOrganInfo.setAddressInfo(addressInfoEntity);
            configService.publishConfig(SysConstant.SYS_ORGAN_INFO_NAME,group,JSON.toJSONString(sysLocalOrganInfo), ConfigType.JSON.getType());
        } catch (NacosException e) {
            log.info("nacos-publish",e);
        }
    }
}
