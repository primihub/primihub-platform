package com.primihub.service;

import com.primihub.entity.DataSet;

import java.util.List;

public interface StorageService {
    void saveDataSet(DataSet dataSet);
    void updateDataSet(DataSet dataSet);
    void deleteDataSet(DataSet dataSet);
    List<DataSet> getAll();
    List<DataSet> getByIds(List<String> ids);
    void saveBatch(List<DataSet> list);
}
