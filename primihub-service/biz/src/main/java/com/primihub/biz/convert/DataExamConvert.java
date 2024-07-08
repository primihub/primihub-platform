package com.primihub.biz.convert;

import com.primihub.biz.entity.data.po.DataExamTask;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.vo.DataExamTaskVo;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;

public class DataExamConvert {

    public static DataExamTask convertReqToPo(DataExamReq req, SysLocalOrganInfo localOrganInfo, DataResource dataResource) {
        DataExamTask po = new DataExamTask();
        po.setTaskId(req.getTaskId());
        po.setTaskName(req.getTaskName());
        po.setOriginResourceId(req.getResourceId());
        po.setOriginOrganId(localOrganInfo.getOrganId());
        po.setTargetOrganId(req.getTargetOrganId());
        po.setTargetField(req.getTargetField());
        return po;
    }

    public static DataExamTaskVo convertPoToVo(DataExamTask task) {
        DataExamTaskVo vo = new DataExamTaskVo();
        // todo
        return vo;
    }

    public static DataExamReq convertPoToReq(DataExamTask po) {
        DataExamReq req = new DataExamReq();
        req.setTaskId(po.getTaskId());
        req.setTaskState(po.getTaskState());
        req.setResourceId(po.getOriginResourceId());
        req.setTaskName(po.getTaskName());
        req.setTargetResourceId(po.getTargetResourceId());
        req.setOriginOrganId(po.getOriginOrganId());
        req.setTargetOrganId(po.getTargetOrganId());
        req.setTargetField(po.getTargetField());
        return req;
    }
}
