package com.primihub.biz.entity;

import com.primihub.biz.entity.data.dataenum.DataFusionCopyEnum;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataFusionCopyEnumTest {

    @Test
    void shouldHaveAllTypes() {
        assertThat(DataFusionCopyEnum.values()).hasSize(2);
    }

    @Test
    void resource_shouldHaveTableName() {
        assertThat(DataFusionCopyEnum.RESOURCE.getTableName()).isEqualTo("data_resource");
        assertThat(DataFusionCopyEnum.RESOURCE.getBeanName()).isEqualTo("dataResourceService");
        assertThat(DataFusionCopyEnum.RESOURCE.getFunctionName()).isEqualTo("findCopyResourceList");
    }

    @Test
    void fusionResource_shouldHaveTableName() {
        assertThat(DataFusionCopyEnum.FUSION_RESOURCE.getTableName()).isEqualTo("fusion_resource");
        assertThat(DataFusionCopyEnum.FUSION_RESOURCE.getBeanName()).isEqualTo("dataResourceService");
        assertThat(DataFusionCopyEnum.FUSION_RESOURCE.getFunctionName()).isEqualTo("findFusionCopyResourceList");
    }

    @Test
    void fusionCopyMap_shouldContainAll() {
        assertThat(DataFusionCopyEnum.FUSION_COPY_MAP).hasSize(2);
        assertThat(DataFusionCopyEnum.FUSION_COPY_MAP.get("data_resource")).isEqualTo(DataFusionCopyEnum.RESOURCE);
    }
}
