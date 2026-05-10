package com.primihub.biz.entity;

import com.primihub.biz.entity.data.dataenum.DataResourceAuthType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataResourceAuthTypeTest {

    @Test
    void shouldHaveAllTypes() {
        assertThat(DataResourceAuthType.values()).hasSize(3);
    }

    @Test
    void public_shouldHaveTypeOne() {
        assertThat(DataResourceAuthType.PUBLIC.getAuthType()).isEqualTo(1);
        assertThat(DataResourceAuthType.PUBLIC.getDesc()).isEqualTo("公开");
    }

    @Test
    void private_shouldHaveTypeTwo() {
        assertThat(DataResourceAuthType.PRIVATE.getAuthType()).isEqualTo(2);
        assertThat(DataResourceAuthType.PRIVATE.getDesc()).isEqualTo("私有");
    }

    @Test
    void assign_shouldHaveTypeThree() {
        assertThat(DataResourceAuthType.ASSIGN.getAuthType()).isEqualTo(3);
        assertThat(DataResourceAuthType.ASSIGN.getDesc()).isEqualTo("指定机构");
    }

    @Test
    void authTypeMap_shouldContainAll() {
        assertThat(DataResourceAuthType.AUTH_TYPE_MAP).hasSize(3);
        assertThat(DataResourceAuthType.AUTH_TYPE_MAP.get(1)).isEqualTo(DataResourceAuthType.PUBLIC);
        assertThat(DataResourceAuthType.AUTH_TYPE_MAP.get(3)).isEqualTo(DataResourceAuthType.ASSIGN);
    }
}
