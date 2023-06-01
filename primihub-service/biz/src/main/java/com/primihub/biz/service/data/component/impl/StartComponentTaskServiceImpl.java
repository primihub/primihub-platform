package com.primihub.biz.service.data.component.impl;

import com.primihub.biz.config.base.ComponentsConfiguration;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.req.ComponentTaskReq;
import com.primihub.biz.entity.data.req.DataComponentReq;
import com.primihub.biz.service.data.component.ComponentTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service("startComponentTaskServiceImpl")
@Slf4j
public class StartComponentTaskServiceImpl extends BaseComponentServiceImpl implements ComponentTaskService {


    @Autowired
    private ComponentsConfiguration componentsConfiguration;

    @Override
    public BaseResultEntity check(DataComponentReq req,  ComponentTaskReq taskReq) {
        Map<String, String> componentVals = getComponentVals(req.getComponentValues());
        DataTask dataTask = new DataTask();
        if (componentVals.containsKey("taskName")){
            dataTask.setTaskName(componentVals.get("taskName"));
        }
        if (componentVals.containsKey("taskDesc")){
            dataTask.setTaskDesc(componentVals.get("taskDesc"));
        }
        taskReq.setDataTask(dataTask);
        return componentTypeVerification(req,componentsConfiguration.getModelComponents(),taskReq);
    }

    @Override
    public BaseResultEntity runTask(DataComponentReq req, ComponentTaskReq taskReq) {
        return BaseResultEntity.success();
    }
}
