package com.primihub.service;

import com.primihub.entity.DataSet;
import com.primihub.repository.DataSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class GrpcDataSetService implements StorageService{
    @Autowired
    private DataSetRepository dataSetRepository;
    @Autowired
    private AsyncService asyncService;

    @Override
    public void saveDataSet(DataSet dataSet) {
        DataSet d = dataSetRepository.getDataSetById(dataSet.getId());
        if (d==null){
            dataSetRepository.insertDataSet(dataSet);
        }else {
            dataSetRepository.updateDataSet(dataSet);
            asyncService.noticeResource(d,dataSet);
        }
    }

    @Override
    public void updateDataSet(DataSet dataSet) {
        DataSet d = dataSetRepository.getDataSetById(dataSet.getId());
        dataSetRepository.updateDataSet(dataSet);
        asyncService.noticeResource(d,dataSet);
    }

    @Override
    public void deleteDataSet(DataSet dataSet) {
        dataSetRepository.deleteDataSet(dataSet);
    }

    @Override
    public List<DataSet> getAll() {
        return dataSetRepository.getDataSetOwnAll();
    }

    @Override
    public List<DataSet> getByIds(Set<String> ids) {
        return dataSetRepository.getDataSetByIds(ids);
    }

    @Override
    public void saveBatch(List<DataSet> list) {
        dataSetRepository.insertBatchDataSet(list);
    }
}
