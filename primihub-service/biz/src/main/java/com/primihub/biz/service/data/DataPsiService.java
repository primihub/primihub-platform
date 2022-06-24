package com.primihub.biz.service.data;

import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.convert.DataPsiConvert;
import com.primihub.biz.convert.DataResourceConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.*;
import com.primihub.biz.entity.data.vo.DataFileHandleFieldVo;
import com.primihub.biz.entity.data.vo.DataOrganPsiTaskVo;
import com.primihub.biz.entity.data.vo.DataPsiResourceVo;
import com.primihub.biz.entity.data.vo.DataPsiTaskVo;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.repository.primarydb.data.DataPsiPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataPsiRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.service.sys.SysOrganService;
import com.primihub.biz.util.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private FusionResourceService fusionResourceService;
    @Autowired
    private SysOrganService sysOrganService;

    @Autowired
    private PsiAsyncService psiAsyncService;

    @Autowired
    private OrganConfiguration organConfiguration;


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

    public BaseResultEntity getPsiResourceAllocationList(PageReq req, String organId, String serverAddress) {
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        String localOrganId = organConfiguration.getSysLocalOrganId();
        if(StringUtils.isBlank(organId) || sysLocalOrganInfo==null || StringUtils.isBlank(sysLocalOrganInfo.getOrganId()) || sysLocalOrganInfo.getOrganId().equals(organId)){
            Map<String,Object> paramMap = new HashMap<>();
            paramMap.put("organId",organId);
            paramMap.put("offset",req.getOffset());
            paramMap.put("pageSize",req.getPageSize());
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
            return BaseResultEntity.success(new PageDataEntity(count.intValue(),req.getPageSize(),req.getPageNo(),dataResources.stream().map(re-> DataResourceConvert.DataResourcePoConvertAllocationVo(re,fileFieldMap.get(re.getResourceId()),localOrganId)).collect(Collectors.toList())));
        }else if (!sysLocalOrganInfo.getOrganId().equals(organId)){
            if (StringUtils.isBlank(serverAddress))
                return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
            DataFResourceReq fResourceReq = new DataFResourceReq();
            fResourceReq.setPageNo(req.getPageNo());
            fResourceReq.setPageSize(req.getPageSize());
            fResourceReq.setServerAddress(serverAddress);
            BaseResultEntity baseResult = fusionResourceService.getResourceList(fResourceReq);
            if (baseResult.getCode()!=0)
                return baseResult;
            Map<String,Object> pageData = (LinkedHashMap<String,Object>)baseResult.getResult();
            List<LinkedHashMap<String,Object>> voList = (List<LinkedHashMap<String,Object>>)pageData.getOrDefault("data",new ArrayList<>());
            return BaseResultEntity.success(new PageDataEntity(Integer.valueOf(pageData.getOrDefault("total","0").toString()),Integer.valueOf(pageData.getOrDefault("pageSize","0").toString()),Integer.valueOf(pageData.getOrDefault("index","0").toString()),voList.stream().map(fr->DataResourceConvert.fusionResourceConvertAllocationVo(fr,localOrganId)).collect(Collectors.toList())));
        }else {
            return BaseResultEntity.success(new PageDataEntity(0,req.getPageSize(),req.getPageNo(),new ArrayList()));
        }
    }

    public BaseResultEntity saveDataPsi(DataPsiReq req, Long userId) {
        if (StringUtils.isBlank(req.getServerAddress())){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM,"serverAddress");
        }
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

    public BaseResultEntity getOrganPsiTask(Long userId, String resultName, PageReq req) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("userId",userId);
        paramMap.put("offset",req.getOffset());
        paramMap.put("pageSize",req.getPageSize());
        paramMap.put("resultName",resultName);
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
        DataResource dataResource = dataResourceRepository.queryDataResourceById(dataPsi.getOwnResourceId());
        Map<String, Object> otherDataResource = null;
        if (dataPsi.getOtherOrganId().equals(organConfiguration.getSysLocalOrganId())){
            DataResource otherResource = dataResourceRepository.queryDataResourceById(dataPsi.getOwnResourceId());
            otherDataResource = new LinkedHashMap<>();
            otherDataResource.put("organName",organConfiguration.getSysLocalOrganName());
            otherDataResource.put("resourceName",otherResource.getResourceName());
        }else {
            BaseResultEntity baseResult = fusionResourceService.getDataResource(dataPsi.getServerAddress(), dataPsi.getOtherResourceId());
            if (baseResult.getCode()==0){
                otherDataResource = (LinkedHashMap)baseResult.getResult();
            }
        }
        SysLocalOrganInfo sysLocalOrganInfo = organConfiguration.getSysLocalOrganInfo();
        return BaseResultEntity.success(DataPsiConvert.DataPsiConvertVo(task,dataPsi,dataResource,otherDataResource,sysLocalOrganInfo));
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
//        psiAsyncService.psiGrpcRun(task,dataPsi);
        return BaseResultEntity.success();
    }
}
