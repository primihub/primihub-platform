package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSON;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.constant.CommonConstant;
import com.primihub.biz.convert.DataProjectConvert;
import com.primihub.biz.entity.base.*;
import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectOrgan;
import com.primihub.biz.entity.data.po.DataProjectResource;
import com.primihub.biz.entity.data.req.DataProjectApprovalReq;
import com.primihub.biz.entity.data.req.DataProjectOrganReq;
import com.primihub.biz.entity.data.req.DataProjectQueryReq;
import com.primihub.biz.entity.data.req.DataProjectReq;
import com.primihub.biz.entity.data.vo.DataProjectDetailsVo;
import com.primihub.biz.entity.data.vo.DataProjectOrganVo;
import com.primihub.biz.entity.data.vo.ShareProjectVo;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysUser;
import com.primihub.biz.repository.primarydb.data.DataProjectPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.repository.secondarydb.sys.SysUserSecondarydbRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataProjectService {
    @Autowired
    private DataProjectRepository dataProjectRepository;
    @Autowired
    private DataProjectPrRepository dataProjectPrRepository;
    @Autowired
    private SysUserSecondarydbRepository sysUserSecondarydbRepository;
    @Resource(name="soaRestTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private SingleTaskChannel singleTaskChannel;
    @Autowired
    private OrganConfiguration organConfiguration;
    @Autowired
    private DataTaskService dataTaskService;

    public BaseResultEntity saveOrUpdateProject(DataProjectReq req,Long userId) {
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        if (sysLocalOrganInfo==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"无机构信息");
        ShareProjectVo shareProjectVo = new ShareProjectVo();
        DataProject dataProject;
        if (req.getId()==null){
            SysUser sysUser = sysUserSecondarydbRepository.selectSysUserByUserId(userId);
            dataProject = DataProjectConvert.dataProjectReqConvertPo(req,sysLocalOrganInfo,sysUser.getUserName());
            if (StringUtils.isBlank(req.getProjectId()))
                dataProject.setProjectId(organConfiguration.generateUniqueCode());
            updateProjectProviderOrganName(req.getProjectOrgans(),dataProject);
            dataProjectPrRepository.saveDataProject(dataProject);
            req.setProjectId(dataProject.getProjectId());
        }else {
            dataProject = dataProjectRepository.selectDataProjectByProjectId(req.getId(), null);
            if (dataProject==null)
                return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"无项目信息");
            if (StringUtils.isNotBlank(req.getProjectName())&&!dataProject.getProjectName().equals(req.getProjectName())){
                dataProject.setProjectName(req.getProjectName());
            }
            if (StringUtils.isNotBlank(req.getProjectDesc())&&!dataProject.getProjectDesc().equals(req.getProjectDesc())){
                dataProject.setProjectDesc(req.getProjectDesc());
            }
            req.setProjectId(dataProject.getProjectId());
            req.setServerAddress(dataProject.getServerAddress());
            updateProjectProviderOrganName(req.getProjectOrgans(),dataProject);
        }
        if (req.getProjectOrgans()!=null){
            List<DataProjectOrgan> dataProjectOrgans = dataProjectRepository.selectDataProjcetOrganByProjectId(req.getProjectId());
            Map<String, DataProjectOrgan> organMap = dataProjectOrgans.stream().collect(Collectors.toMap(DataProjectOrgan::getOrganId, Function.identity()));
            for (DataProjectOrganReq projectOrgan : req.getProjectOrgans()) {
                DataProjectOrgan dataProjectOrgan = organMap.get(projectOrgan.getOrganId());
                if (dataProjectOrgan !=null&&projectOrgan.getResourceIds()==null)
                    return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"添加发起者或协作者重复");
                if (dataProjectOrgan ==null){
                    dataProjectOrgan = new DataProjectOrgan(UUID.randomUUID().toString(),req.getProjectId(),projectOrgan.getOrganId(),sysLocalOrganInfo.getOrganId(),projectOrgan.getParticipationIdentity(),req.getServerAddress());
                    if (projectOrgan.getOrganId().equals(sysLocalOrganInfo.getOrganId())){
                        dataProjectOrgan.setAuditStatus(1);
                        dataProjectOrgan.setAuditOpinion("项目发起者自动同意");
                    }
                    dataProjectPrRepository.saveDataProjcetOrgan(dataProjectOrgan);
                    shareProjectVo.getProjectOrgans().add(dataProjectOrgan);
                }
                Set<String> existenceResourceIds = dataProjectRepository.selectProjectResourceByProjectId(req.getProjectId()).stream().map(DataProjectResource::getResourceId).collect(Collectors.toSet());
                List<String> resourceIds = projectOrgan.getResourceIds();
                if (resourceIds!=null&&!resourceIds.isEmpty()){
                    for (String resourceId : resourceIds) {
                        if (!existenceResourceIds.contains(resourceId)){
                            DataProjectResource dataProjectResource = new DataProjectResource(UUID.randomUUID().toString(), dataProjectOrgan.getProjectId(), sysLocalOrganInfo.getOrganId(), dataProjectOrgan.getOrganId(), dataProjectOrgan.getParticipationIdentity(), req.getServerAddress());
                            dataProjectResource.setResourceId(resourceId);
                            if (projectOrgan.getOrganId().equals(sysLocalOrganInfo.getOrganId())){
                                dataProjectResource.setAuditStatus(1);
                                dataProjectResource.setAuditOpinion("项目发起者自动同意");
                            }
                            dataProjectPrRepository.saveDataProjectResource(dataProjectResource);
                            shareProjectVo.getProjectResources().add(dataProjectResource);
                            dataProject.setResourceNum(dataProject.getResourceNum()+1);
                        }
                    }
                }
            }
        }
        dataProjectPrRepository.updateDataProject(dataProject);
        shareProjectVo.setServerAddress(dataProject.getServerAddress());
        shareProjectVo.setProject(dataProject);
        shareProjectVo.setProjectId(dataProject.getProjectId());
        singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SPREAD_PROJECT_DATA_TASK.getHandleType(),shareProjectVo))).build());
//        dataTaskService.spreadProjectData(JSON.toJSONString(shareProjectVo));
        Map<String,String> map = new HashMap<>();
        map.put("id", dataProject.getId().toString());
        map.put("projectId", dataProject.getProjectId());
        return BaseResultEntity.success(map);
    }

    public Boolean updateProjectProviderOrganName(List<DataProjectOrganReq> organList,DataProject dataProject){
        if (organList==null || organList.isEmpty())
            return false;
        List<String> organNames = new ArrayList<>();
        if (StringUtils.isNotBlank(dataProject.getProviderOrganNames())){
            organNames.addAll(Arrays.asList(dataProject.getProviderOrganNames().split(",")));
        }
        if (organNames.size()<3){
            String localOrganId = organConfiguration.getSysLocalOrganId();
            List<String> organIds = organList.stream().map(DataProjectOrganReq::getOrganId).collect(Collectors.toList());
            organIds.remove(localOrganId);
            if (!organIds.isEmpty()){
                Map<String, Map> organListMap = getOrganListMap(organIds, dataProject.getServerAddress());
                for (String organId : organIds) {
                    Map organMap = organListMap.get(organId);
                    if (organMap!=null){
                        if(organMap.get("globalName")!=null)
                            organNames.add(organMap.get("globalName").toString());
                    }
                }
                dataProject.setProviderOrganNames(StringUtils.join(organNames,","));
            }
        }
        return true;
    }

    public BaseResultEntity getProjectList(DataProjectQueryReq req) {
        req.setOwnOrganId(organConfiguration.getSysLocalOrganId());
        List<DataProject> dataProjects = dataProjectRepository.selectDataProjectPage(req);
        if (dataProjects.isEmpty())
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        Integer count = dataProjectRepository.selectDataProjectCount(req);
        return BaseResultEntity.success(new PageDataEntity(count,req.getPageSize(),req.getPageNo(),dataProjects.stream().map(dp->DataProjectConvert.dataProjectConvertListVo(dp)).collect(Collectors.toList())));
    }

    public BaseResultEntity getProjectDetails(Long id) {
        DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(id, null);
        if (dataProject==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"无项目信息");
        DataProjectDetailsVo dataProjectDetailsVo = DataProjectConvert.dataProjectConvertDetailsVo(dataProject);
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        if (sysLocalOrganInfo.getOrganId().equals(dataProject.getCreatedOrganId()))
            dataProjectDetailsVo.setCreator(true);
        List<DataProjectOrgan> dataProjectOrgans = dataProjectRepository.selectDataProjcetOrganByProjectId(dataProject.getProjectId());
        List<String> organIds = dataProjectOrgans.stream().map(DataProjectOrgan::getOrganId).collect(Collectors.toList());
        List<DataProjectResource> dataProjectResources = dataProjectRepository.selectProjectResourceByProjectId(dataProject.getProjectId());
        Map<String, List<DataProjectResource>> organResourceMap = dataProjectResources.stream().collect(Collectors.groupingBy(DataProjectResource::getOrganId));
        List<String> resourceIds = dataProjectResources.stream().map(DataProjectResource::getResourceId).collect(Collectors.toList());
        Map<String, Map> resourceListMap = getResourceListMap(resourceIds, dataProject.getServerAddress());
        Map<String, Map> organListMap = getOrganListMap(organIds, dataProject.getServerAddress());
        List<DataProjectOrganVo> organs = new ArrayList<>();
        for (DataProjectOrgan projectOrgan : dataProjectOrgans) {
            DataProjectOrganVo dataProjectOrganVo = DataProjectConvert.DataProjectOrganConvertVo(projectOrgan, sysLocalOrganInfo.getOrganId().equals(projectOrgan.getOrganId()), sysLocalOrganInfo,organListMap.get(projectOrgan.getOrganId()));
            List<DataProjectResource> projectResources = organResourceMap.get(dataProjectOrganVo.getOrganId());
            if (projectResources!=null){
                for (DataProjectResource projectResource : projectResources) {
                    dataProjectOrganVo.getResources().add(DataProjectConvert.DataProjectResourceConvertVo(projectResource,resourceListMap.get(projectResource.getResourceId())));
                }
            }
            organs.add(dataProjectOrganVo);
        }
        dataProjectDetailsVo.setOrgans(organs);
        return BaseResultEntity.success(dataProjectDetailsVo);
    }

    public BaseResultEntity approval(DataProjectApprovalReq req) {
        String organId = organConfiguration.getSysLocalOrganId();
        ShareProjectVo shareProjectVo = new ShareProjectVo();
        if (req.getType()==1){
            DataProjectOrgan dataProjectOrgan = dataProjectRepository.selectDataProjcetOrganById(req.getId());
            if (dataProjectOrgan==null)
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"无机构信息");
            if (!dataProjectOrgan.getOrganId().equals(organId))
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"非本机构无法审核");
            if (dataProjectOrgan.getAuditStatus()!=0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"不可以重复审核");
            dataProjectOrgan.setAuditStatus(req.getAuditStatus());
            dataProjectOrgan.setAuditOpinion(req.getAuditOpinion());
            dataProjectPrRepository.updateDataProjcetOrgan(dataProjectOrgan);
            shareProjectVo.setProjectId(dataProjectOrgan.getProjectId());
            shareProjectVo.setServerAddress(dataProjectOrgan.getServerAddress());
            shareProjectVo.getProjectOrgans().add(dataProjectOrgan);
        }else {
            DataProjectResource dataProjectResource = dataProjectRepository.selectProjectResourceById(req.getId());
            if (dataProjectResource==null)
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"无资源信息");
            if (!dataProjectResource.getOrganId().equals(organId))
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"非本机构无法审核");
            if (dataProjectResource.getAuditStatus()!=0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"不可以重复审核");
            dataProjectResource.setAuditStatus(req.getAuditStatus());
            dataProjectResource.setAuditOpinion(req.getAuditOpinion());
            dataProjectPrRepository.updateDataProjectResource(dataProjectResource);
            shareProjectVo.setProjectId(dataProjectResource.getProjectId());
            shareProjectVo.setServerAddress(dataProjectResource.getServerAddress());
            shareProjectVo.getProjectResources().add(dataProjectResource);
        }
        singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SPREAD_PROJECT_DATA_TASK.getHandleType(),shareProjectVo))).build());
        return BaseResultEntity.success();
    }

    public Map<String,Map> getResourceListMap(List<String> resourceIds,String serverAddress){
        MultiValueMap map = new LinkedMultiValueMap<>();
        map.put("resourceIdArray", resourceIds);
        BaseResultEntity resultEntity = restRequest(CommonConstant.FUSION_RESOURCE_LIST_BY_ID_URL.replace("<address>", serverAddress), map);
        Map<String,Map> resourceMap = new HashMap<>();
        if (resultEntity.getCode() == BaseResultEnum.SUCCESS.getReturnCode()) {
            Object result = resultEntity.getResult();
            if (result!=null){
                List<Map> list =(List<Map>) result;
                resourceMap = list.stream().collect(Collectors.toMap(map1 -> map1.get("resourceId").toString(), Function.identity()));
            }
        }
        return resourceMap;
    }

    public Map<String,Map> getOrganListMap(List<String> organId,String serverAddress){
        MultiValueMap map = new LinkedMultiValueMap<>();
        map.put("globalIdArray", organId);
        BaseResultEntity resultEntity = restRequest(CommonConstant.FUSION_ORGAN_BY_GLOBAL_ID_URL.replace("<address>", serverAddress), map);
        Map<String,Map> organMap = new HashMap<>();
        if (resultEntity.getCode() == BaseResultEnum.SUCCESS.getReturnCode()) {
            Map<String,Object> result = (Map<String, Object>) resultEntity.getResult();
            if (result!=null && result.size()!=0){
                List<Map> list =(List<Map>) result.get("organList");
                organMap = list.stream().collect(Collectors.toMap(map1 -> map1.get("globalId").toString(), Function.identity()));
            }
        }
        return organMap;
    }

    public BaseResultEntity restRequest(String url, MultiValueMap map){
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        try{
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            map.put("globalId", new ArrayList(){{add(sysLocalOrganInfo.getOrganId());}});
            map.put("pinCode", new ArrayList(){{add(sysLocalOrganInfo.getPinCode());}});
            HttpEntity<HashMap<String, Object>> request = new HttpEntity(map, headers);
            return restTemplate.postForObject(url,request, BaseResultEntity.class);
        }catch (Exception e){
            log.info("restRequest url:{},Exception:{}",url,e.getMessage());
        }
        return null;
    }

    public BaseResultEntity syncProject(ShareProjectVo vo) {
        if (StringUtils.isBlank(vo.getProjectId()))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"projectId");
        DataProject project = vo.getProject();
        if (project!=null){
            DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(null, project.getProjectId());
            if (dataProject==null){
                dataProjectPrRepository.saveDataProject(vo.getProject());
            }else {
                project.setId(dataProject.getId());
                dataProjectPrRepository.updateDataProject(vo.getProject());
            }
        }
        if (vo.getProjectOrgans()!=null&&vo.getProjectOrgans().size()!=0){
            Map<String, DataProjectOrgan> projectOrganMap = dataProjectRepository.selectDataProjcetOrganByProjectId(vo.getProjectId()).stream().collect(Collectors.toMap(DataProjectOrgan::getOrganId, Function.identity()));
            for (DataProjectOrgan projectOrgan : vo.getProjectOrgans()) {
                DataProjectOrgan dataProjectOrgan = projectOrganMap.get(projectOrgan.getOrganId());
                if (dataProjectOrgan!=null){
                    projectOrgan.setId(dataProjectOrgan.getId());
                    dataProjectPrRepository.updateDataProjcetOrgan(projectOrgan);
                }else {
                    projectOrgan.setPoId(UUID.randomUUID().toString());
                    dataProjectPrRepository.saveDataProjcetOrgan(projectOrgan);
                }
            }
        }

        if (vo.getProjectResources()!=null&&vo.getProjectResources().size()!=0){
            Map<String, DataProjectResource> projectResourceMap = dataProjectRepository.selectProjectResourceByProjectId(vo.getProjectId()).stream().collect(Collectors.toMap(DataProjectResource::getResourceId, Function.identity()));
            for (DataProjectResource projectResource : vo.getProjectResources()) {
                DataProjectResource dataProjectResource = projectResourceMap.get(projectResource.getResourceId());
                if (dataProjectResource!=null){
                    projectResource.setId(dataProjectResource.getId());
                    dataProjectPrRepository.updateDataProjectResource(projectResource);
                }else {
                    projectResource.setPrId(UUID.randomUUID().toString());
                    dataProjectPrRepository.saveDataProjectResource(projectResource);
                }
            }
        }
        return BaseResultEntity.success();
    }
}
