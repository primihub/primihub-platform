package com.yyds.biz.service.data;

import com.yyds.biz.convert.DataModelConvert;
import com.yyds.biz.convert.DataProjectConvert;
import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.base.BaseResultEnum;
import com.yyds.biz.entity.base.PageDataEntity;
import com.yyds.biz.entity.data.po.*;
import com.yyds.biz.entity.data.req.DataProjectReq;
import com.yyds.biz.entity.data.vo.*;
import com.yyds.biz.entity.sys.po.SysOrgan;
import com.yyds.biz.entity.sys.po.SysUser;
import com.yyds.biz.repository.primarydb.data.DataProjectPrRepository;
import com.yyds.biz.repository.primarydb.data.DataResourcePrRepository;
import com.yyds.biz.repository.secondarydb.data.DataModelRepository;
import com.yyds.biz.repository.secondarydb.data.DataProjectRepository;
import com.yyds.biz.repository.secondarydb.data.DataResourceRepository;
import com.yyds.biz.service.sys.SysOrganService;
import com.yyds.biz.service.sys.SysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataProjectService {
    @Autowired
    private DataProjectRepository dataProjectRepository;
    @Autowired
    private DataProjectPrRepository dataProjectPrRepository;
    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private DataResourcePrRepository dataResourcePrRepository;
    @Autowired
    private DataModelRepository dataModelRepository;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private SysOrganService sysOrganService;

    public BaseResultEntity saveDataProject(Long userId, Long organId, DataProjectReq req) {
        DataProject dataProject = DataProjectConvert.dataDataProjectReqConvertPo(req, userId, organId);
        dataProjectPrRepository.saveProject(dataProject);
        Set<Long> resources = new HashSet<>(req.getResources());
        if (resources!=null && resources.size()>0){
            List<DataResourceRecordVo> dataResourceRecordVos = dataResourceRepository.queryDataResourceByIds(resources);
            List<DataProjectResource> prList = new ArrayList<>();
            List<DataResourceAuthRecord> rarList = new ArrayList<>();
            List<Long> organIds = new ArrayList<>();
            int authResourceNum = 0;
            for (DataResourceRecordVo resource : dataResourceRecordVos) {
                // 用户id 或者 机构id
                boolean isHold = (resource.getUserId().compareTo(userId)==0||resource.getOrganId().compareTo(organId)==0);
                prList.add(new DataProjectResource(dataProject.getProjectId(),resource.getResourceId(),isHold));
                if (!isHold){
                    rarList.add(new DataResourceAuthRecord(dataProject.getProjectId(),resource.getResourceId()));
                }
                if (!organIds.contains(resource.getOrganId()) && !isHold){
                    organIds.add(resource.getOrganId());
                }
                if (isHold){
                    authResourceNum ++;
                }
            }
            dataProjectPrRepository.saveProjectResource(prList);
            if (rarList.size()>0){
                dataResourcePrRepository.saveResourceAuthRecordList(rarList);
            }
            dataProject.setAuthResourceNum(authResourceNum);
            dataProject.setResourceNum(prList.size());
            // TODO 待定是否+1 liweihua
            dataProject.setOrganNum(organIds.size());
            dataProject.setResourceOrganIds(organIds.stream().map(String::valueOf).collect(Collectors.joining(",")));
            dataProjectPrRepository.editProject(dataProject);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("projectId",dataProject.getProjectId());
        map.put("projectName",dataProject.getProjectName());
        map.put("projectDesc",dataProject.getProjectDesc());
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getDataProjectList(Long userId, Long organId, DataProjectReq req) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userId",userId);
        paramMap.put("organId",organId);
        return queryDataProject(paramMap,req);
    }

    private BaseResultEntity queryDataProject(Map<String,Object> paramMap, DataProjectReq req){
        paramMap.put("offset",req.getOffset());
        paramMap.put("pageSize",req.getPageSize());
        paramMap.put("projectName",req.getProjectName());
        List<DataProject> dataProjects = dataProjectRepository.queryDataProject(paramMap);
        if (dataProjects.size()==0){
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Integer count = dataProjectRepository.queryDataProjectCount(paramMap);
        Set<Long> userIdSet = new HashSet<>();
        Set<Long> projectIddSet = new HashSet<>();
        for (DataProject dataProject : dataProjects) {
            userIdSet.add(dataProject.getUserId());
            projectIddSet.add(dataProject.getProjectId());
        }
        Map<Long, SysUser> sysUserMap = sysUserService.getSysUserMap(userIdSet);
        Map<Long, Integer> modelCountMap = dataModelRepository.queryModelCountByProjectId(projectIddSet).stream().collect(Collectors.toMap(ProjectModelNumVo::getProjectId, ProjectModelNumVo::getModeNum));
        List<DataProjectListVo> voList = dataProjects.stream().map(dp -> DataProjectConvert.dataDataProjectPoConvertListVo(dp, sysUserMap.get(dp.getUserId()),modelCountMap.get(dp.getProjectId()))).collect(Collectors.toList());
        return BaseResultEntity.success(new PageDataEntity(count,req.getPageSize(),req.getPageNo(),voList));
    }

    public BaseResultEntity getDataProject(Long projectId) {
        DataProject dataProject = dataProjectRepository.queryDataProjectById(projectId);
        if (dataProject==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到项目详情");
        }
        DataProjectVo dataProjectVo = DataProjectConvert.dataDataProjectPoConvertVo(dataProject);
        // 查询资源信息
        List<DataProjectResource> projectResources = dataProjectRepository.queryProjectResourceByProjectId(projectId);
        Set<Long> resourceIds = projectResources.stream().map(DataProjectResource::getResourceId).collect(Collectors.toSet());
        Map<Long, Integer> isAuthedMap = projectResources.stream().collect(Collectors.toMap(DataProjectResource::getResourceId, DataProjectResource::getIsAuthed));
        List<DataResourceRecordVo> dataResourceRecordVos = dataResourceRepository.queryDataResourceByIds(resourceIds);
        dataProjectVo.setResources(dataResourceRecordVos);
        dataProjectVo.setOrganNames(new ArrayList<>());
        // TODO 查询补充机构 liweihua -- 完成
        Set<Long> orgIds = new HashSet<>();
        orgIds.add(dataProject.getOrganId());
        orgIds.addAll(dataResourceRecordVos.stream().map(DataResourceRecordVo::getOrganId).collect(Collectors.toSet()));
        String[] split = dataProject.getResourceOrganIds().split(",");
        for (String id : split) {
            if (id.equals(""))
                continue;
            orgIds.add(Long.valueOf(id));
        }
        Map<Long, SysOrgan> sysOrganMap = sysOrganService.getSysOrganMap(orgIds);
        dataProjectVo.setOrganId(dataProject.getOrganId());
        SysOrgan sysOrgan = sysOrganMap.get(dataProject.getOrganId());
        dataProjectVo.setOrganName(sysOrgan==null?"":sysOrgan.getOrganName());
        Iterator<Map.Entry<Long, SysOrgan>> iterator = sysOrganMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<Long, SysOrgan> next = iterator.next();
            if (!dataProject.getOrganId().equals(next.getKey())){
                dataProjectVo.getOrganNames().add(next.getValue().getOrganName());
            }
        }
        // 模型信息
        dataProjectVo.setModels(dataModelRepository.queryModelListByProjectId(projectId));
        // 资源机构
        dataResourceRecordVos.forEach(vo->{
            vo.setIsAuthed(isAuthedMap.get(vo.getResourceId()));
            vo.setOrganName(sysOrganMap.get(vo.getOrganId())==null?"":sysOrganMap.get(vo.getOrganId()).getOrganName());
        });
        return BaseResultEntity.success(dataProjectVo);
    }

    public BaseResultEntity delDataProject(Long projectId) {
        DataProject dataProject = dataProjectRepository.queryDataProjectById(projectId);
        if (dataProject==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到项目详情");
        }
        // TODO 补充删除校验规则 liweihua
        dataProjectPrRepository.delDataProject(projectId);
        return BaseResultEntity.success();
    }

    public BaseResultEntity getProjectResourceData(Long projectId) {
        DataProject dataProject = dataProjectRepository.queryDataProjectById(projectId);
        if (dataProject==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到项目详情");
        }
        Map<String,Object> map = new HashMap<>();
        map.put("projectId",dataProject.getProjectId());
        map.put("projectName",dataProject.getProjectName());
        List<DataProjectResource> projectResources = dataProjectRepository.queryProjectResourceByProjectId(projectId);
        Set<Long> resourceIds = projectResources.stream().map(DataProjectResource::getResourceId).collect(Collectors.toSet());
        List<DataResource> dataResourceRecordVos = dataResourceRepository.queryDataResourceByResourceIds(resourceIds);
        List<ComponentResourceVo> componentResources = dataResourceRecordVos.stream().map(DataModelConvert::resourceRecordVoConvertComponentResourceVo).collect(Collectors.toList());
        map.put("resource",componentResources);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity getProjectAuthedeList(DataProjectReq req,Long userId,Long organId) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userId",userId);
        paramMap.put("organId",organId);
        paramMap.put("isAuthed",1);
        return queryDataProject(paramMap,req);
    }

    public BaseResultEntity getMpcProjectResourceData(Long projectId) {
        DataProject dataProject = dataProjectRepository.queryDataProjectById(projectId);
        if (dataProject==null){
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到项目详情");
        }
        List<DataProjectResource> projectResources = dataProjectRepository.queryProjectResourceByProjectId(projectId);
        projectResources = projectResources.stream().filter(pr->pr.getIsAuthed()==1).collect(Collectors.toList());
        List<MpcProjectResource> list = new ArrayList<>();
        if (projectResources.size()>0){
            Set<Long> resourceIds = projectResources.stream().map(DataProjectResource::getResourceId).collect(Collectors.toSet());
            List<DataResource> dataResourceRecordVos = dataResourceRepository.queryDataResourceByResourceIds(resourceIds);
            List<DataFileField> fileFields = dataResourceRepository.queryDataFileField(new HashMap() {{
                put("resourceIds", resourceIds);
                put("protectionStatus", 1);
            }});
            Map<Long, List<DataResource>> organResourceMap = dataResourceRecordVos.stream().collect(Collectors.groupingBy(DataResource::getOrganId));
            Map<Long, SysOrgan> sysOrganMap = sysOrganService.getSysOrganMap(organResourceMap.keySet());
            Map<Long, List<DataFileField>> resourceFieldMap = fileFields.stream().collect(Collectors.groupingBy(DataFileField::getResourceId));
            Iterator<Map.Entry<Long, List<DataResource>>> iterator = organResourceMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<Long, List<DataResource>> next = iterator.next();
                Long key = next.getKey();
                List<DataResource> value = next.getValue();
                MpcProjectResource mpcProject = new MpcProjectResource(key,sysOrganMap.get(key)==null?"":sysOrganMap.get(key).getOrganName());
                for (DataResource dataResource : value) {
                    mpcProject.getChildren().add(new MpcProjectResource(dataResource.getResourceId(),dataResource.getResourceName(),resourceFieldMap.get(dataResource.getResourceId())));
                }
                list.add(mpcProject);
            }
        }
        return BaseResultEntity.success(list);
    }
}
