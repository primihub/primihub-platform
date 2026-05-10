package com.primihub.biz.service.sys;

import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.sys.po.OperationLog;
import com.primihub.biz.entity.sys.po.OperationLogDefinition;
import com.primihub.biz.entity.sys.po.ScheduleLog;
import com.primihub.biz.entity.sys.po.ScheduleLogDefinition;
import com.primihub.biz.repository.primarydb.sys.LogManagementPrimarydbRepository;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LogManagementServiceTest {

    @Mock private LogManagementPrimarydbRepository logRepo;
    @InjectMocks private LogManagementService logService;

    // === Operation Log Definition ===

    @Test
    void findOperationLogDefinitionPage_shouldReturnPaginated() {
        given(logRepo.selectOperationLogDefinitionCount(anyMap())).willReturn(10);
        given(logRepo.selectOperationLogDefinitionList(anyMap())).willReturn(Collections.singletonList(new OperationLogDefinition()));

        BaseResultEntity result = logService.findOperationLogDefinitionPage(null, null, null, null, 1, 10);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void addOperationLogDefinition_shouldSucceed() {
        OperationLogDefinition def = new OperationLogDefinition();
        def.setLogCode("test-code");
        def.setLogName("test log");

        BaseResultEntity result = logService.addOperationLogDefinition(def);

        assertThat(result.getCode()).isZero();
        verify(logRepo).insertOperationLogDefinition(def);
    }

    @Test
    void addOperationLogDefinition_shouldFail_whenMissingCode() {
        OperationLogDefinition def = new OperationLogDefinition();

        BaseResultEntity result = logService.addOperationLogDefinition(def);

        assertThat(result.getCode()).isEqualTo(100);
    }

    @Test
    void updateOperationLogDefinition_shouldSucceed() {
        OperationLogDefinition def = new OperationLogDefinition();
        def.setId(1L);

        BaseResultEntity result = logService.updateOperationLogDefinition(def);

        assertThat(result.getCode()).isZero();
        verify(logRepo).updateOperationLogDefinition(def);
    }

    @Test
    void deleteOperationLogDefinition_shouldSucceed() {
        BaseResultEntity result = logService.deleteOperationLogDefinition(1L);

        assertThat(result.getCode()).isZero();
        verify(logRepo).deleteOperationLogDefinition(1L);
    }

    @Test
    void updateOperationLogDefinitionStatus_shouldSucceed() {
        BaseResultEntity result = logService.updateOperationLogDefinitionStatus(1L, 1);

        assertThat(result.getCode()).isZero();
        verify(logRepo).updateOperationLogDefinitionStatus(1L, 1);
    }

    // === Schedule Log Definition ===

    @Test
    void findScheduleLogDefinitionPage_shouldReturnPaginated() {
        given(logRepo.selectScheduleLogDefinitionCount(anyMap())).willReturn(5);
        given(logRepo.selectScheduleLogDefinitionList(anyMap())).willReturn(Collections.singletonList(new ScheduleLogDefinition()));

        BaseResultEntity result = logService.findScheduleLogDefinitionPage(null, null, null, null, 1, 10);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void addScheduleLogDefinition_shouldSucceed() {
        ScheduleLogDefinition def = new ScheduleLogDefinition();
        def.setLogCode("sched-code");
        def.setLogName("sched log");

        BaseResultEntity result = logService.addScheduleLogDefinition(def);

        assertThat(result.getCode()).isZero();
        verify(logRepo).insertScheduleLogDefinition(def);
    }

    // === Operation Log Records ===

    @Test
    void findOperationLogPage_shouldReturnPaginated() {
        given(logRepo.selectOperationLogCount(anyMap())).willReturn(20);
        given(logRepo.selectOperationLogList(anyMap())).willReturn(Collections.singletonList(new OperationLog()));

        BaseResultEntity result = logService.findOperationLogPage(null, null, null, null, null, null, null, null, 1, 10);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void recordOperationLog_shouldCallInsert() {
        OperationLog log = new OperationLog();
        log.setLogCode("op");

        logService.recordOperationLog(log);

        verify(logRepo).insertOperationLog(log);
    }

    // === Schedule Log Records ===

    @Test
    void findScheduleLogPage_shouldReturnPaginated() {
        given(logRepo.selectScheduleLogCount(anyMap())).willReturn(5);
        given(logRepo.selectScheduleLogList(anyMap())).willReturn(Collections.singletonList(new ScheduleLog()));

        BaseResultEntity result = logService.findScheduleLogPage(null, null, null, null, null, null, 1, 10);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void recordScheduleLog_shouldCallInsert() {
        ScheduleLog log = new ScheduleLog();
        log.setLogCode("sched");

        logService.recordScheduleLog(log);

        verify(logRepo).insertScheduleLog(log);
    }

    // === Schedule ===

    @Test
    void deleteScheduleLogDefinition_shouldSucceed() {
        BaseResultEntity result = logService.deleteScheduleLogDefinition(1L);

        assertThat(result.getCode()).isZero();
        verify(logRepo).deleteScheduleLogDefinition(1L);
    }

    @Test
    void updateScheduleLogDefinition_shouldSucceed() {
        ScheduleLogDefinition def = new ScheduleLogDefinition();
        def.setId(1L);

        BaseResultEntity result = logService.updateScheduleLogDefinition(def);

        assertThat(result.getCode()).isZero();
        verify(logRepo).updateScheduleLogDefinition(def);
    }
}
