package com.primihub.biz.service.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.req.SinglePartyReq;
import com.primihub.biz.repository.primarydb.data.SinglePartyPrRepository;
import com.primihub.biz.repository.secondarydb.data.SinglePartyRepository;
import com.primihub.biz.service.sys.LogManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SinglePartyServiceTest {

    @Mock private LogManagementService logManagementService;
    @Mock private SinglePartyRepository repo;
    @Mock private SinglePartyPrRepository prRepo;
    @InjectMocks private SinglePartyService service;

    @Test
    void createTask_shouldFail_whenException() {
        doThrow(new RuntimeException("error")).when(prRepo).saveSingleParty(any());

        BaseResultEntity result = service.createTask(new SinglePartyReq(), 1L);

        assertThat(result.getCode()).isEqualTo(-1);
    }

    @Test
    void getTaskList_shouldSucceed() {
        BaseResultEntity result = service.getTaskList(null, null, null, null, null, null, 1, 10);

        assertThat(result.getCode()).isZero();
    }
}
