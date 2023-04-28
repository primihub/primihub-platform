package com.primihub.service;

import com.primihub.entity.DataSet;
import com.primihub.repository.DataSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GrpcDataSetService implements StorageService{
    @Autowired
    private DataSetRepository dataSetRepository;

    @Override
    public void saveDataSet(DataSet dataSet) {
        dataSetRepository.insertDataSet(dataSet);
    }

    @Override
    public void updateDataSet(DataSet dataSet) {
        dataSetRepository.updateDataSet(dataSet);
    }

    @Override
    public void deleteDataSet(DataSet dataSet) {
        dataSetRepository.deleteDataSet(dataSet);
    }

    @Override
    public List<DataSet> getAll() {
        return getByIds(null);
    }

    @Override
    public List<DataSet> getByIds(List<String> ids) {
        return dataSetRepository.getDataSetByIds(ids);
    }

    @Override
    public void saveBatch(List<DataSet> list) {
        dataSetRepository.insertBatchDataSet(list);
    }
}
