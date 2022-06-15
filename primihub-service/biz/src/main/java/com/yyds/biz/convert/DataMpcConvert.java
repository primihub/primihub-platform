package com.yyds.biz.convert;

import com.yyds.biz.entity.data.po.DataScript;
import com.yyds.biz.entity.data.req.DataScriptReq;
import com.yyds.biz.entity.data.vo.DataScriptVo;

public class DataMpcConvert {

    public static DataScript dataScriptReqConvertPo(DataScriptReq req,Long userId,Long organId){
        DataScript dataScript = new DataScript();
        dataScript.setScriptId(req.getScriptId());
        dataScript.setName(req.getName());
        dataScript.setCatalogue(req.getCatalogue());
        dataScript.setPScriptId(req.getPScriptId());
        dataScript.setScriptType(req.getScriptType());
        dataScript.setScriptStatus(req.getScriptStatus());
        dataScript.setScriptContent(req.getScriptContent());
        dataScript.setUserId(userId);
        dataScript.setOrganId(organId);
        return dataScript;
    }

    public static DataScriptVo dataScriptPoConvertVo(DataScript dataScript){
        DataScriptVo dataScriptVo = new DataScriptVo();
        dataScriptVo.setScriptId(dataScript.getScriptId());
        dataScriptVo.setName(dataScript.getName());
        dataScriptVo.setCatalogue(dataScript.getCatalogue());
        dataScriptVo.setPScriptId(dataScript.getPScriptId());
        dataScriptVo.setScriptType(dataScript.getScriptType());
        dataScriptVo.setScriptStatus(dataScript.getScriptStatus());
        dataScriptVo.setScriptContent(dataScript.getScriptContent());
        return dataScriptVo;
    }
}
