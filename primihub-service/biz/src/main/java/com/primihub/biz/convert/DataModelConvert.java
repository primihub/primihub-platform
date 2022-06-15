package com.primihub.biz.convert;

import com.alibaba.fastjson.JSONArray;
import com.primihub.biz.entity.data.po.DataComponent;
import com.primihub.biz.entity.data.po.DataModel;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.vo.ComponentResourceVo;
import com.primihub.biz.entity.data.vo.DataComponentVo;
import com.primihub.biz.entity.data.vo.DataModelComponentVo;
import com.primihub.biz.entity.data.vo.ModelVo;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.entity.data.req.DataModelAndComponentReq;
import com.primihub.biz.entity.data.req.DataModelReq;
import com.primihub.biz.entity.data.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
public class DataModelConvert {

    public static DataModel dataModelReqConvertPo(DataModelReq req, Long userId, Long organId){
        DataModel po = new DataModel();
        po.setModelName(req.getModelName());
        po.setModelDesc(req.getModelDesc());
        po.setModelType(req.getModelType());
        po.setProjectId(req.getProjectId());
        po.setYValueColumn(req.getYValueColumn());
        po.setResourceNum(0);
        return po;
    }

    public static DataModel dataModelReqConvertPo(DataModelAndComponentReq req, Long userId, Long organId){
        DataModel po = new DataModel();
        po.setModelId(StringUtils.isNotBlank(req.getModelId())?Long.parseLong(req.getModelId()):null);
//        po.setModelName(req.getModelName());
        po.setModelDesc(req.getModelDesc());
        po.setIsDraft(req.getIsDraft());
        po.setTrainType(req.getTrainType());
        po.setUserId(userId);
        po.setOrganId(organId);
        po.setResourceNum(0);
        return po;
    }
    public static ModelVo dataModelConvertVo(DataModel dataModel){
        ModelVo vo = new ModelVo();
        vo.setModelId(dataModel.getModelId());
        vo.setCreateDate(dataModel.getCreateDate());
        vo.setModelType(dataModel.getModelType());
        vo.setModelDesc(dataModel.getModelDesc());
        vo.setModelName(dataModel.getModelName());
        return vo;
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

    public static ComponentResourceVo resourceRecordVoConvertComponentResourceVo(DataResource dataResource){
        // TODO y字段后期补齐
        ComponentResourceVo vo= new ComponentResourceVo(dataResource.getFileHandleField());
        vo.setResourceId(dataResource.getResourceId());
        vo.setResourceName(dataResource.getResourceName());
        return vo;
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
//            log.info("{} / {} = {}",dataComponentVo.getTimeConsuming(),sumTime,dataComponentVo.getTimeRatio());
        }
        return dataComponentVo;
    }

}
