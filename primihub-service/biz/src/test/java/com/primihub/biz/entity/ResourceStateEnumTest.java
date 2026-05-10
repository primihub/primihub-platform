package com.primihub.biz.entity;

import com.primihub.biz.entity.data.dataenum.ResourceStateEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceStateEnumTest {

    @Test
    void shouldHaveAllStates() {
        assertThat(ResourceStateEnum.values()).hasSize(2);
    }

    @Test
    void available_shouldHaveTypeZero() {
        assertThat(ResourceStateEnum.AVAILABLE.getStateType()).isZero();
        assertThat(ResourceStateEnum.AVAILABLE.getStateDesc()).isEqualTo("上线");
    }

    @Test
    void notAvailable_shouldHaveTypeOne() {
        assertThat(ResourceStateEnum.NOT_AVAILABLE.getStateType()).isEqualTo(1);
        assertThat(ResourceStateEnum.NOT_AVAILABLE.getStateDesc()).isEqualTo("下线");
    }

    @Test
    void resourceStateMap_shouldContainAll() {
        assertThat(ResourceStateEnum.RESOURCE_STATE_MAP).hasSize(2);
        assertThat(ResourceStateEnum.RESOURCE_STATE_MAP.get(0)).isEqualTo(ResourceStateEnum.AVAILABLE);
    }
}
