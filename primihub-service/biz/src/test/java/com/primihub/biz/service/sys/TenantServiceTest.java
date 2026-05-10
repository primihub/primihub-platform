package com.primihub.biz.service.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.repository.primarydb.sys.TenantPrimarydbRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TenantServiceTest {

    @Mock private com.primihub.biz.repository.primarydb.sys.TenantPrimarydbRepository tenantRepo;
    @InjectMocks private TenantService tenantService;

    @Test
    void findTenantPage_shouldReturnPaginated() {
        given(tenantRepo.selectTenantCount(anyMap())).willReturn(5);
        given(tenantRepo.selectTenantList(anyMap())).willReturn(Collections.singletonList(new com.primihub.biz.entity.sys.po.Tenant()));

        BaseResultEntity result = tenantService.findTenantPage(null, null, 1, 10);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void getTenantDetail_shouldReturnMap() {
        com.primihub.biz.entity.sys.po.Tenant tenant = new com.primihub.biz.entity.sys.po.Tenant();
        tenant.setId(1L);
        tenant.setTenantName("TestCorp");
        given(tenantRepo.selectTenantById(1L)).willReturn(tenant);

        BaseResultEntity result = tenantService.getTenantDetail(1L);

        assertThat(result.getCode()).isZero();
    }
}
