package com.primihub.service;

import com.primihub.entity.DataSet;

import java.util.List;
import java.util.Set;

public interface StorageService {
    void saveDataSet(DataSet dataSet);
    void updateDataSet(DataSet dataSet);
    void deleteDataSet(DataSet dataSet);
    List<DataSet> getAll();
    List<DataSet> getByIds(Set<String> ids);
    void saveBatch(List<DataSet> list);
}
