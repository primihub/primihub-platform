package com.primihub.biz.service.data;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.req.DataReasoningReq;
import com.primihub.biz.entity.data.req.ReasoningListReq;
import com.primihub.biz.repository.secondarydb.data.DataReasoningRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.data.DataPsiRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import com.primihub.biz.repository.primarydb.data.DataPsiPrRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataReasoningServiceTest {

    @Mock private DataReasoningRepository reasoningRepo;
    @Mock private DataResourceRepository resourceRepo;
    @Mock private DataPsiRepository psiRepo;
    @Mock private DataPsiPrRepository psiPrRepo;
    @Mock private DataTaskRepository taskRepo;
    @Mock private com.primihub.biz.config.base.BaseConfiguration baseConfiguration;
    @InjectMocks private DataReasoningService service;

    @Test
    void getReasoningList_shouldReturnList() {
        given(reasoningRepo.selectDataReasoninPage(any())).willReturn(java.util.Collections.emptyList());
        given(reasoningRepo.selectDataReasoninCount(any())).willReturn(0);

        BaseResultEntity result = service.getReasoningList(new ReasoningListReq());

        assertThat(result.getCode()).isZero();
    }

    @Test
    void getReasoning_shouldFail_whenNotFound() {
        given(reasoningRepo.selectDataReasoninById(anyLong())).willReturn(null);

        BaseResultEntity result = service.getReasoning(99L);

        assertThat(result.getCode()).isEqualTo(1003);
    }
}
