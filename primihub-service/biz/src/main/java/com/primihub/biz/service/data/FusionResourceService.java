package com.primihub.biz.service.data;

import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.req.DataFResourceReq;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

@Slf4j
@Service
public class FusionResourceService {

    @Resource(name="soaRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private OrganConfiguration organConfiguration;


    public BaseResultEntity getResourceList(DataFResourceReq req) {
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        if (!sysLocalOrganInfo.getFusionMap().containsKey(req.getServerAddress()))
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"serverAddress");
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
            map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
            map.put("resourceId", new ArrayList(){{add(req.getResourceId());}});
            map.put("resourceName", new ArrayList(){{add(req.getResourceName());}});
            map.put("resourceType", new ArrayList(){{add(req.getResourceType());}});
            map.put("organId", new ArrayList(){{add(req.getOrganId());}});
            map.put("tagName", new ArrayList(){{add(req.getTagName());}});
            map.put("pageNo", new ArrayList(){{add(req.getPageNo());}});
            map.put("pageSize", new ArrayList(){{add(req.getPageSize());}});
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            BaseResultEntity resultEntity=restTemplate.postForObject(req.getServerAddress()+"/fusionResource/getResourceList",request, BaseResultEntity.class);
            return BaseResultEntity.success(resultEntity.getResult());
        }catch (Exception e){
            log.info("获取中心节点资源数据异常:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"请求中心节点失败");
        }
    }

    public BaseResultEntity getDataResource(String serverAddress,String resourceId) {
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        if (!sysLocalOrganInfo.getFusionMap().containsKey(serverAddress))
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"serverAddress");
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
            map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
            map.put("resourceId", new ArrayList(){{add(resourceId);}});
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            BaseResultEntity resultEntity=restTemplate.postForObject(serverAddress+"/fusionResource/getDataResource",request, BaseResultEntity.class);
            return BaseResultEntity.success(resultEntity.getResult());
        }catch (Exception e){
            log.info("获取中心节点资源详情数据异常:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"请求中心节点失败");
        }
    }

    public BaseResultEntity getResourceListById(String serverAddress,String[] resourceIds) {
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        if (!sysLocalOrganInfo.getFusionMap().containsKey(serverAddress))
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"serverAddress");
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
            map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
            map.put("resourceIdArray", Arrays.asList(resourceIds));
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            BaseResultEntity resultEntity=restTemplate.postForObject(serverAddress+"/fusionResource/getResourceListById",request, BaseResultEntity.class);
            return resultEntity;
        }catch (Exception e){
            log.info("获取中心节点资源详情数据异常:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"请求中心节点失败");
        }
    }


    public BaseResultEntity getResourceTagList(String serverAddress) {
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        if (!sysLocalOrganInfo.getFusionMap().containsKey(serverAddress))
            return BaseResultEntity.failure(BaseResultEnum.PARAM_INVALIDATION,"serverAddress");
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            MultiValueMap map = new LinkedMultiValueMap<>();
            map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
            map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            BaseResultEntity resultEntity=restTemplate.postForObject(serverAddress+"/fusionResource/getResourceTagList",request, BaseResultEntity.class);
            return BaseResultEntity.success(resultEntity.getResult());
        }catch (Exception e){
            log.info("获取中心节点资源标签数据异常:{}",e.getMessage());
            return BaseResultEntity.failure(BaseResultEnum.FAILURE,"请求中心节点失败");
        }
    }
}
