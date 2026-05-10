package com.primihub.biz.entity;

import com.primihub.biz.entity.data.dataenum.TaskTypeEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskTypeEnumTest {

    @Test
    void shouldHaveAllTypes() {
        assertThat(TaskTypeEnum.values()).hasSize(8);
    }

    @Test
    void values_shouldBeInOrder() {
        assertThat(TaskTypeEnum.MODEL.getTaskType()).isEqualTo(1);
        assertThat(TaskTypeEnum.MODEL.getTaskName()).isEqualTo("模型");
        assertThat(TaskTypeEnum.PSI.getTaskType()).isEqualTo(2);
        assertThat(TaskTypeEnum.PIR.getTaskType()).isEqualTo(3);
        assertThat(TaskTypeEnum.FEDERATED_LEARNING.getTaskType()).isEqualTo(8);
    }

    @Test
    void taskTypeMap_shouldContainAll() {
        assertThat(TaskTypeEnum.TASK_TYPE_MAP).hasSize(8);
        assertThat(TaskTypeEnum.TASK_TYPE_MAP.get(2)).isEqualTo(TaskTypeEnum.PSI);
        assertThat(TaskTypeEnum.TASK_TYPE_MAP.get(5)).isEqualTo(TaskTypeEnum.JOINT_STATISTICAL);
    }
}
