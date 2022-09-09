package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.constant.CommonConstant;
import com.primihub.biz.convert.DataModelConvert;
import com.primihub.biz.convert.DataProjectConvert;
import com.primihub.biz.entity.base.*;
import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectOrgan;
import com.primihub.biz.entity.data.po.DataProjectResource;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysUser;
import com.primihub.biz.repository.primarydb.data.DataProjectPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataModelRepository;
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
    private DataModelRepository dataModelRepository;
    @Autowired
    private DataModelService dataModelService;

    public BaseResultEntity saveOrUpdateProject(DataProjectReq req,Long userId) {
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        if (sysLocalOrganInfo==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"无机构信息");
        DataProject dataProject;
        if (req.getId()==null){
            SysUser sysUser = sysUserSecondarydbRepository.selectSysUserByUserId(userId);
            dataProject = DataProjectConvert.dataProjectReqConvertPo(req,sysLocalOrganInfo,sysUser.getUserName());
            if (StringUtils.isBlank(req.getProjectId()))
                dataProject.setProjectId(organConfiguration.generateUniqueCode());
            // Available by default
            dataProject.setStatus(1);
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
                            dataProject.setResourceNum(dataProject.getResourceNum()+1);
                        }
                    }
                }
            }
        }
        dataProjectPrRepository.updateDataProject(dataProject);
        sendTask(new ShareProjectVo(dataProject));
        Map<String,String> map = new HashMap<>();
        map.put("id", dataProject.getId().toString());
        map.put("projectId", dataProject.getProjectId());
        return BaseResultEntity.success(map);
    }

    public void sendTask(ShareProjectVo shareProjectVo){
        singleTaskChannel.input().send(MessageBuilder.withPayload(JSON.toJSONString(new BaseFunctionHandleEntity(BaseFunctionHandleEnum.SPREAD_PROJECT_DATA_TASK.getHandleType(),shareProjectVo))).build());
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
                        if(organMap.get("globalName")!=null){
                            if (!organNames.contains(organMap.get("globalName").toString())){
                                organNames.add(organMap.get("globalName").toString());
                            }
                        }
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
        Set<Long> projectIds = dataProjects.stream().map(DataProject::getId).collect(Collectors.toSet());
        List<Map<String, Object>> projectNumMapList = dataModelRepository.queryModelNumByProjectIds(projectIds);
        Map<Object, List<Map<String, Object>>> projectNumMap = projectNumMapList.stream().collect(Collectors.groupingBy(m -> m.get("projectId")));
        return BaseResultEntity.success(new PageDataEntity(count,req.getPageSize(),req.getPageNo(),dataProjects.stream().map(dp->DataProjectConvert.dataProjectConvertListVo(dp,projectNumMap.get(dp.getId()),req.getStatus())).collect(Collectors.toList())));
    }

    public BaseResultEntity getProjectDetails(Long id) {
        DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(id, null);
        if (dataProject==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"无项目信息");
        DataProjectDetailsVo dataProjectDetailsVo = DataProjectConvert.dataProjectConvertDetailsVo(dataProject);
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        if (sysLocalOrganInfo.getOrganId().equals(dataProject.getCreatedOrganId()))
            dataProjectDetailsVo.setCreator(true);
        List<DataProjectOrgan> dataProjectOrgans = dataProjectRepository.selectDataProjcetOrganByProjectId(dataProject.getProjectId()).stream().filter(organ -> dataProjectDetailsVo.getCreator() || organ.getOrganId().equals(organ.getInitiateOrganId()) || organ.getOrganId().equals(sysLocalOrganInfo.getOrganId())).collect(Collectors.toList());
        List<String> organIds = dataProjectOrgans.stream().map(DataProjectOrgan::getOrganId).collect(Collectors.toList());
        List<DataProjectResource> dataProjectResources = dataProjectRepository.selectProjectResourceByProjectId(dataProject.getProjectId());
        Map<String, List<DataProjectResource>> organResourceMap = dataProjectResources.stream().collect(Collectors.groupingBy(DataProjectResource::getOrganId));
        List<String> resourceIds = dataProjectResources.stream().map(DataProjectResource::getResourceId).collect(Collectors.toList());
        Map<String, Map> resourceListMap = getResourceListMap(resourceIds, dataProject.getServerAddress());
        Map<String, Map> organListMap = getOrganListMap(organIds, dataProject.getServerAddress());
        List<DataProjectOrganVo> organs = new ArrayList<>();
        for (DataProjectOrgan projectOrgan : dataProjectOrgans) {
            DataProjectOrganVo dataProjectOrganVo = DataProjectConvert.DataProjectOrganConvertVo(projectOrgan, dataProject.getCreatedOrganId().equals(projectOrgan.getOrganId()), sysLocalOrganInfo,organListMap.get(projectOrgan.getOrganId()));
            List<DataProjectResource> projectResources = organResourceMap.get(dataProjectOrganVo.getOrganId());
            if (projectResources!=null){
                for (DataProjectResource projectResource : projectResources) {
                    dataProjectOrganVo.getResources().add(DataProjectConvert.DataProjectResourceConvertVo(projectResource,resourceListMap.get(projectResource.getResourceId())));
                }
            }
            organs.add(dataProjectOrganVo);
        }
        organs = organs.stream().sorted(Comparator.comparing(DataProjectOrganVo::getCreator,Comparator.reverseOrder())).collect(Collectors.toList());
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
            // update Project status
            DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(null, dataProjectOrgan.getProjectId());
            dataProject.setStatus(1);
            dataProjectPrRepository.updateDataProject(dataProject);
            shareProjectVo.setProjectId(dataProjectOrgan.getProjectId());
            shareProjectVo.setServerAddress(dataProjectOrgan.getServerAddress());
//            shareProjectVo.getProjectOrgans().add(dataProjectOrgan);
        }else {
            DataProjectResource dataProjectResource = dataProjectRepository.selectProjectResourceById(req.getId());
            if (dataProjectResource==null)
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"无资源信息");
            if (!dataProjectResource.getOrganId().equals(organId))
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"非本机构无法审核");
            if (dataProjectResource.getAuditStatus()!=0)
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"不可以重复审核");
            DataProjectOrgan dataProjectOrgan = dataProjectRepository.selectDataProjcetOrganByProjectIdAndOrganId(dataProjectResource.getProjectId(), dataProjectResource.getOrganId());
            if (dataProjectOrgan==null)
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"无资源机构信息");
            if (dataProjectOrgan.getAuditStatus()!=1)
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"该资源机构审核中或已拒绝,无法进行资源审核");
            dataProjectResource.setAuditStatus(req.getAuditStatus());
            dataProjectResource.setAuditOpinion(req.getAuditOpinion());
            dataProjectPrRepository.updateDataProjectResource(dataProjectResource);
            shareProjectVo.setProjectId(dataProjectResource.getProjectId());
            shareProjectVo.setServerAddress(dataProjectResource.getServerAddress());
            shareProjectVo.getProjectResources().add(dataProjectResource);
        }
        sendTask(shareProjectVo);
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
        log.info(JSONObject.toJSONString(vo));
        if (StringUtils.isBlank(vo.getProjectId()))
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"projectId");
        String sysLocalOrganId = organConfiguration.getSysLocalOrganId();
        boolean isDelProject = vo.getProjectOrgans().stream().anyMatch(po -> po.getOrganId().equals(sysLocalOrganId) && po.getIsDel() != null && po.getIsDel() == 1);
        if (isDelProject){
            dataProjectPrRepository.deleteDataProjectResource(null,vo.getProjectId());
            dataProjectPrRepository.deleteDataProjectOrgan(null,vo.getProjectId());
            dataProjectPrRepository.deleteDataProject(null,vo.getProjectId());
        }else {
            DataProject project = vo.getProject();
            if (project!=null){
                DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(null, project.getProjectId());
                if (dataProject==null){
                    vo.getProject().setStatus(0);
                    dataProjectPrRepository.saveDataProject(vo.getProject());
                }else {
                    project.setId(dataProject.getId());
                    if(dataProject.getStatus()!=null&&dataProject.getStatus()!=2) {
                        dataProject.setStatus(null);
                    }
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
                        if (projectResource.getIsDel()!=null&&projectResource.getIsDel()==1){
                            dataProjectPrRepository.deleteDataProjectResource(dataProjectResource.getId(),null);
                        }else {
                            dataProjectPrRepository.updateDataProjectResource(projectResource);
                        }
                    }else {
                        projectResource.setPrId(UUID.randomUUID().toString());
                        dataProjectPrRepository.saveDataProjectResource(projectResource);
                    }
                }
            }
            DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(null, project.getProjectId());
            dataProject.setResourceNum(dataProjectRepository.selectProjectResourceByProjectId(vo.getProjectId()).size());
            dataProjectPrRepository.updateDataProject(dataProject);
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity getListStatistics() {
        Map<String,Integer> map = new HashMap<>();
        map.put("own",0);
        map.put("other",0);
        map.put("total",0);
        String sysLocalOrganId = organConfiguration.getSysLocalOrganId();
        if (StringUtils.isBlank(sysLocalOrganId))
            return BaseResultEntity.success(map);
        List<Map<String, Object>> projectStatics = dataProjectRepository.selectProjectStatics(sysLocalOrganId);
        if (projectStatics.size()==0)
            return BaseResultEntity.success(map);
        Integer total = 0;
        for (Map<String, Object> projectStatic : projectStatics) {
            Object amount = projectStatic.get("amount");
            if (amount != null) {
                Integer num = Integer.valueOf(amount.toString());
                total = total + num;
                map.put(projectStatic.get("staticsType").toString(), num);
            } else {
                map.put(projectStatic.get("staticsType").toString(), 0);
            }
        }
        map.put("total",total);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity removeResource(Long id) {
        DataProjectResource dataProjectResource = dataProjectRepository.selectProjectResourceById(id);
        if (dataProjectResource==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"无资源信息");
        }
        String sysLocalOrganId = organConfiguration.getSysLocalOrganId();
        if (sysLocalOrganId.equals(dataProjectResource.getInitiateOrganId())||sysLocalOrganId.equals(dataProjectResource.getOrganId())){
            dataProjectPrRepository.deleteDataProjectResource(id,null);
            ShareProjectVo vo = new ShareProjectVo(dataProjectResource.getProjectId(), dataProjectResource.getServerAddress());
            dataProjectResource.setIsDel(1);
            vo.getProjectResources().add(dataProjectResource);
            sendTask(vo);
        }else {
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"非创建机构和操作机构不能操作");
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity removeOrgan(Long id) {
        DataProjectOrgan dataProjectOrgan = dataProjectRepository.selectDataProjcetOrganById(id);
        if (dataProjectOrgan==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"无机构信息");
        String sysLocalOrganId = organConfiguration.getSysLocalOrganId();
        if (sysLocalOrganId.equals(dataProjectOrgan.getInitiateOrganId())||sysLocalOrganId.equals(dataProjectOrgan.getOrganId())){
            dataProjectPrRepository.deleteDataProjectOrgan(id,null);
            ShareProjectVo vo = new ShareProjectVo(dataProjectOrgan.getProjectId(), dataProjectOrgan.getServerAddress());
            dataProjectOrgan.setIsDel(1);
            vo.getProjectOrgans().add(dataProjectOrgan);
            List<DataProjectResource> dataProjectResources = dataProjectRepository.selectProjectResourceByProjectIdAndOrganId(dataProjectOrgan.getProjectId(), dataProjectOrgan.getOrganId());
            for (DataProjectResource dpr : dataProjectResources) {
                dataProjectPrRepository.deleteDataProjectResource(dpr.getId(),null);
                dpr.setIsDel(1);
                vo.getProjectResources().add(dpr);
            }
            sendTask(vo);
        }else {
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"非创建机构和操作机构不能操作");
        }
        return BaseResultEntity.success();
    }

    public BaseResultEntity closeProject(Long id) {
        DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(id, null);
        if (dataProject==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"无项目信息");
        if (dataProject.getStatus()==2)
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"项目已关闭,不可重复操作");
        dataProject.setStatus(2);
        dataProjectPrRepository.updateDataProject(dataProject);
        ShareProjectVo vo = new ShareProjectVo(dataProject.getProjectId(), dataProject.getServerAddress());
        vo.setProject(dataProject);
        sendTask(vo);
        return BaseResultEntity.success();
    }

    public BaseResultEntity getProjectResourceData(Long projectId,String organId) {
        DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(projectId,null);
        if (dataProject==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到项目信息");
        }
        DataProjectOrgan dataProjectOrgan = dataProjectRepository.selectDataProjcetOrganByProjectIdAndOrganId(dataProject.getProjectId(), organId);
        if (dataProjectOrgan==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到项目机构信息");
        }
        if (dataProjectOrgan.getAuditStatus()!=1){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"机构使用状态可不用");
        }
        List<DataProjectResource> dataProjectResources = dataProjectRepository.selectProjectResourceByProjectIdAndOrganId(dataProject.getProjectId(), organId);
        if (dataProjectResources.size()==0)
            return BaseResultEntity.success(new ArrayList());
        List<String> resourceIds = dataProjectResources.stream().map(DataProjectResource::getResourceId).collect(Collectors.toList());
        Map<String, Map> resourceMap = getResourceListMap(resourceIds, dataProject.getServerAddress());
        return BaseResultEntity.success(dataProjectResources.stream().map(resource->DataModelConvert.projectResourcePoCovertModelResourceVo(resource,resourceMap.get(resource.getResourceId()))).collect(Collectors.toList()));
    }

    public BaseResultEntity getProjectResourceOrgan(Long projectId,Long modelId) {
        DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(projectId,null);
        if (dataProject==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到项目信息");
        }
        List<DataProjectOrgan> dataProjectOrgans = dataProjectRepository.selectDataProjcetOrganByProjectId(dataProject.getProjectId());
        dataProjectOrgans = dataProjectOrgans.stream().filter(organ -> organ.getAuditStatus() == 1).collect(Collectors.toList());
        if (dataProjectOrgans.size()==0){
            BaseResultEntity.success(dataProjectOrgans);
        }
        List<String> organIds = dataProjectOrgans.stream().map(DataProjectOrgan::getOrganId).collect(Collectors.toList());
        Map<String, Map> organListMap = getOrganListMap(organIds, dataProject.getServerAddress());
        Map<String, ModelProjectResourceVo> resourceVoMap = new HashMap<>();
        if (modelId!=null&&modelId!=0L){
            DataModelAndComponentReq modelComponentReq = dataModelService.getModelComponentReq(modelId, null,null);
            if (modelComponentReq!=null&&modelComponentReq.getModelComponents()!=null&&modelComponentReq.getModelComponents().size()!=0){
                Map<String, String> componentValMap = dataModelService.getDataAlignmentComponentVals(modelComponentReq.getModelComponents());
                String selectData = componentValMap.get("selectData");
                if(StringUtils.isNotBlank(selectData)){
                    List<ModelProjectResourceVo> modelProjectResourceVos = JSONObject.parseArray(selectData, ModelProjectResourceVo.class);
                    if (modelProjectResourceVos!=null&&modelProjectResourceVos.size()!=0){
                        for (ModelProjectResourceVo vo : modelProjectResourceVos) {
                            resourceVoMap.put(vo.getOrganId(),vo);
                        }
                    }
                }
            }
        }
        String sysLocalOrganId = organConfiguration.getSysLocalOrganId();
        return BaseResultEntity.success(dataProjectOrgans.stream().map(organ -> DataModelConvert.projectOrganPoCovertProjectOrganVo(organ,organListMap.get(organ.getOrganId()),resourceVoMap.get(organ.getOrganId()),sysLocalOrganId)));
    }

    public BaseResultEntity openProject(Long id) {
        DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(id, null);
        if (dataProject==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"无项目信息");
        if (dataProject.getStatus()!=2)
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"项目非关闭状态,不可操作");
        dataProject.setStatus(1);
        dataProjectPrRepository.updateDataProject(dataProject);
        ShareProjectVo vo = new ShareProjectVo(dataProject.getProjectId(), dataProject.getServerAddress());
        vo.setProject(dataProject);
        sendTask(vo);
        return BaseResultEntity.success();
    }


    public BaseResultEntity getResourceList(String organId) {
        return BaseResultEntity.success();
    }
}
