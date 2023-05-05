package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.constant.CommonConstant;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.req.DataFResourceReq;
import com.primihub.biz.entity.data.req.OrganResourceReq;
import com.primihub.biz.entity.fusion.param.OrganResourceParam;
import com.primihub.biz.entity.fusion.param.ResourceParam;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.feign.FusionOrganService;
import com.primihub.biz.feign.FusionResourceService;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.util.crypt.CryptUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OtherBusinessesService {

    @Resource(name="soaRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private FusionResourceService fusionResourceService;
    @Autowired
    private SysOrganSecondarydbRepository sysOrganSecondarydbRepository;


    public BaseResultEntity getResourceList(DataFResourceReq req) {
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        try{
            ResourceParam param = new ResourceParam();
            param.setGlobalId(sysLocalOrganInfo.getOrganId());
            param.setResourceId(req.getResourceId());
            param.setResourceName(req.getResourceName());
            param.setResourceType(req.getResourceSource());
            param.setOrganId(req.getOrganId());
            param.setFileContainsY(req.getFileContainsY());
            param.setTagName(req.getTagName());
            param.setPageNo(req.getPageNo());
            param.setPageSize(req.getPageSize());
            BaseResultEntity resultEntity= fusionResourceService.getResourceList(param);
            return BaseResultEntity.success(resultEntity.getResult());
        }catch (Exception e){
            log.info("元数据资源数据异常:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"请求元数据资源失败");
        }
    }

    public BaseResultEntity getOrganResourceList(OrganResourceReq req) {
        try{
            OrganResourceParam param = new OrganResourceParam();
            param.setResourceName(req.getResourceName());
            param.setColumnStr(req.getColumnStr());
            param.setOrganId(req.getOrganId());
            param.setAuditStatus(req.getAuditStatus());
            param.setPageNo(req.getPageNo());
            param.setPageSize(req.getPageSize());
            BaseResultEntity resultEntity= fusionResourceService.getOrganResourceList(param);
            return BaseResultEntity.success(resultEntity.getResult());
        }catch (Exception e){
            log.info("元数据资源数据异常:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"请求元数据资源失败");
        }
    }

    public BaseResultEntity getDataResource(String resourceId) {
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        try{
            BaseResultEntity resultEntity = fusionResourceService.getDataResource(resourceId, sysLocalOrganInfo.getOrganId());
            return BaseResultEntity.success(resultEntity.getResult());
        }catch (Exception e){
            log.info("获取中心节点资源详情数据异常:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"请求元数据资源失败");
        }
    }

    public BaseResultEntity getResourceListById(List<String> resourceIds) {
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        try{
            return fusionResourceService.getResourceListById(resourceIds,sysLocalOrganInfo.getOrganId());
        }catch (Exception e){
            log.info("获取中心节点资源详情数据异常:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"请求元数据资源失败");
        }
    }


    public BaseResultEntity getResourceTagList() {
        try {
            return fusionResourceService.getResourceTagList();
        }catch (Exception e){
            log.info("获取中心节点资源标签数据异常:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"请求元数据资源失败");
        }
    }

    public BaseResultEntity syncResourceUse(String organId,String resourceId,String projectId,Integer auditStatus){
        try {
            return fusionResourceService.saveOrganResourceAuth(organId,resourceId,projectId,auditStatus);
        }catch (Exception e){
            log.info("获取中心节点资源标签数据异常:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"请求元数据资源失败");
        }
    }

    public Map<String, SysOrgan> getOrganListMap(List<String> organId){
        List<SysOrgan> sysOrgans = sysOrganSecondarydbRepository.selectSysOrganByExamine();
        Map<String, SysOrgan> map = sysOrgans.stream().collect(Collectors.toMap(SysOrgan::getOrganId, Function.identity()));
        SysOrgan o = new SysOrgan();
        o.setOrganId(organConfiguration.getSysLocalOrganId());
        o.setOrganName(organConfiguration.getSysLocalOrganName());
        o.setOrganGateway(organConfiguration.getSysLocalOrganInfo().getGatewayAddress());
        map.put(organConfiguration.getSysLocalOrganId(),o);
        return map;
    }

    public Map<String,Map> getResourceListMap(List<String> resourceIds){
        BaseResultEntity resultEntity = fusionResourceService.getResourceListById(resourceIds,organConfiguration.getSysLocalOrganId());
        Map<String,Map> resourceMap = new HashMap<>();
        if (resultEntity.getCode().equals(BaseResultEnum.SUCCESS.getReturnCode())) {
            Object result = resultEntity.getResult();
            if (result!=null){
                List<Map> list =(List<Map>) result;
                resourceMap = list.stream().collect(Collectors.toMap(map1 -> map1.get("resourceId").toString(), Function.identity()));
            }
        }
        return resourceMap;
    }

    public void syncGatewayApiData(Object vo,String gatewayAddressAndApi,String publicKey){
        try {
            Object data;
            if (StringUtils.isEmpty(publicKey)){
                gatewayAddressAndApi = gatewayAddressAndApi+"?ignore=ignore";
                data = vo;
            }else {
                data = CryptUtil.multipartEncrypt(JSONObject.toJSONString(vo), publicKey);
            }
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(data, headers);
            log.info(gatewayAddressAndApi);
            BaseResultEntity baseResultEntity = restTemplate.postForObject(gatewayAddressAndApi, request, BaseResultEntity.class);
            log.info("baseResultEntity {}",JSONObject.toJSONString(baseResultEntity));
        }catch (Exception e){
            log.info("gatewayAddress api url:{} Exception:{}",gatewayAddressAndApi,e.getMessage());
            e.printStackTrace();
        }
        log.info("gatewayAddress api url:{} end:{}",gatewayAddressAndApi,System.currentTimeMillis());
    }
}
