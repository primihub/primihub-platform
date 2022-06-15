package com.primihub.biz.service.sys;

import com.alibaba.fastjson.JSON;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.ConfigType;
import com.alibaba.nacos.api.exception.NacosException;
import com.primihub.biz.constant.SysConstant;
import com.primihub.biz.repository.primarydb.sys.SysOrganPrimarydbRepository;
import com.primihub.biz.repository.primaryredis.sys.SysCommonPrimaryRedisRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageParam;
import com.primihub.biz.entity.sys.param.AlterOrganNodeStatusParam;
import com.primihub.biz.entity.sys.param.ChangeLocalOrganInfoParam;
import com.primihub.biz.entity.sys.param.CreateOrganNodeParam;
import com.primihub.biz.entity.sys.param.FindOrganPageParam;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.entity.sys.po.SysOrganFusion;
import com.primihub.biz.entity.sys.vo.SysOrganListVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.BeanUtils;
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
import java.util.function.Function;
import java.util.stream.Collectors;

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
    private SysOrganPrimarydbRepository sysOrganPrimarydbRepository;
    @Autowired
    private SysOrganSecondarydbRepository sysOrganSecondarydbRepository;

    public BaseResultEntity findOrganPage(FindOrganPageParam findOrganPageParam, Integer pageNum,Integer pageSize){
        PageParam pageParam=new PageParam(pageNum,pageSize);
        Map paramMap=new HashMap(){
            {
                put("pOrganId",findOrganPageParam.getPOrganId());
                put("organName",findOrganPageParam.getOrganName());
                put("pageIndex",pageParam.getPageIndex());
                put("pageSize",pageParam.getPageSize()+1);
            }
        };
        List<SysOrganListVO> sysOrganList=sysOrganSecondarydbRepository.selectOrganListByParam(paramMap);
        Long count=sysOrganSecondarydbRepository.selectOrganListCountByParam(paramMap);
        pageParam.isLoadMore(sysOrganList);
        pageParam.initItemTotalCount(count);
        Map map=new HashMap<>();
        map.put("sysOrganList",sysOrganList);
        map.put("pageParam",pageParam);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity createOrganNode(CreateOrganNodeParam createOrganNodeParam){
        SysOrgan parentSysOrgan=createOrganNodeParam.getPOrganId()==0?null:sysOrganSecondarydbRepository.selectSysOrganByOrganId(createOrganNodeParam.getPOrganId());
        SysOrgan sysOrgan=new SysOrgan();
        BeanUtils.copyProperties(createOrganNodeParam,sysOrgan);
        if(sysOrgan.getPOrganId().equals(0L)){
            sysOrgan.setROrganId(0L);
            sysOrgan.setOrganDepth(0);
        }else{
            sysOrgan.setROrganId(parentSysOrgan.getROrganId());
            sysOrgan.setOrganDepth(parentSysOrgan.getOrganDepth()+1);
        }
        sysOrgan.setFullPath("");
        sysOrgan.setIsDel(0);
        sysOrganPrimarydbRepository.insertSysOrgan(sysOrgan);
        if(parentSysOrgan!=null) {
            sysOrgan.setFullPath(new StringBuilder().append(parentSysOrgan.getFullPath()).append(",").append(sysOrgan.getOrganId()).toString());
        }else{
            sysOrgan.setFullPath(sysOrgan.getOrganId().toString());
        }
        if(sysOrgan.getROrganId().equals(0L)) {
            sysOrganPrimarydbRepository.updateROrganIdAndFullPath(sysOrgan.getOrganId(),sysOrgan.getOrganId(),sysOrgan.getFullPath());
        }else{
            sysOrganPrimarydbRepository.updateROrganIdAndFullPath(sysOrgan.getOrganId(),null,sysOrgan.getFullPath());
        }
        Map map=new HashMap<>();
        map.put("sysOrgan",sysOrgan);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity alterOrganNodeStatus(AlterOrganNodeStatusParam alterOrganNodeStatusParam){
        Map paramMap=new HashMap(){
            {
                put("organId",alterOrganNodeStatusParam.getOrganId());
                put("organName",alterOrganNodeStatusParam.getOrganName());
            }
        };
        sysOrganPrimarydbRepository.updateSysOrganExplicit(paramMap);
        return BaseResultEntity.success();
    }

    public BaseResultEntity deleteOrganNode(@Param("organId") Long organId){
        sysOrganPrimarydbRepository.updateOrganDelStatus(organId);
        return BaseResultEntity.success();
    }

    public Map<Long,SysOrgan> getSysOrganMap(Set<Long> organIdSet){
        if (organIdSet==null||organIdSet.size()==0){
            return new HashMap<>();
        }
        List<SysOrgan> sysOrgans = this.sysOrganSecondarydbRepository.selectSysOrganByBatchOrganId(organIdSet);
        if (sysOrgans.size()>0){
            return sysOrgans.stream().collect(Collectors.toMap(SysOrgan::getOrganId, Function.identity(),(key1,key2)->key2));
        }
        return new HashMap<>();
    }

    public BaseResultEntity getLocalOrganInfo(){
        String group=environment.getProperty("nacos.config.group");
        String serverAddr=environment.getProperty("nacos.config.server-addr");
        ConfigService configService;
        try {
            configService= NacosFactory.createConfigService(serverAddr);
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
       if(!sysCommonPrimaryRedisRepository.lock(SysConstant.SYS_LOCAL_ORGAN_INFO_LOCK))
           return BaseResultEntity.failure(BaseResultEnum.HANDLE_RIGHT_NOW);
       String group=environment.getProperty("nacos.config.group");
       String serverAddr=environment.getProperty("nacos.config.server-addr");
       ConfigService configService;
       try {
           configService= NacosFactory.createConfigService(serverAddr);
           String organInfoContent=configService.getConfig(SysConstant.SYS_ORGAN_INFO_NAME,group,3000);
           SysLocalOrganInfo sysLocalOrganInfo=JSON.parseObject(organInfoContent,SysLocalOrganInfo.class);
           int flag=0;
           if(sysLocalOrganInfo==null)
               sysLocalOrganInfo=new SysLocalOrganInfo();
           if(sysLocalOrganInfo.getOrganId()==null||sysLocalOrganInfo.getOrganId().trim().equals("")){
               sysLocalOrganInfo.setOrganId(UUID.randomUUID().toString());
               flag|=1;
           }
           if(sysLocalOrganInfo.getPinCode()==null||sysLocalOrganInfo.getPinCode().trim().equals("")){
               sysLocalOrganInfo.setPinCode(RandomStringUtils.randomAlphanumeric(16));
               flag|=1;
           }
           if(changeLocalOrganInfoParam.getOrganName()!=null&&!changeLocalOrganInfoParam.getOrganName().trim().equals("")){
               sysLocalOrganInfo.setOrganName(changeLocalOrganInfoParam.getOrganName());
               flag|=1;
           }
           if(sysLocalOrganInfo!=null&&(sysLocalOrganInfo.getOrganName()==null||changeLocalOrganInfoParam.getOrganName().trim().equals(""))) {
               sysCommonPrimaryRedisRepository.unlock(SysConstant.SYS_LOCAL_ORGAN_INFO_LOCK);
               return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM, "缺少参数organName");
           }
           if(flag==1)
               configService.publishConfig(SysConstant.SYS_ORGAN_INFO_NAME,group,JSON.toJSONString(sysLocalOrganInfo),ConfigType.JSON.getType());
           if (sysLocalOrganInfo.getFusionMap()!=null&&sysLocalOrganInfo.getFusionMap().size()>0){
               String organId = sysLocalOrganInfo.getOrganId();
               String organName = sysLocalOrganInfo.getOrganName();
               HttpHeaders headers = new HttpHeaders();
               headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
               Iterator<Map.Entry<String, SysOrganFusion>> iterator = sysLocalOrganInfo.getFusionMap().entrySet().iterator();
               while(iterator.hasNext()){
                   SysOrganFusion sysOrganFusion = iterator.next().getValue();
                   try{
                       MultiValueMap map = new LinkedMultiValueMap<>();
                       map.put("globalId", new ArrayList(){{add(organId);}});
                       map.put("globalName", new ArrayList(){{add(organName);}});
                       HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
                       BaseResultEntity resultEntity=restTemplate.postForObject(sysOrganFusion.getServerAddress()+"/fusion/changeConnection",request, BaseResultEntity.class);
                       log.info("changeOrganInfo serverAddress:{} | param -- organId:{},globalName:{}  | result -- code:{},msg:{},result:{}",sysOrganFusion.getServerAddress(),organId,organName,resultEntity.getCode(),resultEntity.getMsg(),resultEntity.getResult());
                   }catch (Exception e){
                       log.info("changeOrganInfoException serverAddress:{} | param -- organId:{},globalName:{} | message:{}",sysOrganFusion.getServerAddress(),organId,organName,e.getMessage());
                   }
               }
           }

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

}
