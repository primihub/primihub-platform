package com.primihub.biz.service.data;

import com.primihub.biz.config.base.BaseConfiguration;
import com.primihub.biz.config.base.OrganConfiguration;
import com.primihub.biz.entity.base.BaseResultEntity;
import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.req.DataTaskReq;
import com.primihub.biz.entity.data.req.PageReq;
import com.primihub.biz.entity.data.po.DataFileField;
import com.primihub.biz.entity.data.po.DataResource;
import com.primihub.biz.entity.data.vo.DataTaskVo;
import com.primihub.biz.entity.sys.config.LokiConfig;
import com.primihub.biz.repository.primarydb.data.DataPsiPrRepository;
import com.primihub.biz.repository.primarydb.data.DataTaskPrRepository;
import com.primihub.biz.repository.primarydb.data.*;
import com.primihub.biz.repository.secondarydb.data.*;
import com.primihub.biz.repository.secondarydb.sys.SysOrganSecondarydbRepository;
import com.primihub.biz.service.sys.SysSseEmitterService;
import com.primihub.biz.service.sys.SysUserService;
import com.primihub.biz.service.sys.SysWebSocketService;
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
import static org.mockito.Mockito.*;

import com.primihub.biz.entity.data.po.DataModelTask;
import com.primihub.biz.entity.data.req.PageReq;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DataTaskServiceTest {

    @Mock private DataTaskRepository dataTaskRepository;
    @Mock private DataTaskPrRepository dataTaskPrRepository;
    @Mock private DataResourcePrRepository dataResourcePrRepository;
    @Mock private DataResourceRepository dataResourceRepository;
    @Mock private BaseConfiguration baseConfiguration;
    @Mock private OrganConfiguration organConfiguration;
    @Mock private DataCopyPrimarydbRepository dataCopyPrimarydbRepository;
    @Mock private DataCopyService dataCopyService;
    @Mock private DataProjectRepository dataProjectRepository;
    @Mock private DataProjectPrRepository dataProjectPrRepository;
    @Mock private DataModelRepository dataModelRepository;
    @Mock private SysSseEmitterService sseEmitterService;
    @Mock private SysWebSocketService webSocketService;
    @Mock private DataAsyncService dataAsyncService;
    @Mock private OtherBusinessesService otherBusinessesService;
    @Mock private SysOrganSecondarydbRepository sysOrganSecondarydbRepository;
    @Mock private DataPsiRepository dataPsiRepository;
    @Mock private DataPsiPrRepository dataPsiPrRepository;
    @InjectMocks private DataTaskService dataTaskService;

    @Test
    void getTaskData_shouldReturnTask() {
        DataTask task = new DataTask();
        task.setTaskId(1L);
        task.setTaskIdName("tid1");
        task.setTaskName("test task");
        task.setTaskState(1);
        task.setTaskType(1);
        given(dataTaskRepository.selectDataTaskByTaskId(1L)).willReturn(task);

        BaseResultEntity result = dataTaskService.getTaskData(1L);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void getTaskData_shouldFail_whenNotFound() {
        given(dataTaskRepository.selectDataTaskByTaskId(99L)).willReturn(null);

        BaseResultEntity result = dataTaskService.getTaskData(99L);

        assertThat(result.getCode()).isEqualTo(1003);
    }

    @Test
    void deleteTaskData_shouldDeleteModelTask() {
        DataTask task = new DataTask();
        task.setTaskId(1L);
        task.setTaskState(3);
        task.setTaskType(TaskTypeEnum.MODEL.getTaskType());
        given(dataTaskRepository.selectDataTaskByTaskId(1L)).willReturn(task);

        BaseResultEntity result = dataTaskService.deleteTaskData(1L);

        assertThat(result.getCode()).isZero();
        verify(dataAsyncService).deleteModelTask(task);
        verify(dataTaskPrRepository).updateDataTask(task);
        assertThat(task.getTaskState()).isEqualTo(TaskStateEnum.DELETE.getStateType());
    }

    @Test
    void deleteTaskData_shouldDeleteNonModelTask() {
        DataTask task = new DataTask();
        task.setTaskId(1L);
        task.setTaskState(3);
        task.setTaskType(TaskTypeEnum.PSI.getTaskType());
        given(dataTaskRepository.selectDataTaskByTaskId(1L)).willReturn(task);

        BaseResultEntity result = dataTaskService.deleteTaskData(1L);

        assertThat(result.getCode()).isZero();
        verify(dataTaskPrRepository).deleteDataTask(1L);
    }

    @Test
    void deleteTaskData_shouldFail_whenRunning() {
        DataTask task = new DataTask();
        task.setTaskState(2);
        given(dataTaskRepository.selectDataTaskByTaskId(1L)).willReturn(task);

        BaseResultEntity result = dataTaskService.deleteTaskData(1L);

        assertThat(result.getCode()).isEqualTo(1006);
    }

    @Test
    void deleteTaskData_shouldFail_whenNotFound() {
        given(dataTaskRepository.selectDataTaskByTaskId(99L)).willReturn(null);

        BaseResultEntity result = dataTaskService.deleteTaskData(99L);

        assertThat(result.getCode()).isEqualTo(1006);
    }

    @Test
    void getTaskList_shouldReturnPaginated() {
        DataTaskVo vo = new DataTaskVo();
        vo.setTaskId(1L);
        vo.setTaskName("task1");
        given(dataTaskRepository.selectDataTaskList(any())).willReturn(Collections.singletonList(vo));
        given(dataTaskRepository.selectDataTaskListCount(any())).willReturn(1);

        DataTaskReq req = new DataTaskReq();
        req.setPageNo(1);
        req.setPageSize(10);

        BaseResultEntity result = dataTaskService.getTaskList(req);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void getTaskList_shouldReturnEmpty_whenNoData() {
        given(dataTaskRepository.selectDataTaskList(any())).willReturn(Collections.emptyList());

        DataTaskReq req = new DataTaskReq();
        BaseResultEntity result = dataTaskService.getTaskList(req);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void updateTaskDesc_shouldUpdate() {
        DataTask task = new DataTask();
        task.setTaskId(1L);
        task.setTaskDesc("old desc");
        given(dataTaskRepository.selectDataTaskByTaskId(1L)).willReturn(task);

        BaseResultEntity result = dataTaskService.updateTaskDesc(1L, "new desc");

        assertThat(result.getCode()).isZero();
        assertThat(task.getTaskDesc()).isEqualTo("new desc");
        verify(dataTaskPrRepository).updateDataTask(task);
    }

    @Test
    void updateTaskDesc_shouldFail_whenNotFound() {
        given(dataTaskRepository.selectDataTaskByTaskId(99L)).willReturn(null);

        BaseResultEntity result = dataTaskService.updateTaskDesc(99L, "desc");

        assertThat(result.getCode()).isEqualTo(1002);
    }

    @Test
    void getTaskLogInfo_shouldReturnWithStartTime() {
        LokiConfig lokiConfig = new LokiConfig();
        lokiConfig.setAddress("loki:3100");
        lokiConfig.setJob("test-job");
        given(baseConfiguration.getLokiConfig()).willReturn(lokiConfig);

        DataTask task = new DataTask();
        task.setTaskId(1L);
        task.setTaskIdName("tid1");
        task.setTaskStartTime(1000L);
        given(dataTaskRepository.selectDataTaskByTaskId(1L)).willReturn(task);

        BaseResultEntity result = dataTaskService.getTaskLogInfo(1L);

        assertThat(result.getCode()).isZero();
        Map<String, Object> data = (Map<String, Object>) result.getResult();
        assertThat(data.get("taskIdName")).isEqualTo("tid1");
    }

    @Test
    void getTaskLogInfo_shouldFail_whenNoLokiConfig() {
        given(baseConfiguration.getLokiConfig()).willReturn(null);

        BaseResultEntity result = dataTaskService.getTaskLogInfo(1L);

        assertThat(result.getCode()).isEqualTo(100);
    }

    @Test
    void getModelTaskList_shouldReturnPaginated() {
        given(dataModelRepository.queryModelTaskByModelId(anyMap())).willReturn(Collections.emptyList());

        PageReq req = new PageReq();
        BaseResultEntity result = dataTaskService.getModelTaskList(1L, req);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void batchInsertDataFileField_shouldHandleFieldNames() {
        DataResource resource = new DataResource();
        resource.setResourceId(1L);
        resource.setFileId(100L);
        resource.setFileHandleField("col1,col2");

        List<DataFileField> result = dataTaskService.batchInsertDataFileField(resource);

        assertThat(result).hasSize(2);
        verify(dataResourcePrRepository).saveResourceFileFieldBatch(anyList());
    }

    @Test
    void batchInsertDataFileField_shouldSkip_whenBlankHandleField() {
        DataResource resource = new DataResource();
        resource.setFileHandleField("");

        List<DataFileField> result = dataTaskService.batchInsertDataFileField(resource);

        assertThat(result).isEmpty();
    }

    @Test
    void cancelTask_shouldCancelRunningTask() {
        DataTask task = new DataTask();
        task.setTaskId(1L);
        task.setTaskIdName("tid1");
        task.setTaskState(TaskStateEnum.IN_OPERATION.getStateType());
        task.setTaskType(TaskTypeEnum.PSI.getTaskType());
        given(dataTaskRepository.selectDataTaskByTaskIdName("tid1")).willReturn(task);

        com.primihub.sdk.task.TaskHelper taskHelper = mock(com.primihub.sdk.task.TaskHelper.class);
        given(dataAsyncService.getTaskHelper()).willReturn(taskHelper);
        com.primihub.sdk.task.param.TaskParam taskParam = new com.primihub.sdk.task.param.TaskParam();
        taskParam.setSuccess(true);
        given(taskHelper.killTask("tid1")).willReturn(taskParam);

        given(dataPsiRepository.selectPsiTaskByTaskId("tid1")).willReturn(new com.primihub.biz.entity.data.po.DataPsiTask());
        doNothing().when(dataPsiPrRepository).updateDataPsiTask(any());

        BaseResultEntity result = dataTaskService.cancelTask("tid1");

        assertThat(result.getCode()).isZero();
    }

    @Test
    void cancelTask_shouldFail_whenNotRunning() {
        DataTask task = new DataTask();
        task.setTaskId(1L);
        task.setTaskState(TaskStateEnum.SUCCESS.getStateType());
        given(dataTaskRepository.selectDataTaskByTaskIdName("tid1")).willReturn(task);

        BaseResultEntity result = dataTaskService.cancelTask("tid1");

        assertThat(result.getCode()).isEqualTo(1002);
    }

    @Test
    void getModelTaskList_shouldReturnData() {
        com.primihub.biz.entity.data.po.DataModelTask modelTask = new com.primihub.biz.entity.data.po.DataModelTask();
        modelTask.setTaskId(1L);
        given(dataModelRepository.queryModelTaskByModelId(anyMap())).willReturn(Collections.singletonList(modelTask));
        given(dataModelRepository.queryModelTaskByModelIdCount(anyLong())).willReturn(1);

        DataTask taskData = new DataTask();
        taskData.setTaskId(1L);
        taskData.setTaskIdName("tid1");
        given(dataTaskRepository.selectDataTaskByTaskIds(anySet())).willReturn(Collections.singletonList(taskData));

        com.primihub.biz.entity.data.req.PageReq req = new com.primihub.biz.entity.data.req.PageReq();
        req.setPageNo(1);
        req.setPageSize(10);

        BaseResultEntity result = dataTaskService.getModelTaskList(1L, req);

        assertThat(result.getCode()).isZero();
    }

    @Test
    void getDataTaskById_shouldQueryByModel_whenModelIdGiven() {
        com.primihub.biz.entity.data.po.DataModelTask modelTask = new com.primihub.biz.entity.data.po.DataModelTask();
        modelTask.setTaskId(5L);
        given(dataModelRepository.queryModelTaskByModelId(anyMap())).willReturn(Collections.singletonList(modelTask));
        DataTask task = new DataTask();
        task.setTaskId(5L);
        given(dataTaskRepository.selectDataTaskByTaskId(5L)).willReturn(task);

        DataTask result = dataTaskService.getDataTaskById(null, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getTaskId()).isEqualTo(5L);
    }

    @Test
    void deleteTaskData_shouldDeletePsiTask() {
        DataTask task = new DataTask();
        task.setTaskId(1L);
        task.setTaskState(3);
        task.setTaskType(TaskTypeEnum.PSI.getTaskType());
        given(dataTaskRepository.selectDataTaskByTaskId(1L)).willReturn(task);

        BaseResultEntity result = dataTaskService.deleteTaskData(1L);

        assertThat(result.getCode()).isZero();
        verify(dataTaskPrRepository).deleteDataTask(1L);
    }
}
