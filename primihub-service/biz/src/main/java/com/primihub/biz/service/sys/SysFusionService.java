package com.primihub.biz.service.sys;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.entity.base.BaseFunctionHandleEntity;
import com.primihub.biz.entity.base.BaseFunctionHandleEnum;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrganFusion;
import com.primihub.biz.tool.nodedata.AddressInfoEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;

@Slf4j
@Service
public class SysFusionService {

    @Resource(name="soaRestTemplate")
    private RestTemplate restTemplate;
    @Resource
    private Environment environment;
    @Autowired
    private SingleTaskChannel singleTaskChannel;
    @Autowired
    private SysAsyncService sysAsyncService;

    public BaseResultEntity healthConnection(String serverAddress){
        try{
            BaseResultEntity resultEntity=restTemplate.getForObject(serverAddress+"/fusion/healthConnection", BaseResultEntity.class);
            if (resultEntity.getCode() != BaseResultEnum.SUCCESS.getReturnCode())
                return BaseResultEntity.failure(BaseResultEnum.FAILURE);
        }catch (Exception e){
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }
        return BaseResultEntity.success("connection success");
    }

    public BaseResultEntity registerConnection(String serverAddress){
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
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }

        if(sysLocalOrganInfo==null
                ||(sysLocalOrganInfo.getOrganId()==null&&sysLocalOrganInfo.getOrganId().equals(""))
                ||(sysLocalOrganInfo.getOrganName()==null&&sysLocalOrganInfo.getOrganName().equals(""))
                ||(sysLocalOrganInfo.getPinCode()==null&&sysLocalOrganInfo.getPinCode().equals("")))
            return BaseResultEntity.failure(BaseResultEnum.NO_ORGAN_DATA);

        if(sysLocalOrganInfo.getFusionMap()==null)
            sysLocalOrganInfo.setFusionMap(new HashMap<>());
        if (sysLocalOrganInfo.getFusionMap().containsKey(serverAddress)&&sysLocalOrganInfo.getFusionMap().get(serverAddress).isRegistered())
            return BaseResultEntity.failure(BaseResultEnum.NON_REPEATABLE,"节点已注册");
        SysOrganFusion currentFusion=sysLocalOrganInfo.getFusionMap().getOrDefault(serverAddress,new SysOrganFusion(serverAddress,false,true));
        boolean isRegistered=true;
        String fusionMsg="注册成功";
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
            map.put("globalName", new ArrayList(){{add(sysLocalOrganInfo.getOrganName());}});
            map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
            map.put("gatewayAddress", new ArrayList(){{add(sysLocalOrganInfo.getGatewayAddress());}});
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            BaseResultEntity resultEntity=restTemplate.postForObject(serverAddress+"/fusion/registerConnection",request, BaseResultEntity.class);
            if (resultEntity.getCode() != BaseResultEnum.SUCCESS.getReturnCode()) {
                isRegistered = false;
                fusionMsg = resultEntity.getMsg();
            }
        }catch (Exception e){
            isRegistered=false;
            fusionMsg=e.getMessage();
        }

        currentFusion.setRegistered(isRegistered);
        sysLocalOrganInfo.getFusionMap().put(serverAddress,currentFusion);

        try {
            configService.publishConfig(SysConstant.SYS_ORGAN_INFO_NAME,group,JSON.toJSONString(sysLocalOrganInfo), ConfigType.JSON.getType());
            sysAsyncService.changeExtends();
        } catch (NacosException e) {
            log.info("nacos-publish",e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }
        if (isRegistered)
            singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.BATCH_DATA_FUSION_RESOURCE_TASK.getHandleType(),currentFusion))).build());
        Map result=new HashMap();
        result.put("fusionMsg",fusionMsg);
        result.put("isRegistered",isRegistered);
        return BaseResultEntity.success(result);
    }

    public BaseResultEntity deleteConnection(String serverAddress) {
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
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }
        if(sysLocalOrganInfo==null
                ||(sysLocalOrganInfo.getOrganId()==null&&sysLocalOrganInfo.getOrganId().equals(""))
                ||(sysLocalOrganInfo.getOrganName()==null&&sysLocalOrganInfo.getOrganName().equals(""))
                ||(sysLocalOrganInfo.getPinCode()==null&&sysLocalOrganInfo.getPinCode().equals("")))
            return BaseResultEntity.failure(BaseResultEnum.NO_ORGAN_DATA);
        if (sysLocalOrganInfo.getFusionMap()==null||sysLocalOrganInfo.getFusionMap().size()==0)
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_DELETE,"无连接信息");
        if (!sysLocalOrganInfo.getFusionMap().containsKey(serverAddress))
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_DELETE,"该连接不存在");
        SysOrganFusion sysOrganFusion = sysLocalOrganInfo.getFusionMap().get(serverAddress);
        if (sysOrganFusion.isRegistered())
            return BaseResultEntity.failure(BaseResultEnum.CAN_NOT_DELETE,"连接无法删除");
        sysLocalOrganInfo.getFusionMap().remove(serverAddress);
        try {
            configService.publishConfig(SysConstant.SYS_ORGAN_INFO_NAME,group,JSON.toJSONString(sysLocalOrganInfo), ConfigType.JSON.getType());
        } catch (NacosException e) {
            log.info("nacos-publish",e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }
        return BaseResultEntity.success("删除成功");
    }

    public SysLocalOrganInfo getSysLocalOrganInfo() throws NacosException{
        String group=environment.getProperty("nacos.config.group");
        String serverAddr=environment.getProperty("nacos.config.server-addr");
        String namespace=environment.getProperty("nacos.config.namespace");
        Properties properties = new Properties();
        properties.put("serverAddr",serverAddr);
        properties.put("namespace",namespace);
        ConfigService configService= NacosFactory.createConfigService(properties);
        String organInfoContent=configService.getConfig(SysConstant.SYS_ORGAN_INFO_NAME,group,3000);
        return JSON.parseObject(organInfoContent,SysLocalOrganInfo.class);
    }

    public BaseResultEntity createGroup(String serverAddress,String groupName){
        SysLocalOrganInfo sysLocalOrganInfo;
        try {
            sysLocalOrganInfo = getSysLocalOrganInfo();
        } catch (NacosException e) {
            log.info("nacos-get",e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }

        String fusionMsg="创建成功";
        Map result=new HashMap();
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("groupName", new ArrayList(){{add(groupName);}});
            map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
            map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            BaseResultEntity resultEntity=restTemplate.postForObject(serverAddress+"/group/createGroup",request, BaseResultEntity.class);
            if (resultEntity.getCode() != BaseResultEnum.SUCCESS.getReturnCode())
                fusionMsg=resultEntity.getMsg();
            result.put("groupData",resultEntity.getResult());
        }catch (Exception e){
            fusionMsg=e.getMessage();
        }
        result.put("fusionMsg",fusionMsg);
        return BaseResultEntity.success(result);
    }

    public BaseResultEntity findAllGroup(String serverAddress){
        SysLocalOrganInfo sysLocalOrganInfo;
        try {
            sysLocalOrganInfo = getSysLocalOrganInfo();
        } catch (NacosException e) {
            log.info("nacos-get",e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }
        String fusionMsg="查询成功";
        Map result=new HashMap();
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
            map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            BaseResultEntity resultEntity=restTemplate.postForObject(serverAddress+"/group/findAllGroup",request, BaseResultEntity.class);
            if (resultEntity.getCode() != BaseResultEnum.SUCCESS.getReturnCode())
                fusionMsg=resultEntity.getMsg();
            result.put("organList",resultEntity.getResult());
        }catch (Exception e){
            fusionMsg=e.getMessage();
        }
        result.put("fusionMsg",fusionMsg);
        return BaseResultEntity.success(result);
    }

    public BaseResultEntity joinGroup(String serverAddress,Long groupId){
        SysLocalOrganInfo sysLocalOrganInfo;
        try {
            sysLocalOrganInfo = getSysLocalOrganInfo();
        } catch (NacosException e) {
            log.info("nacos-get",e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }
        String fusionMsg="加入成功";
        Map result=new HashMap();
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("groupId", new ArrayList(){{add(groupId);}});
            map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
            map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            BaseResultEntity resultEntity=restTemplate.postForObject(serverAddress+"/group/joinGroup",request, BaseResultEntity.class);
            if (resultEntity.getCode() != BaseResultEnum.SUCCESS.getReturnCode())
                fusionMsg=resultEntity.getMsg();
        }catch (Exception e){
            fusionMsg=e.getMessage();
        }
        result.put("fusionMsg",fusionMsg);
        return BaseResultEntity.success(result);
    }

    public BaseResultEntity exitGroup(String serverAddress,Long groupId){
        SysLocalOrganInfo sysLocalOrganInfo;
        try {
            sysLocalOrganInfo = getSysLocalOrganInfo();
        } catch (NacosException e) {
            log.info("nacos-get",e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }
        String fusionMsg="退出成功";
        Map result=new HashMap();
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("groupId", new ArrayList(){{add(groupId);}});
            map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
            map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            BaseResultEntity resultEntity=restTemplate.postForObject(serverAddress+"/group/exitGroup",request, BaseResultEntity.class);
            if (resultEntity.getCode() != BaseResultEnum.SUCCESS.getReturnCode())
                fusionMsg=resultEntity.getMsg();
        }catch (Exception e){
            fusionMsg=e.getMessage();
        }
        result.put("fusionMsg",fusionMsg);
        return BaseResultEntity.success(result);
    }

    public BaseResultEntity findOrganInGroup(String serverAddress,Long groupId){
        SysLocalOrganInfo sysLocalOrganInfo;
        try {
            sysLocalOrganInfo = getSysLocalOrganInfo();
        } catch (NacosException e) {
            log.info("nacos-get",e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }
        String fusionMsg="查询成功";
        Map result=new HashMap();
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("groupId", new ArrayList(){{add(groupId);}});
            map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
            map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            BaseResultEntity resultEntity=restTemplate.postForObject(serverAddress+"/group/findOrganInGroup",request, BaseResultEntity.class);
            if (resultEntity.getCode() != BaseResultEnum.SUCCESS.getReturnCode())
                fusionMsg=resultEntity.getMsg();
            result.put("dataList",resultEntity.getResult());
        }catch (Exception e){
            fusionMsg=e.getMessage();
        }
        result.put("fusionMsg",fusionMsg);
        return BaseResultEntity.success(result);
    }

    public BaseResultEntity findMyGroupOrgan(String serverAddress){
        SysLocalOrganInfo sysLocalOrganInfo;
        try {
            sysLocalOrganInfo = getSysLocalOrganInfo();
        } catch (NacosException e) {
            log.info("nacos-get",e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }
        String fusionMsg="查询成功";
        Map result=new HashMap();
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
            map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            BaseResultEntity resultEntity=restTemplate.postForObject(serverAddress+"/group/findMyGroupOrgan",request, BaseResultEntity.class);
            if (resultEntity.getCode() != BaseResultEnum.SUCCESS.getReturnCode())
                fusionMsg=resultEntity.getMsg();
            result.put("dataList",resultEntity.getResult());
        }catch (Exception e){
            fusionMsg=e.getMessage();
        }
        result.put("fusionMsg",fusionMsg);
        return BaseResultEntity.success(result);
    }


    public BaseResultEntity getOrganExtendsList(String serverAddress) {
        SysLocalOrganInfo sysLocalOrganInfo;
        try {
            sysLocalOrganInfo = getSysLocalOrganInfo();
        } catch (NacosException e) {
            log.info("nacos-get",e);
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,e.getMessage());
        }
        String fusionMsg="查询成功";
        Map result=new HashMap();
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
            map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            BaseResultEntity resultEntity=restTemplate.postForObject(serverAddress+"/fusion/getOrganExtendsList",request, BaseResultEntity.class);
            if (resultEntity.getCode() != BaseResultEnum.SUCCESS.getReturnCode())
                fusionMsg=resultEntity.getMsg();
            result.put("dataList",resultEntity.getResult());
        }catch (Exception e){
            fusionMsg=e.getMessage();
        }
        result.put("fusionMsg",fusionMsg);
        return BaseResultEntity.success(result);
    }
}
