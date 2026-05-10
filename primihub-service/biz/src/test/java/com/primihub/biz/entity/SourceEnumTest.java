package com.primihub.biz.entity;

import com.primihub.biz.entity.data.dataenum.SourceEnum;
import com.primihub.biz.service.data.db.impl.MySqlServiceAbstract;
import com.primihub.biz.service.data.db.impl.OtherServiceAbstract;
import com.primihub.biz.service.data.db.impl.SqliteServiceAbstract;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SourceEnumTest {

    @Test
    void shouldHaveAllSources() {
        assertThat(SourceEnum.values()).hasSize(6);
    }

    @Test
    void mysql_shouldMapCorrectly() {
        assertThat(SourceEnum.mysql.getSourceType()).isEqualTo(1);
        assertThat(SourceEnum.mysql.getSourceName()).isEqualTo("mysql");
        assertThat(SourceEnum.mysql.getSourceServiceClass()).isEqualTo(MySqlServiceAbstract.class);
    }

    @Test
    void sqlite_shouldMapCorrectly() {
        assertThat(SourceEnum.sqlite.getSourceType()).isEqualTo(2);
        assertThat(SourceEnum.sqlite.getSourceServiceClass()).isEqualTo(SqliteServiceAbstract.class);
    }

    @Test
    void hive_shouldUseOtherService() {
        assertThat(SourceEnum.hive.getSourceServiceClass()).isEqualTo(OtherServiceAbstract.class);
    }

    @Test
    void sourceMap_shouldContainAll() {
        assertThat(SourceEnum.SOURCE_MAP).hasSize(6);
        assertThat(SourceEnum.SOURCE_MAP.get(1)).isEqualTo(SourceEnum.mysql);
        assertThat(SourceEnum.SOURCE_MAP.get(4)).isEqualTo(SourceEnum.dm);
        assertThat(SourceEnum.SOURCE_MAP.get(6)).isEqualTo(SourceEnum.oracle);
    }
}
