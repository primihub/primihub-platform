package com.primihub.biz.entity;

import com.primihub.biz.entity.data.dataenum.TaskStateEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskStateEnumTest {

    @Test
    void shouldHaveAllStates() {
        assertThat(TaskStateEnum.values()).hasSize(6);
    }

    @Test
    void init_shouldHaveTypeZero() {
        assertThat(TaskStateEnum.INIT.getStateType()).isZero();
        assertThat(TaskStateEnum.INIT.getStateDesc()).isEqualTo("初始未开始");
    }

    @Test
    void success_shouldHaveTypeOne() {
        assertThat(TaskStateEnum.SUCCESS.getStateType()).isEqualTo(1);
        assertThat(TaskStateEnum.SUCCESS.getStateDesc()).isEqualTo("成功");
    }

    @Test
    void inOperation_shouldHaveTypeTwo() {
        assertThat(TaskStateEnum.IN_OPERATION.getStateType()).isEqualTo(2);
        assertThat(TaskStateEnum.IN_OPERATION.getStateDesc()).isEqualTo("运行中");
    }

    @Test
    void fail_shouldHaveTypeThree() {
        assertThat(TaskStateEnum.FAIL.getStateType()).isEqualTo(3);
        assertThat(TaskStateEnum.FAIL.getStateDesc()).isEqualTo("失败");
    }

    @Test
    void cancel_shouldHaveTypeFour() {
        assertThat(TaskStateEnum.CANCEL.getStateType()).isEqualTo(4);
        assertThat(TaskStateEnum.CANCEL.getStateDesc()).isEqualTo("取消");
    }

    @Test
    void delete_shouldHaveTypeFive() {
        assertThat(TaskStateEnum.DELETE.getStateType()).isEqualTo(5);
        assertThat(TaskStateEnum.DELETE.getStateDesc()).isEqualTo("删除");
    }

    @Test
    void taskStateMap_shouldContainAll() {
        assertThat(TaskStateEnum.TASK_STATE_MAP).hasSize(6);
        assertThat(TaskStateEnum.TASK_STATE_MAP.get(0)).isEqualTo(TaskStateEnum.INIT);
        assertThat(TaskStateEnum.TASK_STATE_MAP.get(1)).isEqualTo(TaskStateEnum.SUCCESS);
        assertThat(TaskStateEnum.TASK_STATE_MAP.get(3)).isEqualTo(TaskStateEnum.FAIL);
    }
}
