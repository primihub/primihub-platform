package com.primihub.biz.service.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.req.DataDifferenceReq;
import com.primihub.biz.service.sys.LogManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class DataDifferenceServiceTest {

    @Mock private LogManagementService logManagementService;
    @InjectMocks private DataDifferenceService service;

    @Test
    void saveDataDifference_shouldSucceed() {
        DataDifferenceReq req = new DataDifferenceReq();
        req.setResultName("test-diff");

        BaseResultEntity result = service.saveDataDifference(req, 1L);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void getDifferenceTaskList_shouldSucceed() {
        BaseResultEntity result = service.getDifferenceTaskList(null, null, null, null, null, 1, 10);

        assertThat(result.getCode()).isZero();
    }
}
