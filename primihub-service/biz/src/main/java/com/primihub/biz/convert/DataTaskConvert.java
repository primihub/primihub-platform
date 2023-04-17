package com.primihub.biz.convert;

import com.primihub.biz.entity.data.po.DataPirTask;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.vo.DataModelTaskListVo;
import com.primihub.biz.entity.data.vo.DataPirTaskVo;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;

public class DataTaskConvert {

    public static DataModelTaskListVo dataTaskPoConvertDataModelTaskList(DataTask dataTask){
        DataModelTaskListVo dataModelTaskListVo = new DataModelTaskListVo();
        dataModelTaskListVo.setTaskId(dataTask.getTaskId());
        dataModelTaskListVo.setTaskIdName(dataTask.getTaskIdName());
        dataModelTaskListVo.setTaskName(dataTask.getTaskName());
        dataModelTaskListVo.setTaskDesc(dataTask.getTaskDesc());
        dataModelTaskListVo.setTaskState(dataTask.getTaskState());
        dataModelTaskListVo.setTaskType(dataTask.getTaskType());
        dataModelTaskListVo.setTaskStartTime(dataTask.getTaskStartTime());
        dataModelTaskListVo.setTaskEndTime(dataTask.getTaskEndTime());
        if (dataTask.getTaskStartTime()!=null) {
            dataModelTaskListVo.setTaskStartDate(new Date(dataTask.getTaskStartTime()));
        }
        if (dataTask.getTaskEndTime()!=null) {
            dataModelTaskListVo.setTaskEndDate(new Date(dataTask.getTaskEndTime()));
        }
        dataModelTaskListVo.setTaskErrorMsg(dataTask.getTaskErrorMsg());
        dataModelTaskListVo.setIsCooperation(dataTask.getIsCooperation());
        return dataModelTaskListVo;
    }

    public static void dataPirTaskPoConvertDataPirTaskVo(DataPirTaskVo pirTask, LinkedHashMap<String, Object> map){
        pirTask.setOrganId(getMapValue(map,"organId",""));
        pirTask.setResourceName(getMapValue(map,"resourceName",""));
        pirTask.setResourceRowsCount(Integer.valueOf((getMapValue(map,"resourceRowsCount","0"))));
        pirTask.setResourceColumnCount(Integer.valueOf((getMapValue(map,"resourceColumnCount","0"))));
        pirTask.setResourceContainsY(Integer.valueOf((getMapValue(map,"resourceContainsY","0"))));
        pirTask.setResourceYRowsCount(Integer.valueOf((getMapValue(map,"resourceYRowsCount","0"))));
        pirTask.setResourceYRatio(new BigDecimal((getMapValue(map,"resourceYRatio","0"))));
        pirTask.setAvailable(Integer.valueOf((getMapValue(map,"available","0"))));
        pirTask.setResourceState(Integer.valueOf((getMapValue(map,"resourceState","1"))));
    }

    public static String getMapValue(LinkedHashMap<String, Object> map,String key,String defaultVal){
        Object val = map.get(key);
        if (val==null) {
            return defaultVal;
        }
        return val.toString();
    }
}
