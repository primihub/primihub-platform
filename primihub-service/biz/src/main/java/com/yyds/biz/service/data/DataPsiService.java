package com.yyds.biz.service.data;

import com.yyds.biz.convert.DataPsiConvert;
import com.yyds.biz.convert.DataResourceConvert;
import com.yyds.biz.entity.base.BaseResultEntity;
import com.yyds.biz.entity.base.BaseResultEnum;
import com.yyds.biz.entity.base.PageDataEntity;
import com.yyds.biz.entity.data.po.*;
import com.yyds.biz.entity.data.req.DataPsiReq;
import com.yyds.biz.entity.data.req.DataPsiResourceReq;
import com.yyds.biz.entity.data.req.DataResourceReq;
import com.yyds.biz.entity.data.req.PageReq;
import com.yyds.biz.entity.data.vo.*;
import com.yyds.biz.entity.sys.po.SysOrgan;
import com.yyds.biz.repository.primarydb.data.DataPsiPrRepository;
import com.yyds.biz.repository.secondarydb.data.DataPsiRepository;
import com.yyds.biz.repository.secondarydb.data.DataResourceRepository;
import com.yyds.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.yyds.biz.service.sys.SysOrganService;
import com.yyds.biz.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DataPsiService {

    @Autowired
    private DataResourceRepository dataResourceRepository;
    @Autowired
    private DataPsiRepository dataPsiRepository;
    @Autowired
    private DataPsiPrRepository dataPsiPrRepository;
    @Autowired
    private DataResourceService dataResourceService;
    @Autowired
    private SysOrganSecondarydbRepository sysOrganSecondarydbRepository;

    @Autowired
    private SysOrganService sysOrganService;

    @Autowired
    private PsiAsyncService psiAsyncService;

    public BaseResultEntity getPsiResourceDetails(Long resourceId) {
        DataResource dataResource = dataResourceRepository.queryDataResourceById(resourceId);
        if (dataResource==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到资源信息");
        DataPsiResourceVo dataPsiResourceVo = DataPsiConvert.DataPsiResourceConvertVo(dataPsiRepository.selectPsiResourceByResourceId(resourceId));
        Map<String,Object> map = new HashMap<>();
        map.put("resourceId",dataResource.getResourceId());
        map.put("resourceName",dataResource.getResourceName());
        map.put("resourceState",0);
        map.put("resourceOrganId",dataResource.getOrganId());
        SysOrgan sysOrgan = sysOrganSecondarydbRepository.selectSysOrganByOrganId(dataResource.getOrganId());
        map.put("resourceOrganName",sysOrgan!=null?sysOrgan.getOrganName():"");
        List<DataFileHandleFieldVo> voList = new ArrayList<>();
        if (dataResource.getFileHandleField()!=null){
            List<String> keywordList = new ArrayList<>();
            if (dataPsiResourceVo!=null&&dataPsiResourceVo.getKeywordList()!=null){
                keywordList.addAll(Arrays.asList(dataPsiResourceVo.getKeywordList()));
            }
            String[] fieldsplit = dataResource.getFileHandleField().split(",");
            for (String field : fieldsplit) {
                voList.add(new DataFileHandleFieldVo(field,keywordList.contains(field)));
            }
        }
        map.put("resourceField",voList);
        map.put("psiResource",dataPsiResourceVo);
        return BaseResultEntity.success(map);
    }

    public BaseResultEntity saveOrUpdatePsiResource(DataPsiResourceReq req) {
        DataPsiResource dataPsiResource = DataPsiConvert.DataPsiResourceReqConvertPo(req);
        DataPsiResource existencePsiResource = dataPsiRepository.selectPsiResourceByResourceId(req.getResourceId());
        if (existencePsiResource!=null){
            dataPsiResource.setId(existencePsiResource.getId());
        }
        if (dataPsiResource.getId()==null){
            dataPsiPrRepository.saveDataPsiResource(dataPsiResource);
        }else {
            dataPsiPrRepository.updateDataPsiResource(dataPsiResource);
        }
        Map<String,Object> map = new HashMap<>();
        map.put("id",dataPsiResource.getId());
        return BaseResultEntity.success(map);
    }


    public BaseResultEntity getPsiResourceList(DataResourceReq req, Long organId) {
        return dataResourceService.getDataResourceList(req,null,organId,true);
    }

    public BaseResultEntity getPsiResourceAllocationList(DataResourceReq req, Long organId,Long ownOrganId) {
//        Map<String,Object> paramMap = new HashMap<>();
//        paramMap.put("organId",organId);
//        paramMap.put("offset",req.getOffset());
//        paramMap.put("pageSize",req.getPageSize());
//        paramMap.put("resourceName",req.getResourceName());
//        List<DataPsiResourceAllocationVo> dataPsiResourceAllocationVos = dataPsiRepository.selectPsiResourceAllocation(paramMap);
//        if (dataPsiResourceAllocationVos.size()==0){
//            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
//        }
//        Long count = dataPsiRepository.selectPsiResourceAllocationCount(paramMap);
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("organId",organId);
        paramMap.put("offset",req.getOffset());
        paramMap.put("pageSize",req.getPageSize());
        paramMap.put("resourceName",req.getResourceName());
        paramMap.put("isPsi","true");
        if (organId.equals(ownOrganId)){
            paramMap.put("isOwn",1);
        }
        List<DataResource> dataResources = dataResourceRepository.queryDataResource(paramMap);
        if (dataResources.size()==0){
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Integer count = dataResourceRepository.queryDataResourceCount(paramMap);
        Set<Long> resourceIds = dataResources.stream().map(DataResource::getResourceId).collect(Collectors.toSet());
        List<DataFileField> dataFileField = dataResourceRepository.queryDataFileField(new HashMap() {{
            put("resourceIds", resourceIds);
            put("relevance", 1);
            put("protectionStatus", 1);
        }});
        Map<Long, List<DataFileField>> fileFieldMap = dataFileField.stream().collect(Collectors.groupingBy(DataFileField::getResourceId));
        return BaseResultEntity.success(new PageDataEntity(count.intValue(),req.getPageSize(),req.getPageNo(),dataResources.stream().map(re->{
            return DataResourceConvert.DataResourcePoConvertAllocationVo(re,fileFieldMap.get(re.getResourceId()));
        }).collect(Collectors.toList())));
    }

    public BaseResultEntity saveDataPsi(DataPsiReq req,Long userId) {
        DataPsi dataPsi = DataPsiConvert.DataPsiReqConvertPo(req);
        dataPsi.setUserId(userId);
        dataPsiPrRepository.saveDataPsi(dataPsi);
        DataPsiTask task = new DataPsiTask();
        task.setPsiId(dataPsi.getId());
        task.setTaskId(UUID.randomUUID().toString());
        task.setTaskState(0);
        if (dataPsi.getResultOrganIds().indexOf(",")!=-1){
            task.setAscription("双方获取");
            task.setAscriptionType(1);
        }else {
            task.setAscription("一方获取");
            task.setAscriptionType(0);
        }
        if (dataPsi.getOutputContent()==0){
            task.setAscription(task.getAscription()+"交集");
        }else {
            task.setAscription(task.getAscription()+"差集");
        }
        task.setCreateDate(new Date());
        dataPsiPrRepository.saveDataPsiTask(task);
        psiAsyncService.psiGrpcRun(task,dataPsi);
        Map<String, Object> map = new HashMap<>();
        map.put("dataPsi",dataPsi);
        map.put("dataPsiTask",DataPsiConvert.DataPsiTaskConvertVo(task));
        return BaseResultEntity.success(map);
    }

    public void psiTaskOutputFileHandle(DataPsiTask task){
        if (task.getTaskState()!=1)
            return;
        List<String> fileContent = FileUtil.getFileContent(task.getFilePath(), null);
        StringBuilder sb = new StringBuilder();
        for (String line : fileContent) {
            sb.append(line).append("\r\n");
        }
        task.setFileContent(sb.toString());
        task.setFileRows(fileContent.size());
        dataPsiPrRepository.updateDataPsiTask(task);
    }

    public BaseResultEntity getPsiTaskList(PageReq req, Long userId) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userId",userId);
        paramMap.put("offset",req.getOffset());
        paramMap.put("pageSize",req.getPageSize());
        List<DataPsiTaskVo> dataPsiTaskVos = dataPsiRepository.selectPsiTaskPage(paramMap);
        if (dataPsiTaskVos.size()==0){
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
        Long count = dataPsiRepository.selectPsiTaskPageCount(paramMap);
        return BaseResultEntity.success(new PageDataEntity(count.intValue(),req.getPageSize(),req.getPageNo(),dataPsiTaskVos));
    }

    public BaseResultEntity getOrganPsiTask(Long userId, Long ownOrganId, Long organId, String resultName, PageReq req) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userId",userId);
        paramMap.put("offset",req.getOffset());
        paramMap.put("pageSize",req.getPageSize());
        paramMap.put("ownOrganId",ownOrganId);
        paramMap.put("organId",organId);
        paramMap.put("resultName",resultName);
        if (ownOrganId.compareTo(organId)!=0){//其他机构
            paramMap.put("other",1);
//            paramMap.put("organId",organId);
        }
        List<DataOrganPsiTaskVo> dataOrganPsiTaskVos = dataPsiRepository.selectOrganPsiTaskPage(paramMap);
        if (dataOrganPsiTaskVos.size()==0)
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        Long count = dataPsiRepository.selectOrganPsiTaskPageCount(paramMap);
        return BaseResultEntity.success(new PageDataEntity(count.intValue(),req.getPageSize(),req.getPageNo(),dataOrganPsiTaskVos));
    }

    public BaseResultEntity getPsiTaskDetails(Long taskId, Long userId) {
        DataPsiTask task = dataPsiRepository.selectPsiTaskById(taskId);
        if (task==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到任务信息");
        DataPsi dataPsi = dataPsiRepository.selectPsiById(task.getPsiId());
        if (dataPsi==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL,"未查询到PSI信息");
        Set<Long> organ = new HashSet<>();
        organ.add(dataPsi.getOtherOrganId());
        organ.add(dataPsi.getOwnOrganId());
        Map<Long, SysOrgan> sysOrganMap = sysOrganService.getSysOrganMap(organ);
        Set<Long> resource = new HashSet<>();
        resource.add(dataPsi.getOtherResourceId());
        resource.add(dataPsi.getOwnResourceId());
        List<DataResourceRecordVo> dataResourceRecordVos = dataResourceRepository.queryDataResourceByIds(resource);
        Map<Long, DataResourceRecordVo> resourceMap = dataResourceRecordVos.stream().collect(Collectors.toMap(DataResourceRecordVo::getResourceId, Function.identity()));
        return BaseResultEntity.success(DataPsiConvert.DataPsiConvertVo(task,dataPsi,sysOrganMap,resourceMap));
    }

    public DataPsiTask selectPsiTaskById(Long taskId){
        return dataPsiRepository.selectPsiTaskById(taskId);
    }
    public DataPsi selectPsiById(Long psiId){
        return dataPsiRepository.selectPsiById(psiId);
    }

    public BaseResultEntity delPsiTask(Long taskId, Long userId) {
        DataPsiTask task = dataPsiRepository.selectPsiTaskById(taskId);
        if (task==null)
            return BaseResultEntity.failure(BaseResultEnum.DATA_QUERY_NULL);
        if (task.getTaskState()==2)
            return BaseResultEntity.failure(BaseResultEnum.DATA_DEL_FAIL,"运行中无法删除");
        dataPsiPrRepository.delPsiTask(task.getId());
        dataPsiPrRepository.delPsi(task.getPsiId());
        return BaseResultEntity.success();
    }

    public BaseResultEntity cancelPsiTask(Long taskId, Long userId) {
        DataPsiTask task = dataPsiRepository.selectPsiTaskById(taskId);
        task.setTaskState(4);
        dataPsiPrRepository.updateDataPsiTask(task);
        return BaseResultEntity.success();
    }

    public BaseResultEntity retryPsiTask(Long taskId, Long userId) {
        DataPsiTask task = dataPsiRepository.selectPsiTaskById(taskId);
        if (task.getTaskState()==1||task.getTaskState()==2)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"运行中或完成");
        if (task.getTaskState()<3)
            return BaseResultEntity.failure(BaseResultEnum.DATA_RUN_TASK_FAIL,"无法运行");
        DataPsi dataPsi = dataPsiRepository.selectPsiById(task.getPsiId());
        task.setTaskState(2);
        dataPsiPrRepository.updateDataPsiTask(task);
        psiAsyncService.psiGrpcRun(task,dataPsi);
        return BaseResultEntity.success();
    }
}
