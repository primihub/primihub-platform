package com.primihub.biz.service.data;

import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PirServiceTest {

    @Mock private BaseConfiguration baseConfiguration;
    @Mock private OtherBusinessesService otherBusinessesService;
    @Mock private DataTaskPrRepository dataTaskPrRepository;
    @Mock private DataTaskRepository dataTaskRepository;
    @Mock private DataAsyncService dataAsyncService;
    @InjectMocks private PirService pirService;

    @Test
    void getResultFilePath_shouldBuildPath() {
        org.mockito.BDDMockito.given(baseConfiguration.getResultUrlDirPrefix()).willReturn("/data/results/");

        String result = pirService.getResultFilePath("task-001", "2026-05-10");

        assertThat(result).isEqualTo("/data/results/2026-05-10/task-001.csv");
    }
}
