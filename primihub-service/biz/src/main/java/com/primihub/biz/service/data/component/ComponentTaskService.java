package com.primihub.biz.service.data.component;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;

/**
 * Component
 */
public interface ComponentTaskService {
    BaseResultEntity check(DataComponentReq req, ComponentTaskReq taskReq);
    BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq);
}
