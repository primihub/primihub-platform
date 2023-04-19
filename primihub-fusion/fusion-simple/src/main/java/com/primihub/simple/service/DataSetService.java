package com.primihub.simple.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.primihub.entity.DataSet;
import com.primihub.service.StorageService;
import com.primihub.simple.repository.DataSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataSetService implements StorageService {
    @Autowired
    private DataSetRepository dataSetRepository;
    @Autowired
    private AsyncService asyncService;

    @Override
    public void saveDataSet(DataSet dataSet) {
//        dataSetRepository.save(datSet);
        DataSet byId = getById(dataSet.getId());
        if (byId!=null){
            dataSetRepository.updateById(dataSet);
        }else {
            dataSetRepository.insert(dataSet);
        }
        asyncService.syncOne(dataSet);
    }

    @Override
    public void updateDataSet(DataSet dataSet) {
        dataSetRepository.updateById(dataSet);
        asyncService.syncOne(dataSet);
    }

    @Override
    public void deleteDataSet(String id) {
//        dataSetRepository.removeById(id);
        dataSetRepository.deleteById(id);
        asyncService.syncDelete(id);
    }

    @Override
    public List<DataSet> getAll() {
        QueryWrapper<DataSet> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("holder", 0);
//        return dataSetRepository.list(queryWrapper);
        return dataSetRepository.selectList(queryWrapper);
    }

    @Override
    public List<DataSet> getByIds(List<String> ids) {
        return dataSetRepository.selectBatchIds(ids);
//        return dataSetRepository.listByIds(ids);
    }

    @Override
    public void saveBatch(List<DataSet> list) {
        for (DataSet dataSet : list) {
            DataSet byId = getById(dataSet.getId());
            if (byId!=null){
                dataSetRepository.updateById(dataSet);
            }else {
                dataSetRepository.insert(dataSet);
            }
        }
//        dataSetRepository.saveBatch(list);
    }

    public DataSet getById(String id){
        return dataSetRepository.selectById(id);
    }
}
