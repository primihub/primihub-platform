package com.primihub.biz.convert;

import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.vo.DataModelTaskListVo;

import java.util.Date;

public class DataTaskConvert {

    public static DataModelTaskListVo dataTaskPoConvertDataModelTaskList(DataTask dataTask){
        DataModelTaskListVo dataModelTaskListVo = new DataModelTaskListVo();
        dataModelTaskListVo.setTaskId(dataTask.getTaskId());
        dataModelTaskListVo.setTaskIdName(dataTask.getTaskIdName());
        dataModelTaskListVo.setTaskName(dataTask.getTaskName());
        dataModelTaskListVo.setTaskState(dataTask.getTaskState());
        dataModelTaskListVo.setTaskType(dataTask.getTaskType());
        dataModelTaskListVo.setTaskStartTime(dataTask.getTaskStartTime());
        dataModelTaskListVo.setTaskEndTime(dataTask.getTaskEndTime());
        dataModelTaskListVo.setTaskStartDate(new Date(dataTask.getTaskStartTime()));
        dataModelTaskListVo.setTaskEndDate(new Date(dataTask.getTaskEndTime()));
        dataModelTaskListVo.setTaskErrorMsg(dataTask.getTaskErrorMsg());
        return dataModelTaskListVo;

    }
}
