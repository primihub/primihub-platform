package com.primihub.biz.convert;

import com.primihub.biz.entity.data.po.DataPsi;
import com.primihub.biz.entity.data.po.DataPsiResource;
import com.primihub.biz.entity.data.po.DataPsiTask;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.req.DataPsiReq;
import com.primihub.biz.entity.data.req.DataPsiResourceReq;
import com.primihub.biz.entity.data.vo.DataPsiResourceVo;
import com.primihub.biz.entity.data.vo.DataPsiVo;
import com.primihub.biz.entity.data.vo.DataResourceRecordVo;
import com.primihub.biz.entity.data.vo.PsiTaskVo;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.sys.po.SysOrgan;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

public class DataPsiConvert {

    public static DataPsiResourceVo DataPsiResourceConvertVo(DataPsiResource dataPsiResource){
        if (dataPsiResource==null)
            return null;
        DataPsiResourceVo dataPsiResourceVo = new DataPsiResourceVo();
        dataPsiResourceVo.setId(dataPsiResource.getId());
        dataPsiResourceVo.setResourceId(dataPsiResource.getResourceId());
        dataPsiResourceVo.setTableStructureTemplate(dataPsiResource.getTableStructureTemplate());
        dataPsiResourceVo.setOrganType(dataPsiResource.getOrganType());
        dataPsiResourceVo.setResultsAllowOpen(dataPsiResource.getResultsAllowOpen());
        dataPsiResourceVo.setKeywordList(StringUtils.isNotBlank(dataPsiResource.getKeywordList())?dataPsiResource.getKeywordList().split(","):new String[]{});
        dataPsiResourceVo.setCreateDate(dataPsiResource.getCreateDate());
        return dataPsiResourceVo;
    }

    public static DataPsi DataPsiReqConvertPo(DataPsiReq req){
        DataPsi dataPsi = new DataPsi();
        dataPsi.setOwnOrganId(req.getOwnOrganId());
        dataPsi.setOwnResourceId(req.getOwnResourceId());
        dataPsi.setOwnKeyword(req.getOwnKeyword());
        dataPsi.setOtherOrganId(req.getOtherOrganId());
        dataPsi.setOtherResourceId(req.getOtherResourceId());
        dataPsi.setOtherKeyword(req.getOtherKeyword());
        dataPsi.setOutputFilePathType(req.getOutputFilePathType()==null?0:req.getOutputFilePathType());
        dataPsi.setOutputNoRepeat(req.getOutputNoRepeat()==null?0:req.getOutputNoRepeat());
        dataPsi.setColumnCompleteStatistics(req.getColumnCompleteStatistics()==null?0:req.getColumnCompleteStatistics());
        dataPsi.setResultName(req.getResultName());
        dataPsi.setOutputContent(req.getOutputContent()==null?0:req.getOutputContent());
        dataPsi.setOutputFormat(StringUtils.isBlank(req.getOutputFormat())?"csv":req.getOutputFormat());
        dataPsi.setResultOrganIds(req.getResultOrganIds());
        dataPsi.setRemarks(req.getRemarks());
        dataPsi.setServerAddress(req.getServerAddress());
        return dataPsi;
    }

    public static DataPsiResource DataPsiResourceReqConvertPo(DataPsiResourceReq req){
        DataPsiResource dataPsiResource = new DataPsiResource();
        dataPsiResource.setId(req.getId());
        dataPsiResource.setResourceId(req.getResourceId());
        dataPsiResource.setTableStructureTemplate(req.getTableStructureTemplate());
        dataPsiResource.setOrganType(req.getOrganType());
        dataPsiResource.setResultsAllowOpen(req.getResultsAllowOpen());
        dataPsiResource.setKeywordList(req.getKeywordList());
        dataPsiResource.setPsiResourceDesc(req.getPsiResourceDesc());
        return dataPsiResource;

    }

    public static DataPsiVo DataPsiConvertVo(DataPsiTask task, DataPsi dataPsi, Map<Long, SysOrgan> sysOrganMap, Map<Long, DataResourceRecordVo> resourceMap){
        DataPsiVo dataPsiVo = new DataPsiVo();
        dataPsiVo.setId(task.getId());
        dataPsiVo.setOwnOrganId(dataPsi.getOwnOrganId());
        dataPsiVo.setOwnOrganName(sysOrganMap.containsKey(dataPsi.getOwnOrganId())?sysOrganMap.get(dataPsi.getOwnOrganId()).getOrganName():"");
        dataPsiVo.setOwnResourceId(dataPsi.getOwnResourceId());
        dataPsiVo.setOwnResourceName(resourceMap.containsKey(dataPsi.getOwnResourceId())?resourceMap.get(dataPsi.getOwnResourceId()).getResourceName():"");
        dataPsiVo.setOwnKeyword(dataPsi.getOwnKeyword());
        dataPsiVo.setOtherOrganId(dataPsi.getOtherOrganId());
        dataPsiVo.setOtherOrganName(sysOrganMap.containsKey(dataPsi.getOtherOrganId())?sysOrganMap.get(dataPsi.getOtherOrganId()).getOrganName():"");
        dataPsiVo.setOtherResourceId(dataPsi.getOtherResourceId());
        dataPsiVo.setOtherResourceName(resourceMap.containsKey(dataPsi.getOtherResourceId())?resourceMap.get(dataPsi.getOtherResourceId()).getResourceName():"");
        dataPsiVo.setOtherKeyword(dataPsi.getOtherKeyword());
        dataPsiVo.setOutputFilePathType(dataPsi.getOutputFilePathType());
        dataPsiVo.setOutputNoRepeat(dataPsi.getOutputNoRepeat());
        dataPsiVo.setColumnCompleteStatistics(dataPsi.getColumnCompleteStatistics());
        dataPsiVo.setResultName(dataPsi.getResultName());
        dataPsiVo.setOutputContent(dataPsi.getOutputContent());
        dataPsiVo.setOutputFormat(dataPsi.getOutputFormat());
        dataPsiVo.setResultOrganIds(dataPsi.getResultOrganIds());
        dataPsiVo.setResultOrganName(dataPsi.getResultOrganIds());
        if (StringUtils.isNotBlank(dataPsi.getResultOrganIds())){
            String[] organIds = dataPsi.getResultOrganIds().split(",");
            for (String organId : organIds) {
                dataPsiVo.setResultOrganName(dataPsiVo.getResultOrganName().replace(organId,(sysOrganMap.containsKey(Long.valueOf(organId))?sysOrganMap.get(Long.valueOf(organId)).getOrganName():organId)));
            }
        }
        dataPsiVo.setRemarks(dataPsi.getRemarks());
        dataPsiVo.setCreateDate(task.getCreateDate());
        dataPsiVo.setTaskId(task.getId());
        dataPsiVo.setTaskIdName(task.getTaskId());
        dataPsiVo.setTaskState(task.getTaskState());
        return dataPsiVo;

    }

    public static PsiTaskVo DataPsiTaskConvertVo(DataPsiTask task){
        PsiTaskVo psiTaskVo = new PsiTaskVo();
        psiTaskVo.setTaskId(task.getId());
        psiTaskVo.setPsiId(task.getPsiId());
        psiTaskVo.setTaskIdName(task.getTaskId());
        psiTaskVo.setTaskState(task.getTaskState());
        psiTaskVo.setAscription(task.getAscription());
        psiTaskVo.setAscriptionType(task.getAscriptionType());
        psiTaskVo.setCreateDate(task.getCreateDate());
        return psiTaskVo;

    }

    public static DataPsiVo DataPsiConvertVo(DataPsiTask task, DataPsi dataPsi, DataResource dataResource, Map<String, Object> otherDataResource, SysLocalOrganInfo sysLocalOrganInfo) {
        DataPsiVo dataPsiVo = new DataPsiVo();
        dataPsiVo.setId(task.getId());
        dataPsiVo.setOwnOrganId(dataPsi.getOwnOrganId());
        dataPsiVo.setOwnOrganName(sysLocalOrganInfo.getOrganName());
        dataPsiVo.setOwnResourceId(dataPsi.getOwnResourceId());
        dataPsiVo.setOwnResourceName(dataResource.getResourceName());
        dataPsiVo.setOwnKeyword(dataPsi.getOwnKeyword());
        dataPsiVo.setOtherOrganId(dataPsi.getOtherOrganId());
        dataPsiVo.setOtherOrganName(otherDataResource==null?"":otherDataResource.get("organName")==null?"":otherDataResource.get("organName").toString());
        dataPsiVo.setOtherResourceId(dataPsi.getOtherResourceId());
        dataPsiVo.setOtherResourceName(otherDataResource==null?"":otherDataResource.get("resourceName")==null?"":otherDataResource.get("resourceName").toString());
        dataPsiVo.setOtherKeyword(dataPsi.getOtherKeyword());
        dataPsiVo.setOutputFilePathType(dataPsi.getOutputFilePathType());
        dataPsiVo.setOutputNoRepeat(dataPsi.getOutputNoRepeat());
        dataPsiVo.setColumnCompleteStatistics(dataPsi.getColumnCompleteStatistics());
        dataPsiVo.setResultName(dataPsi.getResultName());
        dataPsiVo.setOutputContent(dataPsi.getOutputContent());
        dataPsiVo.setOutputFormat(dataPsi.getOutputFormat());
        dataPsiVo.setResultOrganIds(dataPsi.getResultOrganIds());
        dataPsiVo.setResultOrganName(dataPsi.getResultOrganIds());
        if (StringUtils.isNotBlank(dataPsi.getResultOrganIds())){
            String[] organIds = dataPsi.getResultOrganIds().split(",");
            if (organIds.length==2){
                dataPsiVo.setResultOrganName(dataPsiVo.getOwnOrganName()+","+dataPsiVo.getOtherOrganName());
            }else {
                dataPsiVo.setResultOrganName(dataPsiVo.getOwnOrganName());
            }
        }
        dataPsiVo.setRemarks(dataPsi.getRemarks());
        dataPsiVo.setCreateDate(task.getCreateDate());
        dataPsiVo.setTaskId(task.getId());
        dataPsiVo.setTaskIdName(task.getTaskId());
        dataPsiVo.setTaskState(task.getTaskState());
        return dataPsiVo;
    }
}
