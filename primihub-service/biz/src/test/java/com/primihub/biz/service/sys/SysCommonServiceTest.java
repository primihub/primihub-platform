package com.primihub.biz.service.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.repository.primaryredis.sys.SysCommonPrimaryRedisRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SysCommonServiceTest {

    @Mock private SysCommonPrimaryRedisRepository commonRedisRepo;
    @Mock private RestTemplate restTemplate;
    @InjectMocks private SysCommonService commonService;

    @Test
    void getValidatePublicKey_shouldSucceed() {
        BaseResultEntity result = commonService.getValidatePublicKey();

        assertThat(result.getCode()).isZero();
    }

    @Test
    void getCollectList_shouldReturnNull_onError() {
        BaseResultEntity result = commonService.getCollectList();

        assertThat(result).isNull();
    }
}
