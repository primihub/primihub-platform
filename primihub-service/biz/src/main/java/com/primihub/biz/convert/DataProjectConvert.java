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
import com.primihub.biz.entity.sys.po.SysOrgan;
import org.apache.commons.lang.StringUtils;

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
        dataProject.setStatus(0);
        return dataProject;
    }

    public static DataProjectListVo dataProjectConvertListVo(DataProject dataProject, List<Map<String, Object>> list,Integer status){
        DataProjectListVo dataProjectListVo = new DataProjectListVo();
        dataProjectListVo.setId(dataProject.getId());
        dataProjectListVo.setOrganId(dataProject.getCreatedOrganId());
        dataProjectListVo.setProjectId(dataProject.getProjectId());
        dataProjectListVo.setProjectName(dataProject.getProjectName());
        dataProjectListVo.setProjectDesc(dataProject.getProjectDesc());
        dataProjectListVo.setUserName(dataProject.getCreatedUsername());
        dataProjectListVo.setCreatedOrganName(dataProject.getCreatedOrganName());
        dataProjectListVo.setResourceNum(dataProject.getResourceNum());
        dataProjectListVo.setProviderOrganNames(dataProject.getProviderOrganNames());
        dataProjectListVo.setStatus(status==null||status==0?dataProject.getStatus():status);
        dataProjectListVo.setCreateDate(dataProject.getCreateDate());
        dataProjectListVo.setUpdateDate(dataProject.getUpdateDate());
        if (list!=null){
            for (Map<String, Object> objectMap : list) {
                Object latestTaskStatus = objectMap.get("latestTaskStatus");
                if (latestTaskStatus!=null && StringUtils.isNotBlank(latestTaskStatus.toString())){
                    if ("1".equals(latestTaskStatus.toString())){
                        dataProjectListVo.setTaskSuccessNum(objectMap.get("statusCount")==null?0:Integer.valueOf(objectMap.get("statusCount").toString()));
                    }
                    if ("2".equals(latestTaskStatus.toString())){
                        dataProjectListVo.setTaskRunNum(objectMap.get("statusCount")==null?0:Integer.valueOf(objectMap.get("statusCount").toString()));
                    }
                    if ("3".equals(latestTaskStatus.toString())){
                        dataProjectListVo.setTaskFailNum(objectMap.get("statusCount")==null?0:Integer.valueOf(objectMap.get("statusCount").toString()));
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
        return dataProjectDetailsVo;
    }

    public static DataProjectOrganVo DataProjectOrganConvertVo(DataProjectOrgan dataProjectOrgan, boolean creator, SysLocalOrganInfo organInfo, SysOrgan organ){
        DataProjectOrganVo dataProjectOrganVo = new DataProjectOrganVo();
        dataProjectOrganVo.setId(dataProjectOrgan.getId());
        dataProjectOrganVo.setProjectId(dataProjectOrgan.getProjectId());
        dataProjectOrganVo.setCreator(creator);
        dataProjectOrganVo.setThisInstitution(dataProjectOrgan.getOrganId().equals(organInfo.getOrganId()));
        dataProjectOrganVo.setParticipationIdentity(dataProjectOrgan.getParticipationIdentity());
        dataProjectOrganVo.setOrganId(dataProjectOrgan.getOrganId());
        dataProjectOrganVo.setOrganName(organ==null?"":organ.getOrganName());
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
        dataProjectResourceVo.setResourceTag(resourceMap==null?new ArrayList<>():resourceMap.get("resourceTag")==null?new ArrayList<>():(List<String>) resourceMap.get("resourceTag"));
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
