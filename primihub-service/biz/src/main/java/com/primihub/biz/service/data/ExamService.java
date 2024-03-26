package com.primihub.biz.service.data;


import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.convert.DataExamConvert;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.base.PageDataEntity;
import com.primihub.biz.entity.data.po.DataExamTask;
import com.primihub.biz.entity.data.req.DataExamReq;
import com.primihub.biz.entity.data.req.DataExamTaskReq;
import com.primihub.biz.entity.data.vo.DataPirTaskVo;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExamService {

    @Autowired
    private OrganConfiguration organConfiguration;

    @Autowired
    private DataTaskPrRepository dataTaskPrRepository;

    public BaseResultEntity<PageDataEntity<DataPirTaskVo>> getExamTaskList(DataExamTaskReq req) {
        // todo
        return null;
    }

    public BaseResultEntity submitExamTask(DataExamReq param) {
        // 先本地保存
        DataExamTask task = saveExamTask(param);
        // 发送给对方机构
        sendExamTask(param);
        return null;
    }

    private DataExamTask saveExamTask(DataExamReq param) {
        DataExamTask task = DataExamConvert.convertReqToPo(param, organConfiguration.getSysLocalOrganInfo()) ;
        dataTaskPrRepository.saveDataExamTask(task);
        return task;
    }
}
