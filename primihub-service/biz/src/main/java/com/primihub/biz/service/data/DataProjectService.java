package com.primihub.biz.service.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.config.mq.SingleTaskChannel;
import com.primihub.biz.constant.CommonConstant;
import com.primihub.biz.convert.DataModelConvert;
import com.primihub.biz.convert.DataProjectConvert;
import com.primihub.biz.convert.DataResourceConvert;
import com.primihub.biz.entity.base.*;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import com.primihub.biz.entity.sys.po.SysUser;
import com.primihub.biz.repository.primarydb.data.DataProjectPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataModelRepository;
import com.primihub.biz.repository.secondarydb.data.DataProjectRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.sys.SysUserSecondarydbRepository;
import com.primihub.biz.service.sys.SysUserService;
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
    @Autowired
    private OtherBusinessesService otherBusinessesService;
    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private SysUserService sysUserService;


    public BaseResultEntity saveOrUpdateProject(DataProjectReq req,Long userId) {
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        if (sysLocalOrganInfo==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"无机构信息");
        }
        DataProject dataProject;
        if (req.getId()==null){
            SysUser sysUser = sysUserSecondarydbRepository.selectSysUserByUserId(userId);
            dataProject = DataProjectConvert.dataProjectReqConvertPo(req,sysLocalOrganInfo,sysUser.getUserName());
            if (StringUtils.isBlank(req.getProjectId())) {
                dataProject.setProjectId(organConfiguration.generateUniqueCode());
            }
            // Available by default
            dataProject.setStatus(1);
            updateProjectProviderOrganName(req.getProjectOrgans(),dataProject);
            dataProjectPrRepository.saveDataProject(dataProject);
            req.setProjectId(dataProject.getProjectId());
        }else {
            dataProject = dataProjectRepository.selectDataProjectByProjectId(req.getId(), null);
            if (dataProject==null) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"无项目信息");
            }
            if (StringUtils.isNotBlank(req.getProjectName())&&!dataProject.getProjectName().equals(req.getProjectName())){
                dataProject.setProjectName(req.getProjectName());
            }
            if (StringUtils.isNotBlank(req.getProjectDesc())&&!dataProject.getProjectDesc().equals(req.getProjectDesc())){
                dataProject.setProjectDesc(req.getProjectDesc());
            }
            req.setProjectId(dataProject.getProjectId());
            updateProjectProviderOrganName(req.getProjectOrgans(),dataProject);
        }
        if (req.getProjectOrgans()!=null){
            List<DataProjectOrgan> dataProjectOrgans = dataProjectRepository.selectDataProjcetOrganByProjectId(req.getProjectId());
            Map<String, DataProjectOrgan> organMap = dataProjectOrgans.stream().collect(Collectors.toMap(DataProjectOrgan::getOrganId, Function.identity()));
            for (DataProjectOrganReq projectOrgan : req.getProjectOrgans()) {
                DataProjectOrgan dataProjectOrgan = organMap.get(projectOrgan.getOrganId());
                if (dataProjectOrgan !=null&&projectOrgan.getResourceIds()==null) {
                    return BaseResultEntity.failure(BaseResultEnum.DATA_SAVE_FAIL,"添加发起者或协作者重复");
                }
                if (dataProjectOrgan ==null){
                    dataProjectOrgan = new DataProjectOrgan(UUID.randomUUID().toString(),req.getProjectId(),projectOrgan.getOrganId(),sysLocalOrganInfo.getOrganId(),projectOrgan.getParticipationIdentity());
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
                            DataProjectResource dataProjectResource = new DataProjectResource(UUID.randomUUID().toString(), dataProjectOrgan.getProjectId(), sysLocalOrganInfo.getOrganId(), dataProjectOrgan.getOrganId(), dataProjectOrgan.getParticipationIdentity());
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
        if (organList==null || organList.isEmpty()) {
            return false;
        }
        Set<String> organNames = new HashSet<>();
        if (StringUtils.isNotBlank(dataProject.getProviderOrganNames())){
            organNames.addAll(Arrays.asList(dataProject.getProviderOrganNames().split(",")));
        }
        String localOrganId = organConfiguration.getSysLocalOrganId();
        List<String> organIds = organList.stream().map(DataProjectOrganReq::getOrganId).collect(Collectors.toList());
        organIds.remove(localOrganId);
        if (!organIds.isEmpty()){
            Map<String, SysOrgan> organListMap = otherBusinessesService.getOrganListMap(organIds);
            for (String organId : organIds) {
                SysOrgan organ = organListMap.get(organId);
                if (organ!=null){
                    if (!organNames.contains(organ.getOrganName())){
                        organNames.add(organ.getOrganName());
                    }
                }
            }
            dataProject.setProviderOrganNames(StringUtils.join(organNames,","));
        }
        return true;
    }

    public BaseResultEntity getProjectList(DataProjectQueryReq req) {
        req.setOwnOrganId(organConfiguration.getSysLocalOrganId());
        List<DataProject> dataProjects = dataProjectRepository.selectDataProjectPage(req);
        if (dataProjects.isEmpty()) {
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Integer count = dataProjectRepository.selectDataProjectCount(req);
        Set<Long> projectIds = dataProjects.stream().map(DataProject::getId).collect(Collectors.toSet());
        List<Map<String, Object>> projectNumMapList = dataModelRepository.queryModelNumByProjectIds(projectIds);
        Map<Object, List<Map<String, Object>>> projectNumMap = projectNumMapList.stream().collect(Collectors.groupingBy(m -> m.get("projectId")));
        return BaseResultEntity.success(new PageDataEntity(count,req.getPageSize(),req.getPageNo(),dataProjects.stream().map(dp->DataProjectConvert.dataProjectConvertListVo(dp,projectNumMap.get(dp.getId()),req.getStatus())).collect(Collectors.toList())));
    }

    public BaseResultEntity getProjectDetails(Long id) {
        DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(id, null);
        if (dataProject==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"无项目信息");
        }
        DataProjectDetailsVo dataProjectDetailsVo = DataProjectConvert.dataProjectConvertDetailsVo(dataProject);
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        if (sysLocalOrganInfo.getOrganId().equals(dataProject.getCreatedOrganId())) {
            dataProjectDetailsVo.setCreator(true);
        }
        List<DataProjectOrgan> dataProjectOrgans = dataProjectRepository.selectDataProjcetOrganByProjectId(dataProject.getProjectId()).stream().filter(organ -> dataProjectDetailsVo.getCreator() || organ.getOrganId().equals(organ.getInitiateOrganId()) || organ.getOrganId().equals(sysLocalOrganInfo.getOrganId())).collect(Collectors.toList());
        List<String> organIds = dataProjectOrgans.stream().map(DataProjectOrgan::getOrganId).collect(Collectors.toList());
        List<DataProjectResource> dataProjectResources = dataProjectRepository.selectProjectResourceByProjectId(dataProject.getProjectId());
        Map<String, List<DataProjectResource>> organResourceMap = dataProjectResources.stream().collect(Collectors.groupingBy(DataProjectResource::getOrganId));
        List<String> resourceIds = dataProjectResources.stream().map(DataProjectResource::getResourceId).collect(Collectors.toList());
        Map<String, Map> resourceListMap = otherBusinessesService.getResourceListMap(resourceIds);
        Map<String, SysOrgan> organListMap = otherBusinessesService.getOrganListMap(organIds);
        List<DataProjectOrganVo> organs = new ArrayList<>();
        for (DataProjectOrgan projectOrgan : dataProjectOrgans) {
            DataProjectOrganVo dataProjectOrganVo = DataProjectConvert.DataProjectOrganConvertVo(projectOrgan, dataProject.getCreatedOrganId().equals(projectOrgan.getOrganId()), sysLocalOrganInfo,organListMap.get(projectOrgan.getOrganId()));
            List<DataProjectResource> projectResources = organResourceMap.get(dataProjectOrganVo.getOrganId());
            if (projectResources!=null){
                projectResources = projectResources.stream().sorted(Comparator.comparing(DataProjectResource::getId).reversed()).collect(Collectors.toList());
                for (DataProjectResource projectResource : projectResources) {
                    Map map = resourceListMap.get(projectResource.getResourceId());
//                    if (Integer.valueOf(map.get("available").toString()).equals("0"))
                    dataProjectOrganVo.getResources().add(DataProjectConvert.DataProjectResourceConvertVo(projectResource,map));
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
            if (dataProjectOrgan==null) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"无机构信息");
            }
            if (!dataProjectOrgan.getOrganId().equals(organId)) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"非本机构无法审核");
            }
            if (dataProjectOrgan.getAuditStatus()!=0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"不可以重复审核");
            }
            dataProjectOrgan.setAuditStatus(req.getAuditStatus());
            dataProjectOrgan.setAuditOpinion(req.getAuditOpinion());
            dataProjectPrRepository.updateDataProjcetOrgan(dataProjectOrgan);
            // update Project status
            DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(null, dataProjectOrgan.getProjectId());
            dataProject.setStatus(1);
            dataProjectPrRepository.updateDataProject(dataProject);
            shareProjectVo.setProjectId(dataProjectOrgan.getProjectId());
//            shareProjectVo.getProjectOrgans().add(dataProjectOrgan);
        }else {
            DataProjectResource dataProjectResource = dataProjectRepository.selectProjectResourceById(req.getId());
            if (dataProjectResource==null) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"无资源信息");
            }
            if (!dataProjectResource.getOrganId().equals(organId)) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"非本机构无法审核");
            }
            if (dataProjectResource.getAuditStatus()!=0) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"不可以重复审核");
            }
            DataProjectOrgan dataProjectOrgan = dataProjectRepository.selectDataProjcetOrganByProjectIdAndOrganId(dataProjectResource.getProjectId(), dataProjectResource.getOrganId());
            if (dataProjectOrgan==null) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"无资源机构信息");
            }
            if (dataProjectOrgan.getAuditStatus()!=1) {
                return BaseResultEntity.failure(BaseResultEnum.DATA_APPROVAL,"该资源机构审核中或已拒绝,无法进行资源审核");
            }
            dataProjectResource.setAuditStatus(req.getAuditStatus());
            dataProjectResource.setAuditOpinion(req.getAuditOpinion());
            dataProjectPrRepository.updateDataProjectResource(dataProjectResource);
            shareProjectVo.setProjectId(dataProjectResource.getProjectId());
            shareProjectVo.getProjectResources().add(dataProjectResource);
            log.info("发送");
            otherBusinessesService.syncResourceUse(dataProjectResource.getOrganId(),dataProjectResource.getResourceId(),dataProjectResource.getProjectId(),dataProjectResource.getAuditStatus());
        }
        sendTask(shareProjectVo);
        return BaseResultEntity.success();
    }

    public BaseResultEntity syncProject(ShareProjectVo vo) {
        log.info(JSONObject.toJSONString(vo));
        if (StringUtils.isBlank(vo.getProjectId())) {
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"projectId");
        }
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
            DataProject dataProject = dataProjectRepository.selectDataProjectByProjectId(null, vo.getProjectId());
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
        if (StringUtils.isBlank(sysLocalOrganId)) {
            return BaseResultEntity.success(map);
        }
        List<Map<String, Object>> projectStatics = dataProjectRepository.selectProjectStatics(sysLocalOrganId);
        if (projectStatics.size()==0) {
            return BaseResultEntity.success(map);
        }
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
            ShareProjectVo vo = new ShareProjectVo(dataProjectResource.getProjectId());
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
        if (dataProjectOrgan==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"无机构信息");
        }
        String sysLocalOrganId = organConfiguration.getSysLocalOrganId();
        if (sysLocalOrganId.equals(dataProjectOrgan.getInitiateOrganId())||sysLocalOrganId.equals(dataProjectOrgan.getOrganId())){
            dataProjectPrRepository.deleteDataProjectOrgan(id,null);
            ShareProjectVo vo = new ShareProjectVo(dataProjectOrgan.getProjectId());
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
        if (dataProject==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"无项目信息");
        }
        if (dataProject.getStatus()==2) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"项目已关闭,不可重复操作");
        }
        dataProject.setStatus(2);
        dataProjectPrRepository.updateDataProject(dataProject);
        ShareProjectVo vo = new ShareProjectVo(dataProject.getProjectId());
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
        if (dataProjectResources.size()==0) {
            return BaseResultEntity.success(new ArrayList());
        }
        List<String> resourceIds = dataProjectResources.stream().map(DataProjectResource::getResourceId).collect(Collectors.toList());
        Map<String, Map> resourceMap = otherBusinessesService.getResourceListMap(resourceIds);
        List<ModelProjectResourceVo> list = new ArrayList<>();
        for (DataProjectResource dataProjectResource : dataProjectResources) {
            Map map = resourceMap.get(dataProjectResource.getResourceId());
            if (map!=null && map.containsKey("available") && "0".equals(map.get("available").toString())){
                list.add(DataModelConvert.projectResourcePoCovertModelResourceVo(dataProjectResource, map));
            }
        }
        return BaseResultEntity.success(list);
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
        Map<String, SysOrgan> organListMap = otherBusinessesService.getOrganListMap(organIds);
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
        if (dataProject==null) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"无项目信息");
        }
        if (dataProject.getStatus()!=2) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_EDIT_FAIL,"项目非关闭状态,不可操作");
        }
        dataProject.setStatus(1);
        dataProjectPrRepository.updateDataProject(dataProject);
        ShareProjectVo vo = new ShareProjectVo(dataProject.getProjectId());
        vo.setProject(dataProject);
        sendTask(vo);
        return BaseResultEntity.success();
    }


    public BaseResultEntity getResourceList(OrganResourceReq req) {
        if (req.getAuditStatus() == null || req.getAuditStatus() == 0) {
            req.setAuditStatus(1);
        }
        List<ModelResourceVo> modelResourceVos = dataModelRepository.queryModelResource(req.getModelId(), null);
        if (modelResourceVos.isEmpty()) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"模型无资源信息");
        }
        List<DataResource> dataResourcesList = dataResourceRepository.queryDataResourceByResourceIds(null, modelResourceVos.stream().map(ModelResourceVo::getResourceId).collect(Collectors.toSet()));
        if (dataResourcesList.isEmpty()) {
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"无资源信息");
        }
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("organId",req.getOrganId());
        paramMap.put("offset",req.getOffset());
        paramMap.put("pageSize",req.getPageSize());
        paramMap.put("resourceName",req.getResourceName());
        paramMap.put("resourceState",0);
//        paramMap.put("fileHandleField",rmFileHandleFieldY(dataResourcesList.get(0).getFileHandleField()));
        if (organConfiguration.getSysLocalOrganId().equals(req.getOrganId())){
            List<DataResource> dataResources = dataResourceRepository.queryDataResource(paramMap);
            if (dataResources.size()==0){
                return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
            }
            Integer count = dataResourceRepository.queryDataResourceCount(paramMap);
            return BaseResultEntity.success(new PageDataEntity(count.intValue(),req.getPageSize(),req.getPageNo(),dataResources.stream().map(re-> DataResourceConvert.resourceConvertSelectVo(re)).collect(Collectors.toList())));
        }else {
//            req.setColumnStr(rmFileHandleFieldY(dataResourcesList.get(0).getFileHandleField()));
            log.info(JSONObject.toJSONString(req));
            return otherBusinessesService.getOrganResourceList(req);
        }
    }

    public String rmFileHandleFieldY(String fileHandleField){
        return Arrays.stream(fileHandleField.split(",")).filter(key->!"y".equals(key)).collect(Collectors.joining(","));
    }

    public BaseResultEntity getDerivationResourceList(Long projectId) {
        DerivationResourceReq req = new DerivationResourceReq();
        req.setProjectId(projectId);
        req.setPageSize(1000);
        List<DataDerivationResourceVo> dataDerivationResourceVos = dataResourceRepository.queryDerivationResourceList(req);
        Set<Long> userIds = dataDerivationResourceVos.stream().map(DataDerivationResourceVo::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> sysUserMap = sysUserService.getSysUserMap(userIds);
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        for (DataDerivationResourceVo dataDerivationResourceVo : dataDerivationResourceVos) {
            SysUser sysUser = sysUserMap.get(dataDerivationResourceVo.getUserId());
            dataDerivationResourceVo.setUserName(sysUser==null?"":sysUser.getUserName());
            dataDerivationResourceVo.setOrganId(sysLocalOrganInfo.getOrganId());
            dataDerivationResourceVo.setOrganName(sysLocalOrganInfo.getOrganName());
            dataDerivationResourceVo.setFileFields(dataResourceRepository.queryDataFileFieldByFileId(dataDerivationResourceVo.getId()).stream().map(DataResourceConvert::DataFileFieldPoConvertVo).collect(Collectors.toList()));
        }
        return BaseResultEntity.success(dataDerivationResourceVos);
    }
}
