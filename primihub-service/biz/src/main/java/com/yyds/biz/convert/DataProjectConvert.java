package com.yyds.biz.convert;

import com.yyds.biz.entity.data.po.DataProject;
import com.yyds.biz.entity.data.req.DataProjectReq;
import com.yyds.biz.entity.data.vo.DataProjectListVo;
import com.yyds.biz.entity.data.vo.DataProjectVo;
import com.yyds.biz.entity.sys.po.SysUser;
import org.apache.commons.lang.StringUtils;

public class DataProjectConvert {

    public static DataProject dataDataProjectReqConvertPo(DataProjectReq req,Long userId,Long organId){
        DataProject po = new DataProject();
        po.setProjectName(req.getProjectName());
        po.setProjectDesc(req.getProjectDesc());
        po.setUserId(userId);
        po.setOrganId(organId);
        po.setOrganNum(0);
        po.setResourceNum(req.getResources().size());
        po.setAuthResourceNum(0);
        po.setResourceOrganIds("");
        return po;
    }

    public static DataProjectListVo dataDataProjectPoConvertListVo(DataProject po, SysUser user,Integer modelNum){
        DataProjectListVo vo = new DataProjectListVo();
        vo.setProjectId(po.getProjectId());
        vo.setProjectName(po.getProjectName());
        vo.setProjectDesc(po.getProjectDesc());
        vo.setOrganId(po.getOrganId());
        vo.setOrganNum(po.getOrganNum());
        vo.setResourceNum(po.getResourceNum());
        if (StringUtils.isNotBlank(po.getResourceOrganIds())){
            vo.setResourceOrganNum(po.getResourceOrganIds().split(",").length);
        }else {
            vo.setResourceOrganNum(0);
        }
        vo.setUserId(po.getUserId());
        if (user!=null){
            vo.setUserName(user.getUserName());
        }
        vo.setCreateDate(po.getCreateDate());
        vo.setAuthResourceNum(po.getAuthResourceNum());
        if (modelNum==null){
            vo.setModelNum(0);
        }else{
            vo.setModelNum(modelNum);
        }
        return vo;
    }

    public static DataProjectVo dataDataProjectPoConvertVo(DataProject po){
        DataProjectVo vo = new DataProjectVo();
        vo.setCreateDate(po.getCreateDate());
        vo.setProjectDesc(po.getProjectDesc());
        vo.setProjectId(po.getProjectId());
        vo.setProjectName(po.getProjectName());
        return vo;
    }
}
