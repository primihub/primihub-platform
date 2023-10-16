package com.primihub.biz.service.sys;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.convert.SysBaseConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.sys.param.ChangeLocalOrganInfoParam;
import com.primihub.biz.entity.sys.param.ChangeOtherOrganInfoParam;
import com.primihub.biz.entity.sys.param.OrganParam;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.repository.primarydb.sys.SysOrganPrimarydbRepository;
import com.primihub.biz.repository.primaryredis.sys.SysCommonPrimaryRedisRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.data.OtherBusinessesService;
import com.primihub.biz.service.feign.FusionOrganService;
import com.primihub.biz.tool.nodedata.AddressInfoEntity;
import com.primihub.biz.tool.nodedata.BasicIPInfoHelper;
import com.primihub.biz.util.crypt.CryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

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
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private SysOrganPrimarydbRepository sysOrganPrimarydbRepository;
    @Autowired
    private SysOrganSecondarydbRepository sysOrganSecondarydbRepository;
    @Autowired
    private FusionOrganService fusionOrganService;

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
            fusionOrganService.organData(sysLocalOrganInfo.getOrganId(),sysLocalOrganInfo.getOrganName());
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
        try {
            if (!sysCommonPrimaryRedisRepository.lock(SysConstant.SYS_LOCAL_ORGAN_INFO_LOCK)) {
                return BaseResultEntity.failure(BaseResultEnum.HANDLE_RIGHT_NOW);
            }
            String group = environment.getProperty("nacos.config.group");
            String serverAddr = environment.getProperty("nacos.config.server-addr");
            String namespace = environment.getProperty("nacos.config.namespace");
            ConfigService configService;
            Properties properties = new Properties();
            properties.put("serverAddr", serverAddr);
            properties.put("namespace", namespace);
            configService = NacosFactory.createConfigService(properties);
            String organInfoContent = configService.getConfig(SysConstant.SYS_ORGAN_INFO_NAME, group, 3000);
            SysLocalOrganInfo sysLocalOrganInfo = JSON.parseObject(organInfoContent, SysLocalOrganInfo.class);
            if (sysLocalOrganInfo == null){
                sysLocalOrganInfo = new SysLocalOrganInfo();
            }
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

    public BaseResultEntity joiningPartners(String gateway, String publicKey) {
        SysOrgan sysOrgan = new SysOrgan();
        sysOrgan.setExamineState(0);
        sysOrgan.setEnable(0);
        sysOrgan.setApplyId(organConfiguration.generateUniqueCode());
        sysOrgan.setOrganGateway(gateway);
        sysOrgan.setPublicKey(publicKey);
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        Map<String,Object> map = new HashMap<>();
        map.put("organId",sysLocalOrganInfo.getOrganId());
        map.put("organName",sysLocalOrganInfo.getOrganName());
        map.put("gateway",sysLocalOrganInfo.getGatewayAddress());
        map.put("publicKey",sysLocalOrganInfo.getPublicKey());
        if (sysLocalOrganInfo.getAddressInfo()!=null){
            map.put("country",sysLocalOrganInfo.getAddressInfo().getCountry());
            map.put("lat",sysLocalOrganInfo.getAddressInfo().getLat());
            map.put("lon",sysLocalOrganInfo.getAddressInfo().getLon());
        }
        map.put("applyId",sysOrgan.getApplyId());
        try {
//            log.info(JSONObject.toJSONString(map));
            BaseResultEntity baseResultEntity = otherBusinessesService.syncGatewayApiData(map, gateway + "/share/shareData/apply", publicKey);
            if (baseResultEntity==null || !baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
                return BaseResultEntity.failure(BaseResultEnum.FAILURE,"合作方建立通信失败,请检查gateway和publicKey是否正确匹配！！！");
            }
            Map<String,Object> resultMap = (Map<String,Object>)baseResultEntity.getResult();
            sysOrgan.setOrganId(resultMap.get("organId").toString());
            if (organConfiguration.getSysLocalOrganId().equals(sysOrgan.getOrganId())){
                return BaseResultEntity.failure(BaseResultEnum.FAILURE,"合作方不可以是本机构!!!");
            }
            sysOrgan.setOrganName(resultMap.get("organName").toString());
            SysOrgan sysOrgan1 = sysOrganSecondarydbRepository.selectSysOrganByOrganId(sysOrgan.getOrganId());
//            log.info("organid:{} - sysOrgan1:{}",sysOrgan.getOrganId(), JSONObject.toJSONString(sysOrgan1));
            if (sysOrgan1!=null){
                sysOrgan.setId(sysOrgan1.getId());
                sysOrganPrimarydbRepository.updateSysOrgan(sysOrgan);
            }else {
                sysOrganPrimarydbRepository.insertSysOrgan(sysOrgan);
            }
        }catch (Exception e){
            e.printStackTrace();
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"合作方建立通信失败,请检查gateway和publicKey是否正确匹配！！！");
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity applyForJoinNode(Map<String, Object> info) {
        log.info(JSONObject.toJSONString(info));
//        SysOrgan sysOrgan = sysOrganSecondarydbRepository.selectSysOrganByApplyId(info.get("applyId").toString());
        if (organConfiguration.getSysLocalOrganId().equals(info.get("organId").toString())){
            return BaseResultEntity.success();
        }
        SysOrgan sysOrgan = sysOrganSecondarydbRepository.selectSysOrganByOrganId(info.get("organId").toString());
        if (sysOrgan==null){
            sysOrgan = new SysOrgan();
            sysOrgan.setApplyId(info.get("applyId").toString());
            sysOrgan.setOrganGateway(info.get("gateway").toString());
            sysOrgan.setPublicKey(info.get("publicKey").toString());
            sysOrgan.setOrganId(info.get("organId").toString());
            sysOrgan.setOrganName(info.get("organName").toString());
            sysOrgan.setExamineState(0);
            sysOrgan.setEnable(0);
            sysOrganPrimarydbRepository.insertSysOrgan(sysOrgan);
        }else {
            sysOrgan.setApplyId(info.get("applyId").toString());
            sysOrgan.setOrganGateway(info.get("gateway").toString());
            sysOrgan.setPublicKey(info.get("publicKey").toString());
            sysOrgan.setOrganId(info.get("organId").toString());
            sysOrgan.setOrganName(info.get("organName").toString());
            if (info.containsKey("examineState")){
                sysOrgan.setExamineState((Integer) info.get("examineState"));
            }
            if (info.containsKey("examineMsg")){
                sysOrgan.setExamineMsg(sysOrgan.getExamineMsg()+ info.get("examineMsg").toString());
            }
            if (info.containsKey("enable")){
                sysOrgan.setEnable((Integer) info.get("enable"));
            }
            sysOrganPrimarydbRepository.updateSysOrgan(sysOrgan);
            sysAsyncService.applyForJoinNode(sysOrgan);
        }
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        Map<String,Object> map = new HashMap<>();
        map.put("organId",sysLocalOrganInfo.getOrganId());
        map.put("organName",sysLocalOrganInfo.getOrganName());
        map.put("gateway",sysLocalOrganInfo.getGatewayAddress());
        map.put("publicKey",sysLocalOrganInfo.getPublicKey());
        map.put("applyId",sysOrgan.getApplyId());
        map.put("examineState",sysOrgan.getExamineState());
        map.put("enable",sysOrgan.getEnable());
        if (sysLocalOrganInfo.getAddressInfo()!=null){
            map.put("country",sysLocalOrganInfo.getAddressInfo().getCountry());
            map.put("lat",sysLocalOrganInfo.getAddressInfo().getLat());
            map.put("lon",sysLocalOrganInfo.getAddressInfo().getLon());
        }
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getOrganList(OrganParam param) {
        List<SysOrgan> list = sysOrganSecondarydbRepository.selectSysOrganByParam(param);
        if (list.size()==0){
            return BaseResultEntity.success(new PageDataEntity(0,param.getPageSize(),param.getPageNo(),new ArrayList()));
        }
        Integer count = sysOrganSecondarydbRepository.selectSysOrganByParamCount(param);
        String localOrganShortCode = organConfiguration.getLocalOrganShortCode();
        for (SysOrgan sysOrgan : list) {
            if (sysOrgan.getApplyId().contains(localOrganShortCode)){
                sysOrgan.setIdentity(0);
            }
        }
        return BaseResultEntity.success(new PageDataEntity(count,param.getPageSize(),param.getPageNo(),list));
    }

    public BaseResultEntity examineJoining(Long id, Integer examineState, String examineMsg) {
        SysOrgan sysOrgan = sysOrganSecondarydbRepository.selectSysOrganById(id);
        if (sysOrgan==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到审核机构信息");
        }
        String localOrganShortCode = organConfiguration.getLocalOrganShortCode();
        if (sysOrgan.getApplyId().contains(localOrganShortCode)&&sysOrgan.getExamineState()==0){
            return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"发起申请者不能进行审核");
        }
        if (examineState!=0){
            if (sysOrgan.getEnable()!=0){
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"机构禁用，请启用后再次申请！");
            }
        }else {
            sysOrgan.setEnable(0);
            sysOrgan.setApplyId(organConfiguration.generateUniqueCode());
        }
        sysOrgan.setExamineState(examineState);
        if (StringUtils.isNotBlank(examineMsg)){
            sysOrgan.setExamineMsg(sysOrgan.getExamineMsg()+examineMsg+"\n");
        }
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        Map<String,Object> map = new HashMap<>();
        map.put("organId",sysLocalOrganInfo.getOrganId());
        map.put("organName",sysLocalOrganInfo.getOrganName());
        map.put("gateway",sysLocalOrganInfo.getGatewayAddress());
        map.put("publicKey",sysLocalOrganInfo.getPublicKey());
        map.put("applyId",sysOrgan.getApplyId());
        map.put("examineState",sysOrgan.getExamineState());
        map.put("enable",sysOrgan.getEnable());
        log.info(JSONObject.toJSONString(map));
        BaseResultEntity baseResultEntity = otherBusinessesService.syncGatewayApiData(map, sysOrgan.getOrganGateway() + "/share/shareData/apply", sysOrgan.getPublicKey());
        if (baseResultEntity==null || !baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
            if (baseResultEntity!=null && baseResultEntity.getCode().equals(BaseResultEnum.DECRYPTION_FAILED.getReturnCode())){
                return BaseResultEntity.failure(BaseResultEnum.DECRYPTION_FAILED,"合作方publicKey已更换");
            }
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"合作方建立通信失败,请检查gateway和publicKey是否正确匹配！！！");
        }
        sysOrganPrimarydbRepository.updateSysOrgan(sysOrgan);
//        List<SysOrgan> sysOrgans = sysOrganSecondarydbRepository.selectOrganByOrganId(sysOrgan.getOrganId());
//        if (sysOrgans != null && sysOrgans.size() > 0){
//            for (SysOrgan organ : sysOrgans){
//                sysOrgan.setExamineState(examineState);
//                sysOrgan.setExamineMsg(sysOrgan.getExamineMsg()+examineMsg+"\n");
//
//            }
//        }
        sysAsyncService.applyForJoinNode(sysOrgan);
        return BaseResultEntity.success();
    }

    public BaseResultEntity enableStatus(Long id, Integer status) {
        SysOrgan sysOrgan = sysOrganSecondarydbRepository.selectSysOrganById(id);
        if (sysOrgan==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到机构信息");
        }
//        String localOrganShortCode = organConfiguration.getLocalOrganShortCode();
//        if (sysOrgan.getApplyId().contains(localOrganShortCode)){
//            return BaseResultEntity.failure(BaseResultEnum.NO_AUTH,"发起申请者不能进行状态变更");
//        }
        if (sysOrgan.getEnable().equals(status)){
            return BaseResultEntity.success();
        }
        sysOrgan.setEnable(status);
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        Map<String,Object> map = new HashMap<>();
        map.put("organId",sysLocalOrganInfo.getOrganId());
        map.put("organName",sysLocalOrganInfo.getOrganName());
        map.put("gateway",sysLocalOrganInfo.getGatewayAddress());
        map.put("publicKey",sysLocalOrganInfo.getPublicKey());
        map.put("applyId",sysOrgan.getApplyId());
        map.put("examineState",sysOrgan.getExamineState());
        map.put("enable",sysOrgan.getEnable());
        BaseResultEntity baseResultEntity = otherBusinessesService.syncGatewayApiData(map, sysOrgan.getOrganGateway() + "/share/shareData/apply", sysOrgan.getPublicKey());
        if (baseResultEntity==null || !baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"合作方建立通信失败,请检查gateway和publicKey是否正确匹配！！！");
        }
        sysOrganPrimarydbRepository.updateSysOrgan(sysOrgan);
        sysAsyncService.applyForJoinNode(sysOrgan);
        return BaseResultEntity.success();
    }

    public BaseResultEntity getAvailableOrganList() {
        return BaseResultEntity.success(sysOrganSecondarydbRepository.selectSysOrganByExamine().stream().map(SysBaseConvert::SysOrganConvertVo).collect(Collectors.toList()));
    }

    public BaseResultEntity changeOtherOrganInfo(ChangeOtherOrganInfoParam changeOtherOrganInfoParam) {
        // 查询机构信息
        List<SysOrgan> sysOrgans = sysOrganSecondarydbRepository.selectOrganByOrganId(changeOtherOrganInfoParam.getOrganId());
        if (sysOrgans == null || sysOrgans.size() <=0 ){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到机构信息");
        }
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        Map<String,Object> map = new HashMap<>();
        map.put("organId",sysLocalOrganInfo.getOrganId());
        map.put("organName",sysLocalOrganInfo.getOrganName());
        map.put("gateway",sysLocalOrganInfo.getGatewayAddress());
        map.put("publicKey",sysLocalOrganInfo.getPublicKey());
        map.put("applyId",sysOrgans.get(0).getApplyId());
        // 通过修改的 网关 和 公钥 测试连接
        BaseResultEntity baseResultEntity = otherBusinessesService.syncGatewayApiData(map,
                changeOtherOrganInfoParam.getGatewayAddress() + "/share/shareData/apply", changeOtherOrganInfoParam.getPublicKey());
        if (baseResultEntity==null || !baseResultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())){
            if (baseResultEntity!=null && baseResultEntity.getCode().equals(BaseResultEnum.DECRYPTION_FAILED.getReturnCode())){
                return BaseResultEntity.failure(BaseResultEnum.DECRYPTION_FAILED,"合作方publicKey已更换");
            }
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"合作方建立通信失败,请检查gateway和publicKey是否正确匹配！！！");
        }
        for (SysOrgan sysOrgan : sysOrgans){
            sysOrgan.setOrganGateway(changeOtherOrganInfoParam.getGatewayAddress());
            sysOrgan.setPublicKey(changeOtherOrganInfoParam.getPublicKey());
            sysOrganPrimarydbRepository.updateSysOrgan(sysOrgan);
        }
        return BaseResultEntity.success("修改成功");
    }
}
