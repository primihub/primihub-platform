package com.primihub.biz.entity;

import com.primihub.biz.entity.data.dataenum.ModelStateEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModelStateEnumTest {

    @Test
    void shouldHaveAllStates() {
        assertThat(ModelStateEnum.values()).hasSize(2);
    }

    @Test
    void draft_shouldHaveTypeZero() {
        assertThat(ModelStateEnum.DRAFT.getStateType()).isZero();
        assertThat(ModelStateEnum.DRAFT.getStateDesc()).isEqualTo("草稿");
    }

    @Test
    void save_shouldHaveTypeOne() {
        assertThat(ModelStateEnum.SAVE.getStateType()).isEqualTo(1);
        assertThat(ModelStateEnum.SAVE.getStateDesc()).isEqualTo("保存");
    }

    @Test
    void modelStateMap_shouldContainAll() {
        assertThat(ModelStateEnum.MODEL_STATE_MAP).hasSize(2);
        assertThat(ModelStateEnum.MODEL_STATE_MAP.get(0)).isEqualTo(ModelStateEnum.DRAFT);
        assertThat(ModelStateEnum.MODEL_STATE_MAP.get(1)).isEqualTo(ModelStateEnum.SAVE);
    }
}
