package com.primihub.simple.service;

import com.primihub.entity.DataSet;
import com.primihub.simple.base.BaseResultEntity;
import com.primihub.simple.base.BaseResultEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FrontDataSetService {
    @Autowired
    private DataSetService dataSetService;

    public BaseResultEntity one(DataSet dataSet) {
        try {
            dataSet.setHolder(1);
            dataSetService.saveDataSet(dataSet);
            return BaseResultEntity.success();
        }catch (Exception e){
            e.printStackTrace();
            return BaseResultEntity.failure(BaseResultEnum.FAILURE);
        }
    }

    public BaseResultEntity delete(DataSet dataSet) {
        try {
            dataSet.setHolder(1);
            dataSetService.deleteDataSet(dataSet);
            return BaseResultEntity.success();
        }catch (Exception e){
            e.printStackTrace();
            return BaseResultEntity.failure(BaseResultEnum.FAILURE);
        }
    }

    public BaseResultEntity many(List<DataSet> dataSets) {
        try {
            for (DataSet dataSet : dataSets) {
                dataSet.setHolder(1);
                dataSetService.saveDataSet(dataSet);
            }
            return BaseResultEntity.success();
        }catch (Exception e){
            e.printStackTrace();
            return BaseResultEntity.failure(BaseResultEnum.FAILURE);
        }
    }
}
