package com.primihub.biz.service.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.sys.po.Whitelist;
import com.primihub.biz.entity.sys.po.WhitelistAccessLog;
import com.primihub.biz.entity.sys.po.WhitelistConfig;
import com.primihub.biz.repository.primarydb.sys.WhitelistPrimarydbRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WhitelistServiceTest {

    @Mock private WhitelistPrimarydbRepository whitelistRepo;
    @InjectMocks private WhitelistService whitelistService;

    @Test
    void addWhitelist_shouldSucceed() {
        Whitelist wl = new Whitelist();
        wl.setType("ip");
        wl.setValue("192.168.1.1");

        BaseResultEntity result = whitelistService.addWhitelist(wl);

        assertThat(result.getCode()).isZero();
        verify(whitelistRepo).insertWhitelist(wl);
    }

    @Test
    void addWhitelist_shouldSetDefaultStatus() {
        Whitelist wl = new Whitelist();
        wl.setType("ip");
        wl.setValue("10.0.0.1");
        wl.setStatus(null);

        whitelistService.addWhitelist(wl);

        assertThat(wl.getStatus()).isEqualTo(1);
    }

    @Test
    void addWhitelist_shouldFail_whenMissingType() {
        Whitelist wl = new Whitelist();
        wl.setValue("test");

        BaseResultEntity result = whitelistService.addWhitelist(wl);

        assertThat(result.getCode()).isEqualTo(100);
    }

    @Test
    void addWhitelist_shouldFail_whenMissingValue() {
        Whitelist wl = new Whitelist();
        wl.setType("ip");

        BaseResultEntity result = whitelistService.addWhitelist(wl);

        assertThat(result.getCode()).isEqualTo(100);
    }

    @Test
    void updateWhitelist_shouldSucceed() {
        Whitelist wl = new Whitelist();
        wl.setId(1L);
        wl.setValue("new-value");

        BaseResultEntity result = whitelistService.updateWhitelist(wl);

        assertThat(result.getCode()).isZero();
        verify(whitelistRepo).updateWhitelist(wl);
    }

    @Test
    void updateWhitelist_shouldFail_whenMissingId() {
        Whitelist wl = new Whitelist();

        BaseResultEntity result = whitelistService.updateWhitelist(wl);

        assertThat(result.getCode()).isEqualTo(100);
    }

    @Test
    void deleteWhitelist_shouldSucceed() {
        BaseResultEntity result = whitelistService.deleteWhitelist(1L);

        assertThat(result.getCode()).isZero();
        verify(whitelistRepo).deleteWhitelist(1L);
    }

    @Test
    void deleteWhitelist_shouldFail_whenNullId() {
        BaseResultEntity result = whitelistService.deleteWhitelist(null);

        assertThat(result.getCode()).isEqualTo(100);
    }

    @Test
    void getWhitelistDetail_shouldReturnWhitelist() {
        Whitelist wl = new Whitelist();
        wl.setId(1L);
        wl.setValue("10.0.0.1");
        given(whitelistRepo.selectWhitelistById(1L)).willReturn(wl);

        BaseResultEntity result = whitelistService.getWhitelistDetail(1L);

        assertThat(result.getCode()).isZero();
        assertThat(((Whitelist) result.getResult()).getValue()).isEqualTo("10.0.0.1");
    }

    @Test
    void getWhitelistDetail_shouldFail_whenNullId() {
        BaseResultEntity result = whitelistService.getWhitelistDetail(null);

        assertThat(result.getCode()).isEqualTo(100);
    }

    @Test
    void findWhitelistPage_shouldReturnPaginatedResults() {
        given(whitelistRepo.selectWhitelistCount(anyMap())).willReturn(25);
        List<Whitelist> mockList = Collections.singletonList(new Whitelist());
        given(whitelistRepo.selectWhitelistList(anyMap())).willReturn(mockList);

        BaseResultEntity result = whitelistService.findWhitelistPage(null, null, null, 1, 10);

        assertThat(result.getCode()).isZero();
        Map<String, Object> data = (Map<String, Object>) result.getResult();
        assertThat(data.get("list")).isEqualTo(mockList);
    }

    @Test
    void findWhitelistAccessLogPage_shouldReturnPaginatedResults() {
        given(whitelistRepo.selectWhitelistAccessLogCount(anyMap())).willReturn(5);
        List<WhitelistAccessLog> mockList = Collections.singletonList(new WhitelistAccessLog());
        given(whitelistRepo.selectWhitelistAccessLogList(anyMap())).willReturn(mockList);

        BaseResultEntity result = whitelistService.findWhitelistAccessLogPage(null, null, null, null, null, 1, 10);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void saveWhitelistConfig_shouldSucceed() {
        WhitelistConfig config = new WhitelistConfig();
        config.setConfigKey("key1");
        config.setConfigValue("val1");

        BaseResultEntity result = whitelistService.saveWhitelistConfig(Collections.singletonList(config));

        assertThat(result.getCode()).isZero();
        verify(whitelistRepo).insertOrUpdateWhitelistConfig(config);
    }

    @Test
    void saveWhitelistConfig_shouldFail_whenEmpty() {
        BaseResultEntity result = whitelistService.saveWhitelistConfig(Collections.emptyList());

        assertThat(result.getCode()).isEqualTo(100);
    }

    @Test
    void getWhitelistConfigDetail_shouldReturnConfig() {
        WhitelistConfig config = new WhitelistConfig();
        config.setConfigKey("test-key");
        given(whitelistRepo.selectWhitelistConfigByKey("test-key")).willReturn(config);

        BaseResultEntity result = whitelistService.getWhitelistConfigDetail("test-key");

        assertThat(result.getCode()).isZero();
    }

    @Test
    void getWhitelistConfigDetail_shouldFail_whenNullKey() {
        BaseResultEntity result = whitelistService.getWhitelistConfigDetail(null);

        assertThat(result.getCode()).isEqualTo(100);
    }

    @Test
    void findWhitelistConfigList_shouldReturnConfigs() {
        given(whitelistRepo.selectWhitelistConfigList()).willReturn(Collections.emptyList());
        given(whitelistRepo.selectWhitelistConfigHistory()).willReturn(Collections.emptyList());

        BaseResultEntity result = whitelistService.findWhitelistConfigList();

        assertThat(result.getCode()).isZero();
    }

    @Test
    void recordAccessLog_shouldCallInsert() {
        WhitelistAccessLog log = new WhitelistAccessLog();
        log.setAccessIp("10.0.0.1");

        whitelistService.recordAccessLog(log);

        verify(whitelistRepo).insertWhitelistAccessLog(log);
    }

    @Test
    void batchDeleteAccessLog_shouldSucceed() {
        BaseResultEntity result = whitelistService.batchDeleteAccessLog(Arrays.asList(1L, 2L, 3L));

        assertThat(result.getCode()).isZero();
        verify(whitelistRepo).batchDeleteAccessLog(Arrays.asList(1L, 2L, 3L));
    }

    @Test
    void batchDeleteAccessLog_shouldFail_whenNull() {
        BaseResultEntity result = whitelistService.batchDeleteAccessLog(null);

        assertThat(result.getCode()).isEqualTo(100);
    }

    @Test
    void cleanExpiredLogs_shouldSucceed() {
        given(whitelistRepo.deleteExpiredLogs(anyString())).willReturn(10);

        BaseResultEntity result = whitelistService.cleanExpiredLogs(30);

        assertThat(result.getCode()).isZero();
        Map<String, Object> data = (Map<String, Object>) result.getResult();
        assertThat(data.get("deletedCount")).isEqualTo(10);
    }

    @Test
    void cleanExpiredLogs_shouldFail_whenDaysInvalid() {
        BaseResultEntity result = whitelistService.cleanExpiredLogs(0);

        assertThat(result.getCode()).isEqualTo(100);
    }

    @Test
    void getWhitelistAccessLogDetail_shouldReturnLog() {
        WhitelistAccessLog log = new WhitelistAccessLog();
        log.setId(1L);
        given(whitelistRepo.selectWhitelistAccessLogById(1L)).willReturn(log);

        BaseResultEntity result = whitelistService.getWhitelistAccessLogDetail(1L);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void getAccessTrend_shouldUseDefaultDays() {
        given(whitelistRepo.selectAccessTrend(7)).willReturn(Collections.emptyList());

        BaseResultEntity result = whitelistService.getAccessTrend(null);

        assertThat(result.getCode()).isZero();
        Map<String, Object> data = (Map<String, Object>) result.getResult();
        assertThat(data.get("days")).isEqualTo(7);
    }

    @Test
    void getTopAccessIps_shouldUseDefaultLimit() {
        given(whitelistRepo.selectTopAccessIps(10)).willReturn(Collections.emptyList());

        BaseResultEntity result = whitelistService.getTopAccessIps(null);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void exportAccessLog_shouldReturnList() {
        given(whitelistRepo.exportAccessLogList(anyMap())).willReturn(Collections.emptyList());

        BaseResultEntity result = whitelistService.exportAccessLog(null, null, null, null, null);

        assertThat(result.getCode()).isZero();
    }
}
