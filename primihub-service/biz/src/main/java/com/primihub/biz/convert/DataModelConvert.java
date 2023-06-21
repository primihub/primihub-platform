package com.primihub.biz.convert;

import com.alibaba.fastjson.JSONArray;
import com.primihub.biz.entity.data.po.DataComponent;
import com.primihub.biz.entity.data.po.DataModel;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.req.ComponentDraftReq;
import com.primihub.biz.entity.data.vo.ComponentResourceVo;
import com.primihub.biz.entity.data.vo.DataComponentVo;
import com.primihub.biz.entity.data.vo.DataModelComponentVo;
import com.primihub.biz.entity.data.vo.ModelVo;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.entity.data.req.DataModelAndComponentReq;
import com.primihub.biz.entity.data.req.DataModelReq;
import com.primihub.biz.entity.data.vo.*;
import com.primihub.biz.entity.sys.po.SysOrgan;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
public class DataModelConvert {

    public static DataModel dataModelReqConvertPo(DataModelAndComponentReq req, Long userId){
        DataModel po = new DataModel();
        po.setModelId(StringUtils.isNotBlank(req.getModelId())?Long.parseLong(req.getModelId()):null);
        po.setModelDesc(req.getModelDesc());
        po.setIsDraft(req.getIsDraft());
        po.setTrainType(req.getTrainType());
        po.setUserId(userId);
        po.setResourceNum(0);
        return po;
    }

    public static DataComponent dataModelReqConvertDataComponentPo(DataComponentReq req){
        DataComponent dataComponent = new DataComponent();
        dataComponent.setFrontComponentId(req.getFrontComponentId());
        dataComponent.setComponentCode(req.getComponentCode());
        dataComponent.setComponentId(StringUtils.isNotBlank(req.getComponentId())?Long.parseLong(req.getComponentId()):null);
        dataComponent.setComponentName(req.getComponentName());
        dataComponent.setCoordinateX(req.getCoordinateX());
        dataComponent.setCoordinateY(req.getCoordinateY());
        dataComponent.setHeight(req.getHeight());
        dataComponent.setWidth(req.getWidth());
        dataComponent.setShape(req.getShape());
        dataComponent.setIsDel(0);
        dataComponent.setDataJson(JSONArray.toJSONString(req.getComponentValues()));
        return dataComponent;
    }

    public static DataComponentVo dataComponentPoConvertDataComponentVo(DataComponent dataComponent){
        DataComponentVo dataComponentVo = new DataComponentVo();
        dataComponentVo.setComponentId(dataComponent.getComponentId());
        dataComponentVo.setComponentCode(dataComponent.getComponentCode());
        dataComponentVo.setComponentName(dataComponent.getComponentName());
        dataComponentVo.setStartTime(dataComponent.getStartTime());
        dataComponentVo.setEndTime(dataComponent.getEndTime());
        dataComponentVo.setComponentState(dataComponent.getComponentState());
        return dataComponentVo;
    }

    public static DataModelComponentVo dataComponentPoConvertDataModelComponentVo(DataComponent dataComponent, Integer sumTime){
        DataModelComponentVo dataComponentVo = new DataModelComponentVo();
        dataComponentVo.setComponentId(dataComponent.getComponentId());
        dataComponentVo.setComponentCode(dataComponent.getComponentCode());
        dataComponentVo.setComponentName(dataComponent.getComponentName());
        dataComponentVo.setComponentState(dataComponent.getComponentState());
        dataComponentVo.setModelId(dataComponent.getModelId());
        dataComponentVo.setTimeConsuming(dataComponent.getTimeConsuming().intValue());
        if (dataComponentVo.getTimeConsuming()>0){
            dataComponentVo.setTimeRatio(dataComponentVo.getTimeRatio().add(new BigDecimal(dataComponentVo.getTimeConsuming())));
            dataComponentVo.setTimeRatio(dataComponentVo.getTimeRatio().divide(new BigDecimal(sumTime),6, RoundingMode.HALF_DOWN));
        }
        return dataComponentVo;
    }

    public static ModelProjectOrganVo projectOrganPoCovertProjectOrganVo(DataProjectOrgan organ, SysOrgan sysOrgan, ModelProjectResourceVo resourceVo, String sysLocalOrganId){
        ModelProjectOrganVo modelProjectOrganVo = new ModelProjectOrganVo();
        modelProjectOrganVo.setParticipationIdentity(organ.getParticipationIdentity());
        modelProjectOrganVo.setOrganId(organ.getOrganId());

        modelProjectOrganVo.setAuditStatus(organ.getAuditStatus());
        modelProjectOrganVo.setCreator(sysLocalOrganId.equals(organ.getOrganId()));
        modelProjectOrganVo.setOrganName(sysOrgan==null?"":sysOrgan.getOrganName());
        if (resourceVo!=null) {
            modelProjectOrganVo.getResources().add(resourceVo);
        }
        return modelProjectOrganVo;
    }


    public static ModelProjectResourceVo projectResourcePoCovertModelResourceVo(DataProjectResource resource,Map<String,Object> resourceMap){
        ModelProjectResourceVo modelProjectResourceVo = new ModelProjectResourceVo();
        modelProjectResourceVo.setOrganId(resource.getOrganId());
        modelProjectResourceVo.setResourceId(resource.getResourceId());
        modelProjectResourceVo.setResourceName(resourceMap==null?"":resourceMap.get("resourceName")==null?"":resourceMap.get("resourceName").toString());
        modelProjectResourceVo.setResourceRowsCount(resourceMap==null?null:resourceMap.get("resourceRowsCount")==null?0:Integer.valueOf(resourceMap.get("resourceRowsCount").toString()));
        modelProjectResourceVo.setResourceColumnCount(resourceMap==null?null:resourceMap.get("resourceColumnCount")==null?0:Integer.valueOf(resourceMap.get("resourceColumnCount").toString()));
        modelProjectResourceVo.setResourceContainsY(resourceMap==null?null:resourceMap.get("resourceContainsY")==null?0:Integer.valueOf(resourceMap.get("resourceContainsY").toString()));
        modelProjectResourceVo.setAuditStatus(resource.getAuditStatus());
        modelProjectResourceVo.setParticipationIdentity(resource.getParticipationIdentity());
        String resourceColumnNameList = resourceMap==null?null: ObjectUtils.isEmpty(resourceMap.get("resourceColumnNameList"))?null:resourceMap.get("resourceColumnNameList").toString();
        if (StringUtils.isNotBlank(resourceColumnNameList)){
            modelProjectResourceVo.setFileHandleField(Arrays.asList(resourceColumnNameList.split(",")));
            modelProjectResourceVo.setResourceField((List<DataFileFieldVo>)resourceMap.get("fieldList"));
        }
        return modelProjectResourceVo;
    }

    public static DataComponentDraft componentDraftReqCovertPo(ComponentDraftReq req){
        DataComponentDraft dataComponentDraft = new DataComponentDraft();
        dataComponentDraft.setDraftId(req.getDraftId());
        dataComponentDraft.setDraftName(req.getDraftName());
        dataComponentDraft.setUserId(req.getUserId());
        dataComponentDraft.setComponentJson(req.getComponentJson());
        dataComponentDraft.setComponentImage(req.getComponentImage());
        dataComponentDraft.setIsDel(0);
        return dataComponentDraft;

    }

}
