package com.primihub.biz.service.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.po.FederatedLearning;
import com.primihub.biz.entity.data.req.FederatedLearningReq;
import com.primihub.biz.repository.primarydb.data.FederatedLearningPrRepository;
import com.primihub.biz.repository.secondarydb.data.FederatedLearningRepository;
import com.primihub.biz.service.sys.LogManagementService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FederatedLearningServiceTest {

    @Mock private LogManagementService logManagementService;
    @Mock private FederatedLearningRepository flRepo;
    @Mock private FederatedLearningPrRepository flPrRepo;
    @InjectMocks private FederatedLearningService flService;

    @Test
    void createTask_shouldFail_whenException() {
        doThrow(new RuntimeException("DB error"))
            .when(flPrRepo).saveFederatedLearning(any());

        FederatedLearningReq req = new FederatedLearningReq();

        BaseResultEntity result = flService.createTask(req, 42L);

        assertThat(result.getCode()).isEqualTo(-1);
    }
}
