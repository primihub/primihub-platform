package com.primihub.biz.convert;

import com.primihub.biz.entity.data.po.DataProject;
import com.primihub.biz.entity.data.po.DataProjectOrgan;
import com.primihub.biz.entity.data.po.DataProjectResource;
import com.primihub.biz.entity.data.req.DataProjectReq;
import com.primihub.biz.entity.data.vo.DataProjectDetailsVo;
import com.primihub.biz.entity.data.vo.DataProjectListVo;
import com.primihub.biz.entity.data.vo.DataProjectOrganVo;
import com.primihub.biz.entity.data.vo.DataProjectResourceVo;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DataProjectConvert {

    public static DataProject dataProjectReqConvertPo(DataProjectReq req, SysLocalOrganInfo organInfo,String userName){
        DataProject dataProject = new DataProject();
        dataProject.setId(req.getId());
        dataProject.setProjectId(req.getProjectId());
        dataProject.setProjectName(req.getProjectName());
        dataProject.setProjectDesc(req.getProjectDesc());
        dataProject.setCreatedOrganId(organInfo.getOrganId());
        dataProject.setCreatedOrganName(organInfo.getOrganName());
        dataProject.setCreatedUsername(userName);
        dataProject.setResourceNum(0);
        dataProject.setProviderOrganNames("");
        dataProject.setServerAddress(req.getServerAddress());
        dataProject.setStatus(0);
        return dataProject;
    }

    public static DataProjectListVo dataProjectConvertListVo(DataProject dataProject, List<Map<String, Object>> list){
        DataProjectListVo dataProjectListVo = new DataProjectListVo();
        dataProjectListVo.setId(dataProject.getId());
        dataProjectListVo.setProjectId(dataProject.getProjectId());
        dataProjectListVo.setProjectName(dataProject.getProjectName());
        dataProjectListVo.setProjectDesc(dataProject.getProjectDesc());
        dataProjectListVo.setUserName(dataProject.getCreatedUsername());
        dataProjectListVo.setCreatedOrganName(dataProject.getCreatedOrganName());
        dataProjectListVo.setResourceNum(dataProject.getResourceNum());
        dataProjectListVo.setProviderOrganNames(dataProject.getProviderOrganNames());
        dataProjectListVo.setStatus(dataProject.getStatus());
        dataProjectListVo.setCreateDate(dataProject.getCreateDate());
        if (list!=null){
            for (Map<String, Object> objectMap : list) {
                Object latestTaskStatus = objectMap.get("latestTaskStatus");
                if (latestTaskStatus!=null){
                    if ("0".equals(latestTaskStatus.toString())){
                        dataProjectListVo.setModelAssembleNum(objectMap.get("statusCount")==null?0:Integer.valueOf(objectMap.get("statusCount").toString()));
                        dataProjectListVo.setModelNum(dataProjectListVo.getModelNum()+ dataProjectListVo.getModelAssembleNum());
                    }
                    if ("1".equals(latestTaskStatus.toString())){
                        dataProjectListVo.setModelRunNum(objectMap.get("statusCount")==null?0:Integer.valueOf(objectMap.get("statusCount").toString()));
                        dataProjectListVo.setModelNum(dataProjectListVo.getModelNum()+ dataProjectListVo.getModelRunNum());
                    }
                    if ("2".equals(latestTaskStatus.toString())){
                        dataProjectListVo.setModelSuccessNum(objectMap.get("statusCount")==null?0:Integer.valueOf(objectMap.get("statusCount").toString()));
                        dataProjectListVo.setModelNum(dataProjectListVo.getModelNum()+ dataProjectListVo.getModelSuccessNum());
                    }
                }
            }

        }
        return dataProjectListVo;
    }

    public static DataProjectDetailsVo dataProjectConvertDetailsVo(DataProject dataProject){
        DataProjectDetailsVo dataProjectDetailsVo = new DataProjectDetailsVo();
        dataProjectDetailsVo.setId(dataProject.getId());
        dataProjectDetailsVo.setProjectId(dataProject.getProjectId());
        dataProjectDetailsVo.setProjectName(dataProject.getProjectName());
        dataProjectDetailsVo.setProjectDesc(dataProject.getProjectDesc());
        dataProjectDetailsVo.setUserName(dataProject.getCreatedUsername());
        dataProjectDetailsVo.setStatus(dataProject.getStatus());
        dataProjectDetailsVo.setCreateDate(dataProject.getCreateDate());
        dataProjectDetailsVo.setServerAddress(dataProject.getServerAddress());
        return dataProjectDetailsVo;
    }

    public static DataProjectOrganVo DataProjectOrganConvertVo(DataProjectOrgan dataProjectOrgan,boolean creator,SysLocalOrganInfo organInfo,Map<String,Object> organMap){
        DataProjectOrganVo dataProjectOrganVo = new DataProjectOrganVo();
        dataProjectOrganVo.setId(dataProjectOrgan.getId());
        dataProjectOrganVo.setProjectId(dataProjectOrgan.getProjectId());
        dataProjectOrganVo.setCreator(creator);
        dataProjectOrganVo.setThisInstitution(dataProjectOrgan.getOrganId().equals(organInfo.getOrganId()));
        dataProjectOrganVo.setParticipationIdentity(dataProjectOrgan.getParticipationIdentity());
        dataProjectOrganVo.setOrganId(dataProjectOrgan.getOrganId());
        dataProjectOrganVo.setOrganName(organMap==null?"":organMap.get("globalName")==null?"":organMap.get("globalName").toString());
        dataProjectOrganVo.setAuditStatus(dataProjectOrgan.getAuditStatus());
        dataProjectOrganVo.setAuditOpinion(dataProjectOrgan.getAuditOpinion());
        dataProjectOrganVo.setResources(new ArrayList<>());
        return dataProjectOrganVo;
    }

    public static DataProjectResourceVo DataProjectResourceConvertVo(DataProjectResource dataProjectResource, Map<String,Object> resourceMap){
        DataProjectResourceVo dataProjectResourceVo = new DataProjectResourceVo();
        dataProjectResourceVo.setId(dataProjectResource.getId());
        dataProjectResourceVo.setProjectId(dataProjectResource.getProjectId());
        dataProjectResourceVo.setParticipationIdentity(dataProjectResource.getParticipationIdentity());
        dataProjectResourceVo.setResourceId(dataProjectResource.getResourceId());
        dataProjectResourceVo.setResourceName(resourceMap==null?"":resourceMap.get("resourceName")==null?"":resourceMap.get("resourceName").toString());
        dataProjectResourceVo.setResourceTag(resourceMap==null?null:resourceMap.get("resourceTag")==null?new String[]{}:resourceMap.get("resourceTag").toString().split(","));
        dataProjectResourceVo.setResourceRowsCount(resourceMap==null?null:resourceMap.get("resourceRowsCount")==null?0:Integer.valueOf(resourceMap.get("resourceRowsCount").toString()));
        dataProjectResourceVo.setResourceColumnCount(resourceMap==null?null:resourceMap.get("resourceColumnCount")==null?0:Integer.valueOf(resourceMap.get("resourceColumnCount").toString()));
        dataProjectResourceVo.setResourceContainsY(resourceMap==null?null:resourceMap.get("resourceContainsY")==null?0:Integer.valueOf(resourceMap.get("resourceContainsY").toString()));
        dataProjectResourceVo.setResourceYRowsCount(resourceMap==null?null:resourceMap.get("resourceYRowsCount")==null?0:Integer.valueOf(resourceMap.get("resourceYRowsCount").toString()));
        dataProjectResourceVo.setResourceYRatio(resourceMap==null?null:resourceMap.get("resourceYRatio")==null?new BigDecimal(0):new BigDecimal(resourceMap.get("resourceYRatio").toString()));
        dataProjectResourceVo.setAuditStatus(dataProjectResource.getAuditStatus());
        dataProjectResourceVo.setAuditOpinion(dataProjectResource.getAuditOpinion());
        return dataProjectResourceVo;

    }



}
