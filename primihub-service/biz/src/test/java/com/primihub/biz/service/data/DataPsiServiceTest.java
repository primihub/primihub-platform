package com.primihub.biz.service.data;

import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.entity.sys.po.SysLocalOrganInfo;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.po.*;
import com.primihub.biz.entity.data.req.DataPsiReq;
import com.primihub.biz.repository.primarydb.data.DataPsiPrRepository;
import com.primihub.biz.repository.secondarydb.data.DataPsiRepository;
import com.primihub.biz.repository.secondarydb.data.DataResourceRepository;
import com.primihub.biz.repository.secondarydb.data.DataTaskRepository;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class DataPsiServiceTest {

    @Mock private DataResourceRepository dataResourceRepository;
    @Mock private DataPsiRepository dataPsiRepository;
    @Mock private DataPsiPrRepository dataPsiPrRepository;
    @Mock private DataTaskRepository dataTaskRepository;
    @Mock private DataResourceService dataResourceService;
    @Mock private OtherBusinessesService otherBusinessesService;
    @Mock private DataAsyncService dataAsyncService;
    @Mock private OrganConfiguration organConfiguration;
    @Mock private SysOrganSecondarydbRepository sysOrganSecondarydbRepository;
    @InjectMocks private DataPsiService dataPsiService;

    @Test
    void saveDataPsi_shouldCreatePsiAndTask() {
        DataPsiReq req = new DataPsiReq();
        req.setOwnOrganId("organ1");
        req.setOwnResourceId("res1");
        req.setOwnKeyword("key1");
        req.setOtherOrganId("organ2");
        req.setOtherResourceId("res2");
        req.setOtherKeyword("key2");
        req.setOutputContent(0);
        req.setResultOrganIds("organ1,organ2");

            doAnswer(i -> {
            DataPsi psi = i.getArgument(0);
            psi.setId(100L);
            return null;
        }).when(dataPsiPrRepository).saveDataPsi(any());

        BaseResultEntity result = dataPsiService.saveDataPsi(req, 42L);

        assertThat(result.getCode()).isZero();
        verify(dataPsiPrRepository).saveDataPsiTask(any());
        verify(dataAsyncService).psiGrpcRun(any(), any(), any());
    }

    @Test
    void getPsiTaskDetails_shouldReturnDetails() {
        DataPsiTask task = new DataPsiTask();
        task.setId(1L);
        task.setPsiId(100L);
        task.setTaskId("tid1");
        given(dataPsiRepository.selectPsiTaskById(1L)).willReturn(task);

        DataPsi dataPsi = new DataPsi();
        dataPsi.setId(100L);
        dataPsi.setOwnOrganId("o1");
        dataPsi.setOwnResourceId("r1");
        dataPsi.setOtherOrganId("o2");
        dataPsi.setOtherResourceId("r2");
        dataPsi.setResultOrganIds("o1,o2");
        given(dataPsiRepository.selectPsiById(100L)).willReturn(dataPsi);

        DataTask dataTask = new DataTask();
        dataTask.setTaskId(1L);
        dataTask.setTaskIdName("tid1");
        given(dataTaskRepository.selectDataTaskByTaskIdName("tid1")).willReturn(dataTask);

        DataResource dataResource = new DataResource();
        dataResource.setResourceName("testRes");
        given(dataResourceRepository.queryDataResourceByResourceFusionId("r1")).willReturn(dataResource);
        given(organConfiguration.getSysLocalOrganId()).willReturn("o2");

        given(otherBusinessesService.getDataResource("r2")).willReturn(BaseResultEntity.success(new java.util.LinkedHashMap<>()));
        SysLocalOrganInfo localInfo = new SysLocalOrganInfo();
        localInfo.setOrganName("MyOrgan");
        localInfo.setOrganId("myOrgan");
        given(organConfiguration.getSysLocalOrganInfo()).willReturn(localInfo);
        given(organConfiguration.getSysLocalOrganId()).willReturn("myOrgan");

        BaseResultEntity result = dataPsiService.getPsiTaskDetails(1L);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void getPsiTaskDetails_shouldFail_whenTaskNotFound() {
        given(dataPsiRepository.selectPsiTaskById(99L)).willReturn(null);

        BaseResultEntity result = dataPsiService.getPsiTaskDetails(99L);

        assertThat(result.getCode()).isEqualTo(1003);
    }

    @Test
    void delPsiTask_shouldDelete_whenNotRunning() {
        DataPsiTask task = new DataPsiTask();
        task.setId(1L);
        task.setPsiId(100L);
        task.setTaskState(3);
        given(dataPsiRepository.selectPsiTaskById(1L)).willReturn(task);

        BaseResultEntity result = dataPsiService.delPsiTask(1L);

        assertThat(result.getCode()).isZero();
        verify(dataPsiPrRepository).delPsiTask(1L);
        verify(dataPsiPrRepository).delPsi(100L);
    }

    @Test
    void delPsiTask_shouldFail_whenRunning() {
        DataPsiTask task = new DataPsiTask();
        task.setTaskState(2);
        given(dataPsiRepository.selectPsiTaskById(1L)).willReturn(task);

        BaseResultEntity result = dataPsiService.delPsiTask(1L);

        assertThat(result.getCode()).isEqualTo(1006);
    }

    @Test
    void cancelPsiTask_shouldSetStateToCancelled() {
        DataPsiTask task = new DataPsiTask();
        task.setId(1L);
        given(dataPsiRepository.selectPsiTaskById(1L)).willReturn(task);

        BaseResultEntity result = dataPsiService.cancelPsiTask(1L);

        assertThat(result.getCode()).isZero();
        verify(dataPsiPrRepository).updateDataPsiTask(task);
        assertThat(task.getTaskState()).isEqualTo(4);
    }

    @Test
    void retryPsiTask_shouldRetry_whenFailed() {
        DataPsiTask task = new DataPsiTask();
        task.setId(1L);
        task.setPsiId(100L);
        task.setTaskState(3);
        given(dataPsiRepository.selectPsiTaskById(1L)).willReturn(task);
        given(dataPsiRepository.selectPsiById(100L)).willReturn(new DataPsi());

        BaseResultEntity result = dataPsiService.retryPsiTask(1L);

        assertThat(result.getCode()).isZero();
        verify(dataAsyncService).psiGrpcRun(any(), any(), any());
    }

    @Test
    void retryPsiTask_shouldFail_whenAlreadyRunningOrSuccess() {
        DataPsiTask task = new DataPsiTask();
        task.setTaskState(1);
        given(dataPsiRepository.selectPsiTaskById(1L)).willReturn(task);

        BaseResultEntity result = dataPsiService.retryPsiTask(1L);

        assertThat(result.getCode()).isEqualTo(1007);
    }

    @Test
    void updateDataPsiResultName_shouldUpdate() {
        DataPsi dataPsi = new DataPsi();
        dataPsi.setId(100L);
        dataPsi.setResultName("old");
        given(dataPsiRepository.selectPsiById(100L)).willReturn(dataPsi);

        DataPsiReq req = new DataPsiReq();
        req.setId(100L);
        req.setResultName("new name");

        BaseResultEntity result = dataPsiService.updateDataPsiResultName(req);

        assertThat(result.getCode()).isZero();
        assertThat(dataPsi.getResultName()).isEqualTo("new name");
        verify(dataPsiPrRepository).updateDataPsi(dataPsi);
    }

    @Test
    void updateDataPsiResultName_shouldFail_whenNotFound() {
        given(dataPsiRepository.selectPsiById(99L)).willReturn(null);

        DataPsiReq req = new DataPsiReq();
        req.setId(99L);

        BaseResultEntity result = dataPsiService.updateDataPsiResultName(req);

        assertThat(result.getCode()).isEqualTo(1003);
    }
}
