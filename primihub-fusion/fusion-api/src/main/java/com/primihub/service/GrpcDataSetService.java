package com.primihub.service;

import com.primihub.entity.DataSet;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrpcDataSetService implements StorageService{
    @Override
    public void saveDataSet(DataSet dataSet) {

    }

    @Override
    public void updateDataSet(DataSet dataSet) {

    }

    @Override
    public void deleteDataSet(DataSet dataSet) {

    }

    @Override
    public List<DataSet> getAll() {
        return null;
    }

    @Override
    public List<DataSet> getByIds(List<String> ids) {
        return null;
    }

    @Override
    public void saveBatch(List<DataSet> list) {

    }
}
