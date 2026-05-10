package com.primihub.biz.convert;

import com.primihub.biz.entity.data.po.DataTask;
import com.primihub.biz.entity.data.vo.DataModelTaskListVo;
import com.primihub.biz.entity.data.vo.DataPirTaskVo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.LinkedHashMap;

import static org.assertj.core.api.Assertions.assertThat;

class DataTaskConvertTest {

    @Test
    void dataTaskPoConvertDataModelTaskList_shouldMapAllFields() {
        DataTask task = new DataTask();
        task.setTaskId(1L);
        task.setTaskIdName("tid1");
        task.setTaskName("test task");
        task.setTaskDesc("desc");
        task.setTaskState(1);
        task.setTaskType(2);
        task.setTaskStartTime(1000L);
        task.setTaskEndTime(2000L);
        task.setTaskErrorMsg("error");
        task.setIsCooperation(0);

        DataModelTaskListVo result = DataTaskConvert.dataTaskPoConvertDataModelTaskList(task);

        assertThat(result.getTaskId()).isEqualTo(1L);
        assertThat(result.getTaskIdName()).isEqualTo("tid1");
        assertThat(result.getTaskName()).isEqualTo("test task");
        assertThat(result.getTaskState()).isEqualTo(1);
        assertThat(result.getTaskType()).isEqualTo(2);
        assertThat(result.getTaskStartTime()).isEqualTo(1000L);
        assertThat(result.getTaskEndTime()).isEqualTo(2000L);
        assertThat(result.getTaskErrorMsg()).isEqualTo("error");
        assertThat(result.getIsCooperation()).isEqualTo(0);
        assertThat(result.getTaskStartDate()).isNotNull();
        assertThat(result.getTaskEndDate()).isNotNull();
    }

    @Test
    void dataTaskPoConvertDataModelTaskList_shouldHandleNullTimestamps() {
        DataTask task = new DataTask();
        task.setTaskId(1L);
        task.setTaskName("test");

        DataModelTaskListVo result = DataTaskConvert.dataTaskPoConvertDataModelTaskList(task);

        assertThat(result.getTaskStartDate()).isNull();
        assertThat(result.getTaskEndDate()).isNull();
    }

    @Test
    void dataPirTaskPoConvertDataPirTaskVo_shouldMapFromLinkedHashMap() {
        DataPirTaskVo vo = new DataPirTaskVo();
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("organId", "organ1");
        map.put("resourceName", "testRes");
        map.put("resourceRowsCount", "500");
        map.put("resourceColumnCount", "20");
        map.put("resourceContainsY", "1");
        map.put("resourceYRowsCount", "200");
        map.put("resourceYRatio", "0.4");
        map.put("available", "1");
        map.put("resourceState", "0");

        DataTaskConvert.dataPirTaskPoConvertDataPirTaskVo(vo, map);

        assertThat(vo.getOrganId()).isEqualTo("organ1");
        assertThat(vo.getResourceName()).isEqualTo("testRes");
        assertThat(vo.getResourceRowsCount()).isEqualTo(500);
        assertThat(vo.getResourceColumnCount()).isEqualTo(20);
        assertThat(vo.getResourceContainsY()).isEqualTo(1);
        assertThat(vo.getResourceYRowsCount()).isEqualTo(200);
        assertThat(vo.getResourceYRatio()).isEqualByComparingTo(new BigDecimal("0.4"));
        assertThat(vo.getAvailable()).isEqualTo(1);
        assertThat(vo.getResourceState()).isEqualTo(0);
    }

    @Test
    void dataPirTaskPoConvertDataPirTaskVo_shouldUseDefaults_whenMapMissing() {
        DataPirTaskVo vo = new DataPirTaskVo();
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        DataTaskConvert.dataPirTaskPoConvertDataPirTaskVo(vo, map);

        assertThat(vo.getOrganId()).isEqualTo("");
        assertThat(vo.getResourceName()).isEqualTo("");
        assertThat(vo.getResourceRowsCount()).isZero();
        assertThat(vo.getResourceColumnCount()).isZero();
    }

    @Test
    void getMapValue_shouldReturnDefault_whenKeyMissing() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("name", "test");

        String result = DataTaskConvert.getMapValue(map, "missing", "defaultVal");

        assertThat(result).isEqualTo("defaultVal");
    }

    @Test
    void getMapValue_shouldReturnValue_whenKeyExists() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("name", "test");

        String result = DataTaskConvert.getMapValue(map, "name", "default");

        assertThat(result).isEqualTo("test");
    }
}
