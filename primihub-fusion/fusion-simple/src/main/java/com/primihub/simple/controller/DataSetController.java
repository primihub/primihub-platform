package com.primihub.simple.controller;

import com.primihub.entity.DataSet;
import com.primihub.simple.base.BaseResultEntity;
import com.primihub.simple.base.BaseResultEnum;
import com.primihub.simple.constant.SysConstant;
import com.primihub.simple.service.FrontDataSetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DataSetController {
    @Autowired
    private FrontDataSetService frontDataSetService;

    @PostMapping(SysConstant.ONE_URL)
    public BaseResultEntity one(@RequestBody DataSet dataSet){
        if (dataSet == null || dataSet.getId()==null){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM);
        }
        return frontDataSetService.one(dataSet);
    }

    @PostMapping(SysConstant.MANY_URL)
    public BaseResultEntity many(@RequestBody List<DataSet> dataSets){
        if (dataSets==null || dataSets.size()==0){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM);
        }
        return frontDataSetService.many(dataSets);
    }

    @PostMapping(SysConstant.DELETE_URL)
    public BaseResultEntity delete(@RequestBody DataSet dataSet){
        if (dataSet==null || dataSet.getId()==null || "".equals(dataSet.getId())){
            return BaseResultEntity.failure(BaseResultEnum.LACK_OF_PARAM);
        }
        return frontDataSetService.delete(dataSet);
    }

    @GetMapping(SysConstant.HEALTH_URL)
    public BaseResultEntity health(){
        return BaseResultEntity.success("success");
    }
}
