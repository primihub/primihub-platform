package com.primihub.biz.service.data.component;

import com.primihub.biz.convert.DataModelConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.BaseResultEnum;
import com.primihub.biz.entity.data.po.DataModel;
import com.primihub.biz.entity.data.po.DataModelComponent;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentRelationReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.entity.data.req.DataComponentValue;
import com.primihub.biz.entity.data.vo.InputValue;
import com.primihub.biz.entity.data.vo.ModelComponent;
import com.primihub.biz.entity.data.vo.ModelComponentType;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Component
 */
public interface ComponentTaskService {
    BaseResultEntity check(DataComponentReq req, ComponentTaskReq taskReq);
    BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq);
}
